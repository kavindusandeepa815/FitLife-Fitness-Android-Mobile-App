package com.example.fitlife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Objects;

import model.CountryData;
import model.User;

public class SignInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView6 = findViewById(R.id.textView6);
        textView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(SignInActivity.this,AdminSignInActivity.class);
                startActivity(intent1);
            }
        });

        TextView textView7 = findViewById(R.id.textView7);
        textView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(SignInActivity.this,RegisterActivity.class);
                startActivity(intent2);
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText email = findViewById(R.id.editTextTextEmailAddress);
                EditText password = findViewById(R.id.editTextTextPassword);

                if(email.getText().toString().isEmpty()){
                    Toast.makeText(SignInActivity.this, "Please enter email.", Toast.LENGTH_SHORT).show();
                } else if(password.getText().toString().isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    firestore.collection("user")
                            .whereEqualTo("email", email.getText().toString())
                            .whereEqualTo("password", password.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            // User already exists

                                            User user  = new User("",email.getText().toString(),"","","","","");

                                            Gson gson = new Gson();
                                            String  userjson = gson.toJson(user);

                                            SharedPreferences sp = getSharedPreferences(
                                                    "com.example.fitlife.data",
                                                    Context.MODE_PRIVATE
                                            );

                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("user",userjson);
                                            editor.apply();


                                            Intent intent = new Intent(SignInActivity.this,HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                            startActivity(intent);
                                        } else {
                                            // User does not exist
                                            Toast.makeText(SignInActivity.this, "Invalid Details", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(SignInActivity.this, "Error checking user!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }

            }


        });

    }
}