package com.example.fitlife;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import model.StepData;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView imageView1 = findViewById(R.id.imageView1);
        SpringAnimation springAnimation = new SpringAnimation(imageView1, DynamicAnimation.TRANSLATION_Y);

        SpringForce springForce = new SpringForce();
        springForce.setStiffness(SpringForce.STIFFNESS_LOW);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        springForce.setFinalPosition(370f);

        springAnimation.setSpring(springForce);

        springAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            @Override
            public void onAnimationEnd(DynamicAnimation animation, boolean canceled, float value, float velocity) {

                // Get SharedPreferences
                SharedPreferences sp = getSharedPreferences("com.example.fitlife.data", Context.MODE_PRIVATE);

                if(!sp.contains("tracking")){
                    List<StepData> stepDataList = new ArrayList<>();
                    stepDataList.add(new StepData("Sunday", 50));
                    stepDataList.add(new StepData("Monday", 10));
                    stepDataList.add(new StepData("Tuesday", 20));
                    stepDataList.add(new StepData("Wednesday", 40));
                    stepDataList.add(new StepData("Thursday", 80));
                    stepDataList.add(new StepData("Friday", 55));
                    stepDataList.add(new StepData("Saturday ", 40));

                    SharedPreferences spp = getSharedPreferences(
                            "com.example.fitlife.data",
                            Context.MODE_PRIVATE
                    );

                    Gson gson = new Gson();
                    String  datajson = gson.toJson(stepDataList);

                    SharedPreferences.Editor editor = spp.edit();
                    editor.putString("tracking",datajson);
                    editor.apply();
                }

                // Check if "user" exists
                if (sp.contains("user")) {
                    // User exists -> Go to HomeActivity
                    Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                    startActivity(intent);
                } else {
                    // No user found -> Go to SignInActivity
                    Intent intent = new Intent(MainActivity.this,SignInActivity.class);
                    startActivity(intent);
                }

                // Finish current activity so it doesn't stay in the back stack
                finish();

            }
        });

        springAnimation.start();

    }
}