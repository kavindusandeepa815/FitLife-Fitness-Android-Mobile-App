package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminSignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText email = findViewById(R.id.editTextTextEmailAddress3);
                EditText password = findViewById(R.id.editTextTextPassword3);

                if(email.getText().toString().isEmpty()){
                    Toast.makeText(AdminSignInActivity.this, "Please enter email.", Toast.LENGTH_SHORT).show();
                } else if(password.getText().toString().isEmpty()) {
                    Toast.makeText(AdminSignInActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("admin")
                            .whereEqualTo("email", email.getText().toString())
                            .whereEqualTo("password", password.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            // User already exists
                                            Intent intent = new Intent(AdminSignInActivity.this,AdminActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                            startActivity(intent);
                                        } else {
                                            // User does not exist
                                            Toast.makeText(AdminSignInActivity.this, "Invalid Details", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(AdminSignInActivity.this, "Error checking user!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }


        });

    }
}