package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddExNuSuActivity extends AppCompatActivity {

    String email;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ex_nu_su);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i = getIntent();
        email = i.getStringExtra("email");

        db = FirebaseFirestore.getInstance();

        // Load existing data
        loadExistingData();

        // Add/Update Exercises
        Button button15 = findViewById(R.id.button15);
        button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextText11 = findViewById(R.id.editTextText11);
                String exercises = editTextText11.getText().toString();

                if (exercises.isEmpty()) {
                    Toast.makeText(AddExNuSuActivity.this, "Please Add Exercises", Toast.LENGTH_SHORT).show();
                } else {
                    updateOrAddData("exercises", exercises, editTextText11);
                }
            }
        });

        // Add/Update Nutrition
        Button button16 = findViewById(R.id.button16);
        button16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextText12 = findViewById(R.id.editTextText12);
                String nutrition = editTextText12.getText().toString();

                if (nutrition.isEmpty()) {
                    Toast.makeText(AddExNuSuActivity.this, "Please Add Nutritions", Toast.LENGTH_SHORT).show();
                } else {
                    updateOrAddData("nutrition", nutrition, editTextText12);
                }
            }
        });

        // Add/Update Subscription
        Button button17 = findViewById(R.id.button17);
        button17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editTextNumber4 = findViewById(R.id.editTextNumber4);
                String subscription = editTextNumber4.getText().toString();

                if (subscription.isEmpty()) {
                    Toast.makeText(AddExNuSuActivity.this, "Please Add Subscription", Toast.LENGTH_SHORT).show();
                } else {
                    updateOrAddData("subscriptions", subscription, editTextNumber4);
                }
            }
        });
    }

    // Method to load existing data
    private void loadExistingData() {
        db.collection("exercises")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String exercises = document.getString("exercises");
                        EditText editTextText11 = findViewById(R.id.editTextText11);
                        editTextText11.setText(exercises);
                    }
                });

        db.collection("nutrition")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String nutrition = document.getString("nutrition");
                        EditText editTextText12 = findViewById(R.id.editTextText12);
                        editTextText12.setText(nutrition);
                    }
                });

        db.collection("subscriptions")
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String subscription = document.getString("subscriptions");
                        EditText editTextNumber4 = findViewById(R.id.editTextNumber4);
                        editTextNumber4.setText(subscription);
                    }
                });
    }

    // Method to update or add data
    private void updateOrAddData(String collection, String data, EditText editText) {
        db.collection(collection)
                .whereEqualTo("userEmail", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Document exists, update it
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        db.collection(collection)
                                .document(documentId)
                                .update("userEmail", email, collection, data)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AddExNuSuActivity.this, collection.substring(0, 1).toUpperCase() + collection.substring(1) + " Updated Successfully", Toast.LENGTH_SHORT).show();
                                    editText.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddExNuSuActivity.this, "Failed to Update " + collection, Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Document does not exist, add new
                        Map<String, Object> newData = new HashMap<>();
                        newData.put("userEmail", email);
                        newData.put(collection, data);

                        db.collection(collection)
                                .add(newData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(AddExNuSuActivity.this, collection.substring(0, 1).toUpperCase() + collection.substring(1) + " Added Successfully", Toast.LENGTH_SHORT).show();
                                    editText.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AddExNuSuActivity.this, "Failed to Add " + collection, Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddExNuSuActivity.this, "Failed to Check Existing Data", Toast.LENGTH_SHORT).show();
                });
    }
}