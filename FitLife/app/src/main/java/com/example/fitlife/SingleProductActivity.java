package com.example.fitlife;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import model.Product;
import model.SQLiteHelper;

public class SingleProductActivity extends AppCompatActivity {

    HashMap<String, String> hashMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String x = intent.getStringExtra("pid");

        TextView title = findViewById(R.id.textView29);
        TextView description = findViewById(R.id.textView30);
        TextView price = findViewById(R.id.textView31);
        TextView qty = findViewById(R.id.textView32);
        ImageView image = findViewById(R.id.imageView6);


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("product")
                .document(x)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                hashMap.put("pid", x);
                                hashMap.put("title", String.valueOf(document.get("title")));
                                hashMap.put("price", String.valueOf(document.get("price")));
                                hashMap.put("qty", String.valueOf(document.get("qty")));
                                hashMap.put("uri", String.valueOf(document.get("uri")));

                                title.setText(String.valueOf(document.get("title")));
                                description.setText(String.valueOf(document.get("description")));
                                price.setText(String.valueOf("Rs." + document.get("price")));
                                qty.setText(String.valueOf("Quantity - " + document.get("qty")));

                                Glide.with(SingleProductActivity.this)
                                        .load(String.valueOf(document.get("uri")))  // Use the correct variable from the cursor
                                        .into(image);
                            }
                        }
                    }
                });


        Button button12 = findViewById(R.id.button12);
        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText editTextNumber3 = findViewById(R.id.editTextNumber3);
                String addqty = editTextNumber3.getText().toString();

                if (addqty.isEmpty()) {
                    Toast.makeText(SingleProductActivity.this, "Please add quantity", Toast.LENGTH_SHORT).show();
                } else if (addqty.equals("0")) {
                    Toast.makeText(SingleProductActivity.this, "Quantity must be more than 0", Toast.LENGTH_SHORT).show();
                } else if (Integer.parseInt(addqty) > Integer.parseInt(hashMap.get("qty"))) {
                    Toast.makeText(SingleProductActivity.this, "Quantity is heigher.", Toast.LENGTH_SHORT).show();
                } else {


                    SQLiteHelper sqLiteHelper = new SQLiteHelper(
                            SingleProductActivity.this,
                            "fitlife.db",
                            null,
                            1);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SQLiteDatabase sqLiteDatabase = sqLiteHelper.getReadableDatabase();

                            // Define selection criteria (WHERE clause)
                            String selection = "pid = ?";
                            String[] selectionArgs = new String[]{hashMap.get("pid")}; // Replace "5" with your desired ID

                            Cursor cursor = sqLiteDatabase.query(
                                    "product",
                                    null, // Select all columns
                                    selection, // WHERE clause
                                    selectionArgs, // Arguments for WHERE clause
                                    null,
                                    null,
                                    null
                            );

                            if (cursor.moveToFirst()) {
                                // update quantity
                                int existingQty = cursor.getInt(cursor.getColumnIndexOrThrow("qty"));
                                int newQty = existingQty + Integer.parseInt(addqty);

                                ContentValues contentValues = new ContentValues();
                                contentValues.put("qty", newQty);

                                SQLiteDatabase writableDatabase = sqLiteHelper.getWritableDatabase();
                                int updatedRows = writableDatabase.update(
                                        "product",
                                        contentValues,
                                        "pid = ?",
                                        new String[]{hashMap.get("pid")}
                                );

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(SingleProductActivity.this, "Item quantity update", Toast.LENGTH_SHORT).show();
                                        editTextNumber3.setText("");
                                    }
                                });

                            } else {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SQLiteDatabase sqLiteDatabase = sqLiteHelper.getWritableDatabase();

                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("pid",hashMap.get("pid"));
                                        contentValues.put("title",hashMap.get("title"));
                                        contentValues.put("price",hashMap.get("price"));
                                        contentValues.put("qty",addqty);
                                        contentValues.put("uri",hashMap.get("uri"));

                                        long insertId = sqLiteDatabase.insert(
                                                "product",
                                                null,
                                                contentValues
                                        );

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(SingleProductActivity.this, "Item add to cart", Toast.LENGTH_SHORT).show();
                                                editTextNumber3.setText("");
                                            }
                                        });

                                    }
                                }).start();

                            }

                            cursor.close();
                            sqLiteDatabase.close();
                        }
                    }).start();

                }
            }
        });

    }
}
