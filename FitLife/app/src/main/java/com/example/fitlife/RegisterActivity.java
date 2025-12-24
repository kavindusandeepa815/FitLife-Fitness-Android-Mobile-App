package com.example.fitlife;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import model.CountryData;
import model.User;

public class RegisterActivity extends AppCompatActivity {

    boolean isFlip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner1 = findViewById(R.id.spinner1);

        ArrayList<CountryData> arrayList = new ArrayList<>();
        arrayList.add(new CountryData(R.drawable.usa,"United States"));
        arrayList.add(new CountryData(R.drawable.canada,"Canada"));
        arrayList.add(new CountryData(R.drawable.india,"India"));
        arrayList.add(new CountryData(R.drawable.germany,"Germany"));

        CountryAdapter arrayAdapter = new CountryAdapter(
                RegisterActivity.this,
                R.layout.custom_spinner_item,
                arrayList
        );

        spinner1.setAdapter(arrayAdapter);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float values[]  =  sensorEvent.values;
                float z = values[2];

                if(z<-9 && !isFlip){
                    Log.i("FitLifeLog","Flip");
                    isFlip = true;
                    TextView flip = findViewById(R.id.textView15);
                    flip.setText("Flipped: Not Robot.");
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(listener,sensor,SensorManager.SENSOR_DELAY_NORMAL);

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText name = findViewById(R.id.editTextText);
                EditText email = findViewById(R.id.editTextTextEmailAddress2);
                EditText mobile = findViewById(R.id.editTextPhone);
                EditText password = findViewById(R.id.editTextTextPassword2);

                CountryData selectedCountry = (CountryData) spinner1.getSelectedItem();

                if(name.getText().toString().isEmpty()){
                    Toast.makeText(RegisterActivity.this, "Please enter name.", Toast.LENGTH_SHORT).show();
                } else if(email.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter email.", Toast.LENGTH_SHORT).show();
                } else if(mobile.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter mobile.", Toast.LENGTH_SHORT).show();
                } else if(password.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter password.", Toast.LENGTH_SHORT).show();
                }else if(!isFlip) {
                    Toast.makeText(RegisterActivity.this, "Please flip your phone.", Toast.LENGTH_SHORT).show();
                } else {

                    String countryName = selectedCountry.getName();

                    int selectedId = radioGroup.getCheckedRadioButtonId();

                    RadioButton selectedRadioButton = findViewById(selectedId);
                    String gender = selectedRadioButton.getText().toString();

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    User user = new User(
                            name.getText().toString(),
                            email.getText().toString(),
                            mobile.getText().toString(),
                            password.getText().toString(),
                            countryName,
                            gender,
                            "Active"
                    );

                    firestore.collection("user")
                            .whereEqualTo("email", email.getText().toString())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        boolean emailExists = !task.getResult().isEmpty();

                                        firestore.collection("user")
                                                .whereEqualTo("mobile", mobile.getText().toString())
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            boolean mobileExists = !task.getResult().isEmpty();

                                                            if (emailExists || mobileExists) {
                                                                // Either email or mobile already exists
                                                                Toast.makeText(RegisterActivity.this, "User already exists!", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // User does not exist, proceed with registration
                                                                saveUserToFirestore(user);
                                                            }
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Error checking mobile!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Error checking email!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                }

            }

            private void saveUserToFirestore(User user) {
                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("user")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(RegisterActivity.this, "Registered Successfully.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this,SignInActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

        });

    }
}

class CountryAdapter extends ArrayAdapter<CountryData>{

    List<CountryData> countryDataList;
    int layout;

    public CountryAdapter(@NonNull Context context, int resource, @NonNull List<CountryData> objects) {
        super(context, resource, objects);
        countryDataList = objects;
        layout = resource;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layout,parent,false);
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textView = view.findViewById(R.id.textView13);

        CountryData countryData = countryDataList.get(position);
        imageView.setImageResource(countryData.getFlagResourceId());
        textView.setText(countryData.getName());

        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getDropDownView(position,convertView,parent);
    }
}