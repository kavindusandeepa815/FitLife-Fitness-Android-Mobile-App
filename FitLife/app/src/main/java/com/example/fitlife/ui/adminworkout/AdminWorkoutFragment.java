package com.example.fitlife.ui.adminworkout;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.fitlife.R;
import com.example.fitlife.databinding.FragmentAdminWorkoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import model.Product;
import model.Workout;

public class AdminWorkoutFragment extends Fragment {

    private FragmentAdminWorkoutBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdminWorkoutBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        Spinner spinner4  = root.findViewById(R.id.spinner4);

        ArrayList<String> workouts = new ArrayList<>();
        workouts.add("Weight Loss");
        workouts.add("Muscle Building");
        workouts.add("General Health");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                workouts
        );

        spinner4.setAdapter(arrayAdapter);

        Button button13 = root.findViewById(R.id.button13);
        button13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText title = root.findViewById(R.id.editTextText4);
                EditText url = root.findViewById(R.id.editTextText5);

                String workoutSelect;

                workoutSelect = spinner4.getSelectedItem().toString();

                if(workoutSelect.equals("Weight Loss")){
                    workoutSelect = "weightLoss";
                } else if(workoutSelect.equals("Muscle Building")){
                    workoutSelect = "muscleBuilding";
                } else if(workoutSelect.equals("General Health")){
                    workoutSelect = "generalHealth";
                }

                if(title.getText().toString().isEmpty()){
                    Toast.makeText(requireContext(), "Please add title.", Toast.LENGTH_SHORT).show();
                } else if(url.getText().toString().isEmpty()) {
                    Toast.makeText(requireContext(), "Please add url.", Toast.LENGTH_SHORT).show();
                } else {

                    Workout workout = new Workout(
                            title.getText().toString(),
                            url.getText().toString()
                    );

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    firestore.collection(workoutSelect)
                            .add(workout)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(requireContext(), "Workout add successfully.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                                }
                            });

                }

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}