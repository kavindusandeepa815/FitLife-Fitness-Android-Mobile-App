package com.example.fitlife;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import model.SQLiteHelper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001; // Unique request ID
    private static final String CHANNEL_ID = "Checkout_Channel";
    private static final String MERCHANT_ID = "1221608"; // Move to secure storage
    private static final String API_URL = "https://390391373651.ngrok-free.app/fitlife/Checkout"; // Use HTTPS

    private double totalPrice = 0;
    private String firstname, lastname, email, mobile, address, city, country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Get total price from intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0);

        // Initialize views
        Button checkoutButton = findViewById(R.id.button14);
        checkoutButton.setOnClickListener(view -> validateAndProceedToPayment());
    }

    private void validateAndProceedToPayment() {
        // Get input values
        firstname = ((EditText) findViewById(R.id.editTextText6)).getText().toString();
        lastname = ((EditText) findViewById(R.id.editTextText7)).getText().toString();
        email = ((EditText) findViewById(R.id.editTextTextEmailAddress4)).getText().toString();
        mobile = ((EditText) findViewById(R.id.editTextPhone2)).getText().toString();
        address = ((EditText) findViewById(R.id.editTextText8)).getText().toString();
        city = ((EditText) findViewById(R.id.editTextText9)).getText().toString();
        country = ((EditText) findViewById(R.id.editTextText10)).getText().toString();

        // Validate inputs
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || mobile.isEmpty() ||
                address.isEmpty() || city.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
        } else {
            initiatePayment();
        }
    }

    private void initiatePayment() {
        InitRequest req = new InitRequest();
        req.setMerchantId(MERCHANT_ID);
        req.setCurrency("LKR");
        req.setAmount(totalPrice);
        req.setOrderId("ORDER_" + System.currentTimeMillis());
        req.setItemsDescription("FitLife Products");
        req.getCustomer().setFirstName(firstname);
        req.getCustomer().setLastName(lastname);
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);
        req.getCustomer().getAddress().setAddress(address);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry(country);
        req.setSandBox(true); // Use sandbox for testing

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL); // Use SANDBOX_URL for testing
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYHERE_REQUEST && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK && response != null) {
                if (response.isSuccess()) {
                    handlePaymentSuccess();
                } else {
                    Toast.makeText(this, "Payment failed: " + response.toString(), Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment canceled by user", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handlePaymentSuccess() {
        // Fetch cart items from SQLite database
        new Thread(() -> {
            List<HashMap<String, String>> cartList = fetchCartItems();
            if (cartList != null && !cartList.isEmpty()) {
                sendHttpRequest(cartList);
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<HashMap<String, String>> fetchCartItems() {
        List<HashMap<String, String>> cartList = new ArrayList<>();
        SQLiteHelper sqLiteHelper = new SQLiteHelper(this, "fitlife.db", null, 1);
        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = sqLiteDatabase.rawQuery("SELECT * FROM product", null);
            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> item = new HashMap<>();
                    item.put("pid", cursor.getString(cursor.getColumnIndex("pid")));
                    item.put("title", cursor.getString(cursor.getColumnIndex("title")));
                    item.put("price", cursor.getString(cursor.getColumnIndex("price")));
                    item.put("qty", cursor.getString(cursor.getColumnIndex("qty")));
                    item.put("uri", cursor.getString(cursor.getColumnIndex("uri")));
                    cartList.add(item);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error fetching cart items", e);
        } finally {
            if (cursor != null) cursor.close();
            sqLiteDatabase.close();
        }

        return cartList;
    }

    private void sendHttpRequest(List<HashMap<String, String>> cartList) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("firstname", firstname);
        jsonObject.addProperty("lastname", lastname);
        jsonObject.addProperty("email", email);
        jsonObject.addProperty("mobile", mobile);
        jsonObject.addProperty("address", address);
        jsonObject.addProperty("city", city);
        jsonObject.addProperty("country", country);
        jsonObject.add("cartlist", gson.toJsonTree(cartList));

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject responseJson = gson.fromJson(response.body().string(), JsonObject.class);
                if (Objects.requireNonNull(responseJson).get("message").getAsString().equals("Success")) {
                    removeCart();
                    reduceQuantityInFirestore(cartList); // Reduce quantity in Firestore
                    sendNotification();
                    Intent i = new Intent(CheckoutActivity.this, GMapActivity.class);
                    i.putExtra("city", city);
                    i.putExtra("country", country);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                    startActivity(i);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Something went wrong.", Toast.LENGTH_LONG).show());
                }
            }
        } catch (IOException e) {
            Log.e("CheckoutActivity", "HTTP request failed", e);
            runOnUiThread(() -> Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_LONG).show());
        }
    }

    private void reduceQuantityInFirestore(List<HashMap<String, String>> cartList) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (HashMap<String, String> item : cartList) {
            String productId = item.get("pid");
            String purchasedQty = item.get("qty");

            db.collection("product").document(productId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String currentQty = documentSnapshot.get("qty").toString();
                                int newQty = Integer.parseInt(currentQty) - Integer.parseInt(purchasedQty);
                                String newQtyString = String.valueOf(newQty);

                                db.collection("product").document(productId)
                                        .update("qty", newQtyString)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("CheckoutActivity", "Quantity updated successfully for product: " + productId);
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("CheckoutActivity", "Error updating quantity for product: " + productId, e);
                                        });
                            } else {
                                Log.e("CheckoutActivity", "Product not found: " + productId);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("CheckoutActivity", "Error fetching product: " + productId, e);
                    });
        }
    }

    private void removeCart() {
        new Thread(() -> {
            SQLiteHelper sqLiteHelper = new SQLiteHelper(this, "fitlife.db", null, 1);
            SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

            try {
                // Delete all items from the product table
                int deletedRows = sqLiteDatabase.delete("product", null, null);
                if (deletedRows > 0) {
                    Log.d("CheckoutActivity", "Cart items deleted successfully.");
                    runOnUiThread(() -> Toast.makeText(this, "Cart cleared successfully.", Toast.LENGTH_SHORT).show());
                } else {
                    Log.d("CheckoutActivity", "No items to delete.");
                    runOnUiThread(() -> Toast.makeText(this, "Cart is already empty.", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e("CheckoutActivity", "Error deleting cart items", e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to clear cart.", Toast.LENGTH_SHORT).show());
            } finally {
                sqLiteDatabase.close();
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Checkout Notifications", NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Checkout Complete")
                .setContentText("Your order is being processed.")
                .setSmallIcon(R.drawable.fitlife)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        notificationManager.notify(1, notification);
    }
}