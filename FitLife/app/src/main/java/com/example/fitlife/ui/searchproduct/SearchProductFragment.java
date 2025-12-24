package com.example.fitlife.ui.searchproduct;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fitlife.GMapActivity;
import com.example.fitlife.R;
import com.example.fitlife.SingleProductActivity;
import com.example.fitlife.databinding.FragmentSearchProductBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.bumptech.glide.Glide;

import model.Product;

public class SearchProductFragment extends Fragment {

    private FragmentSearchProductBinding binding;
    private ArrayList<Product> productArrayList = new ArrayList<>();
    private ProductAdapter productAdapter;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText editTextText3 = root.findViewById(R.id.editTextText3);
        Button button10 = root.findViewById(R.id.button10);
        RecyclerView recyclerView3 = root.findViewById(R.id.recyclerView3);

        // Set up RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView3.setLayoutManager(linearLayoutManager);
        productAdapter = new ProductAdapter(productArrayList);
        recyclerView3.setAdapter(productAdapter);

        // Load all products when the fragment is created
        loadAllProducts();

        // Set up search button click listener
        button10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = editTextText3.getText().toString().trim();
                filterProducts(searchText);

            }
        });

        return root;
    }

    private void loadAllProducts() {
        firestore.collection("product")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                productArrayList.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Product product = new Product();
                                    product.setTitle(document.getString("title"));
                                    product.setDescription(document.getString("description"));
                                    product.setPrice(document.getString("price"));
                                    product.setQty(document.getString("qty") + "ABC" + document.getId());
                                    product.setUri(document.getString("uri"));
                                    productArrayList.add(product);
                                }
                                productAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getContext(), "No products found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("FirestoreError", "Error getting documents", task.getException());
                        }
                    }
                });
    }

    private void filterProducts(String searchText) {
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product product : productArrayList) {
            if (product.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    class ProductAdapter extends RecyclerView.Adapter<SearchProductViewHolder> {

        private ArrayList<Product> productArrayList;

        public ProductAdapter(ArrayList<Product> productArrayList) {
            this.productArrayList = productArrayList;
        }

        public void updateList(ArrayList<Product> filteredList) {
            this.productArrayList = filteredList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SearchProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View productView = layoutInflater.inflate(R.layout.searchproduct_item, parent, false);
            return new SearchProductViewHolder(productView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchProductViewHolder holder, int position) {
            Product product = productArrayList.get(position);
            holder.productTitle.setText(product.getTitle());
            holder.productPrice.setText("Rs." + product.getPrice());

            // Load image using Glide
            Glide.with(getContext())
                    .load(product.getUri())
                    .into(holder.productImage);

            holder.viewProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SingleProductActivity.class);
                    intent.putExtra("pid", product.getQty().split("ABC")[1]);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return productArrayList.size();
        }
    }

    class SearchProductViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productTitle;
        TextView productPrice;
        Button viewProduct;

        public SearchProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.imageView77);
            productTitle = itemView.findViewById(R.id.textView277);
            productPrice = itemView.findViewById(R.id.textView288);
            viewProduct = itemView.findViewById(R.id.button111);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}