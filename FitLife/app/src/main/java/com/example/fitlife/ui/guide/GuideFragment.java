package com.example.fitlife.ui.guide;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.example.fitlife.R;
import com.example.fitlife.databinding.FragmentGuideBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import model.User;

public class GuideFragment extends Fragment {

    String email;
    private FirebaseFirestore db;
    private FragmentGuideBinding binding;

    private static final int PAYHERE_REQUEST = 11001; // Unique request ID
    private static final String CHANNEL_ID = "Checkout_Channel";
    private static final String MERCHANT_ID = "1221608"; // Move to secure storage

    String subscriptionData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGuideBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get user email from SharedPreferences
        SharedPreferences sp = requireContext().getSharedPreferences(
                "com.example.fitlife.data",
                Context.MODE_PRIVATE
        );
        String userJson = sp.getString("user", null);
        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);
        email = user.getEmail();

        // Initialize EditText fields
        EditText exercises = root.findViewById(R.id.editTextTextMultiLine2F);
        EditText nutritions = root.findViewById(R.id.editTextTextMultiLine3F);
        TextView subscription = root.findViewById(R.id.textView39F);
        TextView subscriptionDetails = root.findViewById(R.id.textView40F);
        Button button = root.findViewById(R.id.button18F);

        // Make EditText fields non-editable
        exercises.setKeyListener(null);
        nutritions.setKeyListener(null);
        subscription.setKeyListener(null);


        // Load existing data from Firestore
        loadExistingData(exercises, nutritions, subscription, subscriptionDetails, button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePayment();
            }
        });

        return root;
    }

    // Method to load existing data from Firestore
    private void loadExistingData(EditText exercises, EditText nutritions, TextView subscription, TextView subscriptionDetails, Button button) {

        // Load Exercises
        db.collection("exercises")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String exercisesData = queryDocumentSnapshots.getDocuments().get(0).getString("exercises");
                        exercises.setText(exercisesData);
                    } else {
                        exercises.setText("No exercises data found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load exercises data.", Toast.LENGTH_SHORT).show();
                });

        // Load Nutrition
        db.collection("nutrition")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String nutritionData = queryDocumentSnapshots.getDocuments().get(0).getString("nutrition");
                        nutritions.setText(nutritionData);
                    } else {
                        nutritions.setText("No nutrition data found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load nutrition data.", Toast.LENGTH_SHORT).show();
                });

        // Load Subscription
        db.collection("subscriptions")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        subscriptionData = queryDocumentSnapshots.getDocuments().get(0).getString("subscriptions");
                        subscription.setText("Rs." + subscriptionData);

                        // Fetch startdate and enddate
                        long startDateMillis = queryDocumentSnapshots.getDocuments().get(0).getLong("startdate");
                        long endDateMillis = queryDocumentSnapshots.getDocuments().get(0).getLong("enddate");

                        // Format dates
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        String startDate = sdf.format(new Date(startDateMillis));
                        String endDate = sdf.format(new Date(endDateMillis));

                        // Set the formatted dates in the new TextView
                        subscriptionDetails.setText("From: " + startDate + " To " + endDate);
                    } else {
                        button.setKeyListener(null);
                        subscription.setText("Rs.0.00");
                        subscriptionDetails.setText("No active subscription.");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load subscription data.", Toast.LENGTH_SHORT).show();
                });
    }


    private void initiatePayment() {
        InitRequest req = new InitRequest();
        req.setMerchantId("1221608");       // Replace with your Merchant ID
        req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
        req.setAmount(Double.parseDouble(subscriptionData));         // Final Amount to be charged
        req.setOrderId("SUBC_" + System.currentTimeMillis());  // Ensure unique order ID
        req.setItemsDescription("FitLife Subscription");  // Item description title
        req.setCustom1("Custom message 1");
        req.setCustom2("Custom message 2");
        req.getCustomer().setFirstName("None");
        req.getCustomer().setLastName("None");
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone("000000000");
        req.getCustomer().getAddress().setAddress("None");
        req.getCustomer().getAddress().setCity("None");
        req.getCustomer().getAddress().setCountry("None");
        req.setSandBox(true);

        Intent intent = new Intent(getContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Use SANDBOX_URL for testing
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null) {
                    if (response.isSuccess()) {
                        msg = "Payment success: " + response.getData().toString();
                        sendNotification();
                        adddb();
                    } else {
                        msg = "Payment failed: " + response.toString();
                    }
                } else {
                    msg = "No response from PayHere";
                }
                Log.d("PayHere", msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Payment canceled by user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void adddb() {
        // Get today's date
        long today = System.currentTimeMillis();
        // Calculate end date (30 days from today)
        long endDate = today + (30L * 24 * 60 * 60 * 1000); // 30 days in milliseconds

        // Create a Map to store the data
        Map<String, Object> subscriptionData = new HashMap<>();
        subscriptionData.put("userEmail", email);
        subscriptionData.put("startdate", today);
        subscriptionData.put("enddate", endDate);

        // Check if a subscription document already exists for the user
        db.collection("subscriptions")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Document exists, update it
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection("subscriptions")
                                .document(documentId)
                                .update(subscriptionData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "Subscription updated successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to update subscription.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document does not exist, add new
                        db.collection("subscriptions")
                                .add(subscriptionData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(requireContext(), "Subscription added successfully!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "Failed to add subscription.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to check existing subscription.", Toast.LENGTH_SHORT).show();
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Checkout Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setContentTitle("Payment Complete")
                .setContentText("Your have to successfully subscribed FitLife app.")
                .setSmallIcon(R.drawable.fitlife)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clean up binding
    }
}