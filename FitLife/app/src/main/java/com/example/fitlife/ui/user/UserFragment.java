package com.example.fitlife.ui.user;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// import com.example.fitlife.Manifest;
import com.example.fitlife.AddExNuSuActivity;
import com.example.fitlife.R;
import com.example.fitlife.SignInActivity;
import com.example.fitlife.databinding.FragmentUserBinding;
import com.example.fitlife.ui.home.HomeFragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import model.User;
import model.Workout;

public class UserFragment extends Fragment {
    private PieChart pieChart1;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FragmentUserBinding binding;
    private ArrayList<User> userArrayList = new ArrayList<>();
    private UserAdapter userAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        EditText editTextPhone5 = root.findViewById(R.id.editTextPhone5);
        Button button8 = root.findViewById(R.id.button8);
        RecyclerView recyclerView2 = root.findViewById(R.id.recyclerView2);
        pieChart1 = root.findViewById(R.id.pieChart1);

        // Set up RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView2.setLayoutManager(linearLayoutManager);
        userAdapter = new UserAdapter(userArrayList);
        recyclerView2.setAdapter(userAdapter);

        // Load all users when the fragment is created
        loadAllUsers();

        // Load gender data for the pie chart
        loadGenderData();

        // Set up search button click listener
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchText = editTextPhone5.getText().toString().trim();
                filterUsers(searchText);
            }
        });

        return root;
    }

    // Load all users from Firestore
    private void loadAllUsers() {
        firestore.collection("user")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = new User(
                                        document.getString("name") + "ABC" + document.getId(),
                                        document.getString("email"),
                                        document.getString("mobile"),
                                        document.getString("password"),
                                        document.getString("country"),
                                        document.getString("gender"),
                                        document.getString("status")
                                );
                                userArrayList.add(user);
                            }
                            userAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("FirestoreError", "Error getting documents", task.getException());
                        }
                    }
                });
    }

    // Filter users based on the entered name
    private void filterUsers(String searchText) {
        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : userArrayList) {
            if (user.getMobile().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateList(filteredList);
    }

    // Load gender data for the pie chart
    private void loadGenderData() {
        firestore.collection("user").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int maleCount = 0;
                            int femaleCount = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String gender = document.getString("gender");
                                if (gender != null) {
                                    if (gender.equalsIgnoreCase("Male")) {
                                        maleCount++;
                                    } else if (gender.equalsIgnoreCase("Female")) {
                                        femaleCount++;
                                    }
                                }
                            }

                            updatePieChart(maleCount, femaleCount);
                        }
                    }
                });
    }

    // Update the pie chart with gender data
    private void updatePieChart(int maleCount, int femaleCount) {
        ArrayList<PieEntry> pieEntryArrayList = new ArrayList<>();
        pieEntryArrayList.add(new PieEntry(maleCount, "Male"));
        pieEntryArrayList.add(new PieEntry(femaleCount, "Female"));

        PieDataSet pieDataSet = new PieDataSet(pieEntryArrayList, "Gender");
        pieDataSet.setColors(Color.RED, Color.BLUE);
        pieDataSet.setValueTextSize(12f);

        PieData pieData = new PieData(pieDataSet);
        pieChart1.setData(pieData);

        // Chart Customization
        pieChart1.animateY(1000, Easing.EaseInCirc);
        pieChart1.setEntryLabelTextSize(14f);
        pieChart1.setDescription(null);
        pieChart1.invalidate(); // Refresh chart
    }

    // RecyclerView Adapter for Users
    class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private ArrayList<User> userArrayList;

        public UserAdapter(ArrayList<User> userArrayList) {
            this.userArrayList = userArrayList;
        }

        public void updateList(ArrayList<User> filteredList) {
            this.userArrayList = filteredList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View userView = layoutInflater.inflate(R.layout.user_item, parent, false);
            return new UserViewHolder(userView);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userArrayList.get(position);
            holder.textViewNameLetter.setText(String.valueOf(user.getName().charAt(0)));
            holder.textViewName.setText(String.valueOf(user.getName().split("ABC")[0]));
            holder.buttonStatus.setText(String.valueOf(user.getStatus()));

            holder.textViewName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent i = new Intent(holder.itemView.getContext(), AddExNuSuActivity.class);
                    i.putExtra("email",String.valueOf(user.getEmail()));
                    startActivity(i);
                    return false;
                }
            });

            holder.buttonStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newStatus = user.getStatus().equals("Active") ? "Inactive" : "Active";
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("status", newStatus);
                    firestore.collection("user")
                            .document(user.getName().split("ABC")[1])
                            .update(update)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "Update Success", Toast.LENGTH_SHORT).show();
                                    user.setStatus(newStatus);
                                    notifyItemChanged(position);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });

            // Make phone call to user on long press
            holder.textViewNameLetter.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String phoneNumber = user.getMobile(); // Get the user's mobile number
                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + phoneNumber));
                        try {
                            checkCallPermission(phoneNumber);
                            getContext().startActivity(intent); // Start the phone call
                        } catch (SecurityException e) {
                            Toast.makeText(getContext(), "Permission denied to make phone calls", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No phone number available", Toast.LENGTH_SHORT).show();
                    }
                    return true; // Return true to indicate the long click event is consumed
                }
            });
        }


        @Override
        public int getItemCount() {
            return userArrayList.size();
        }
    }

    private static final int REQUEST_CALL_PHONE = 1;

    private void checkCallPermission(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            makePhoneCall(phoneNumber);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, make the phone call
               // String phoneNumber = userArrayList.get(adapterPosition).getMobile();
              //  makePhoneCall(phoneNumber);
                Toast.makeText(getContext(), "Permission granted. Call again. ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission denied to make phone calls", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            getContext().startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Permission denied to make phone calls", Toast.LENGTH_SHORT).show();
        }
    }

    // ViewHolder for User RecyclerView
    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNameLetter;
        TextView textViewName;
        Button buttonStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNameLetter = itemView.findViewById(R.id.textView23);
            textViewName = itemView.findViewById(R.id.textView24);
            buttonStatus = itemView.findViewById(R.id.button9);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}