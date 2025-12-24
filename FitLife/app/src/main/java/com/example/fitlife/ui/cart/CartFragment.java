package com.example.fitlife.ui.cart;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fitlife.CheckoutActivity;
import com.example.fitlife.R;
import com.example.fitlife.SingleProductActivity;
import com.example.fitlife.databinding.FragmentCartBinding;
import com.example.fitlife.ui.searchproduct.SearchProductFragment;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import model.SQLiteHelper;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    RecyclerView recyclerView4;
    CartAdapter cartAdapter;
    SQLiteHelper sqLiteHelper;
    TextView totalPriceTextView; // Add this line

    private static final int PAYHERE_REQUEST = 11001; // Unique request ID

    double totalPrice = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView4 = root.findViewById(R.id.recyclerView4);
        totalPriceTextView = root.findViewById(R.id.textView27); // Initialize the TextView

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView4.setLayoutManager(layoutManager);

        sqLiteHelper = new SQLiteHelper(
                getContext(),
                "fitlife.db",
                null,
                1);

        loadCartItems();

        Button button11 = root.findViewById(R.id.button11);
        button11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CheckoutActivity.class);
                intent.putExtra("totalPrice", totalPrice);
                startActivity(intent);


            }
        });

        return root;
    }

    private void loadCartItems() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
                Cursor cursor = sqLiteDatabase.query(
                        "product",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (cursor != null && cursor.getCount() > 0) {
                            cartAdapter = new CartAdapter(cursor);
                            recyclerView4.setAdapter(cartAdapter);
                            calculateTotalPrice(cursor); // Calculate and display total price
                        } else {
                            Toast.makeText(getContext(), "Cart is Empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }).start();
    }


    private void calculateTotalPrice(Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String price = cursor.getString(3); // Price column
                String qty = cursor.getString(4);   // Quantity column

                double itemPrice = Double.parseDouble(price);
                int itemQty = Integer.parseInt(qty);

                totalPrice += itemPrice * itemQty; // Calculate total price
            } while (cursor.moveToNext());
        }

        // Update the TextView with the total price
        totalPriceTextView.setText("Total: Rs." + totalPrice);
    }


//    private void initiatePayment() {
//        InitRequest req = new InitRequest();
//        req.setMerchantId("1221608");       // Replace with your Merchant ID
//        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
//        req.setAmount(totalPrice);         // Final Amount to be charged
//        req.setOrderId("ORDER_" + System.currentTimeMillis());  // Ensure unique order ID
//        req.setItemsDescription("FitLife Products");  // Item description title
//        req.setCustom1("Custom message 1");
//        req.setCustom2("Custom message 2");
//        req.getCustomer().setFirstName("John");
//        req.getCustomer().setLastName("Doe");
//        req.getCustomer().setEmail("johndoe@gmail.com");
//        req.getCustomer().setPhone("+94771234567");
//        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
//        req.getCustomer().getAddress().setCity("Colombo");
//        req.getCustomer().getAddress().setCountry("Sri Lanka");
//        req.setSandBox(true);
//
//        // Optional Params
//       // req.setNotifyUrl("https://your-notify-url.com"); // Notify URL
//       // req.getItems().add(new Item(null, "FitLife Products", 1, totalPrice));
//
//        Intent intent = new Intent(getContext(), PHMainActivity.class);
//        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
//        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Use SANDBOX_URL for testing
//        startActivityForResult(intent, PAYHERE_REQUEST);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
//            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
//            if (resultCode == Activity.RESULT_OK) {
//                String msg;
//                if (response != null) {
//                    if (response.isSuccess()) {
//                        msg = "Payment success: " + response.getData().toString();
//                    } else {
//                        msg = "Payment failed: " + response.toString();
//                    }
//                } else {
//                    msg = "No response from PayHere";
//                }
//                Log.d("PayHere", msg);
//                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
//            } else if (resultCode == Activity.RESULT_CANCELED) {
//                Toast.makeText(getContext(), "Payment canceled by user", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


    @Override
    public void onResume() {
        super.onResume();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                CartAdapter.CartViewHolder holder = (CartAdapter.CartViewHolder) viewHolder;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();
                        int row = sqLiteDatabase.delete(
                                "product",
                                "`id`=?",
                                new String[]{holder.id}
                        );

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (row > 0) {
                                    // Refresh the cursor and update the adapter
                                    refreshCursor();
                                    Toast.makeText(viewHolder.itemView.getContext(), "Item Deleted.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(viewHolder.itemView.getContext(), "Failed to delete item.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                }).start();

            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView4);
    }

    private void refreshCursor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
                Cursor newCursor = sqLiteDatabase.query(
                        "product",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the adapter with the new cursor
                        cartAdapter.swapCursor(newCursor);
                        calculateTotalPrice(newCursor); // Recalculate total price after deletion
                    }
                });

            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartAdapter != null && cartAdapter.cursor != null) {
            cartAdapter.cursor.close();
        }
        binding = null;
    }
}

class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    Cursor cursor;

    public CartAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView priceText;
        TextView qtyText;
        ImageView productImage;

        String id;

        View containerView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textView277);
            priceText = itemView.findViewById(R.id.textView288);
            qtyText = itemView.findViewById(R.id.textView2777);
            productImage = itemView.findViewById(R.id.imageView77);

            containerView = itemView;
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        cursor.moveToPosition(position);

        holder.id = cursor.getString(0);
        String title = cursor.getString(2);
        String price = cursor.getString(3);
        String qty = cursor.getString(4);
        String uri = cursor.getString(5);

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(uri)  // Use the correct variable from the cursor
                .into(holder.productImage);

        holder.titleText.setText(title);
        holder.priceText.setText("Rs." + price);
        holder.qtyText.setText(qty);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close(); // Close the old cursor
        }
        cursor = newCursor; // Update the cursor
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }
}