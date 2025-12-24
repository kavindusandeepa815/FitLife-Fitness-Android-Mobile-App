package com.example.fitlife.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.BlockActivity;
import com.example.fitlife.HomeActivity;
import com.example.fitlife.R;
import com.example.fitlife.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
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

import model.User;
import model.Workout;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final int BRIGHTNESS_THRESHOLD = 10;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // final TextView textView = binding.textHome;
        // homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        SharedPreferences sp = requireContext().getSharedPreferences(
                "com.example.fitlife.data",
                Context.MODE_PRIVATE
        );

        String userJson = sp.getString("user", null);

        Gson gson = new Gson();
        User user = gson.fromJson(userJson, User.class);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("user")
                .whereEqualTo("email", user.getEmail()) // Search for the user's document by email
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            // Get the first document that matches the query
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);

                            if (document.getString("status").equals("Inactive")) {
                                Intent intent = new Intent(getContext(), BlockActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                                startActivity(intent);

                            } else {
                                // Extract the "name" field from the document
                                String userName = document.getString("name");

                                // Find the TextView and set the user's name
                                TextView textView33 = root.findViewById(R.id.textView33);
                                textView33.setText(userName);
                            }

                        } else {
                            // Handle case where no document is found or error occurs
                            Log.e("Firestore", "Error finding user document", task.getException());
                        }
                    }
                });

        Spinner spinner2 = root.findViewById(R.id.spinner2);

        ArrayList<String> workouts = new ArrayList<>();
        workouts.add("Weight Loss");
        workouts.add("Muscle Building");
        workouts.add("General Health");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                workouts
        );

        spinner2.setAdapter(arrayAdapter);

//        ArrayList<Workout> workoutArrayList = new ArrayList<>();
//        // Adding Workout objects to the list
//        workoutArrayList.add(new Workout("Push-ups", "https://example.com/push-ups"));
//        workoutArrayList.add(new Workout("Squats", "https://example.com/squats"));
//        workoutArrayList.add(new Workout("Plank", "https://example.com/plank"));

//        String jsonData = "[{\"title\":\"Push-ups\",\"uri\":\"https://youtu.be/cvEJ5WFk2KE?si=ivNgMgHYXG9yG46v\"}," +
//                "{\"title\":\"Squats\",\"uri\":\"https://youtu.be/n4Vy-ac7Kok?si=7hAwgNvvU-_CdLqQ\"}," +
//                "{\"title\":\"Plank\",\"uri\":\"https://example.com/plank\"}]";
//
//        Gson gson1 = new Gson();
//        Type type = new TypeToken<ArrayList<Workout>>(){}.getType();
//        ArrayList<Workout> workoutArrayList = gson1.fromJson(jsonData,type);
//
//        RecyclerView recyclerView1 = root.findViewById(R.id.recyclerView1);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        recyclerView1.setLayoutManager(linearLayoutManager);
//        recyclerView1.setAdapter(new WorkoutAdapter(workoutArrayList));


        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWorkout = parent.getItemAtPosition(position).toString();

                if (selectedWorkout.equals("Weight Loss")) {

                    firestore.collection("weightLoss")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        JSONArray jsonArray = new JSONArray();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            JSONObject jsonObject = new JSONObject();
                                            try {
                                                jsonObject.put("title", document.getString("title"));
                                                jsonObject.put("uri", document.getString("uri"));
                                                jsonArray.put(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        // Convert JSONArray to String
                                        String weightLossData = jsonArray.toString();

                                        Gson gson1 = new Gson();
                                        Type type = new TypeToken<ArrayList<Workout>>() {
                                        }.getType();
                                        ArrayList<Workout> workoutArrayList = gson1.fromJson(weightLossData, type);

                                        RecyclerView recyclerView1 = root.findViewById(R.id.recyclerView1);
                                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                        recyclerView1.setLayoutManager(linearLayoutManager);
                                        recyclerView1.setAdapter(new WorkoutAdapter(workoutArrayList));

                                    } else {
                                        Log.e("FirestoreError", "Error getting documents", task.getException());
                                    }
                                }
                            });

                } else if (selectedWorkout.equals("Muscle Building")) {

                    firestore.collection("muscleBuilding")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        JSONArray jsonArray = new JSONArray();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            JSONObject jsonObject = new JSONObject();
                                            try {
                                                jsonObject.put("title", document.getString("title"));
                                                jsonObject.put("uri", document.getString("uri"));
                                                jsonArray.put(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        // Convert JSONArray to String
                                        String weightLossData = jsonArray.toString();

                                        Gson gson1 = new Gson();
                                        Type type = new TypeToken<ArrayList<Workout>>() {
                                        }.getType();
                                        ArrayList<Workout> workoutArrayList = gson1.fromJson(weightLossData, type);

                                        RecyclerView recyclerView1 = root.findViewById(R.id.recyclerView1);
                                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                        recyclerView1.setLayoutManager(linearLayoutManager);
                                        recyclerView1.setAdapter(new WorkoutAdapter(workoutArrayList));

                                    } else {
                                        Log.e("FirestoreError", "Error getting documents", task.getException());
                                    }
                                }
                            });

                } else if (selectedWorkout.equals("General Health")) {

                    firestore.collection("generalHealth")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        JSONArray jsonArray = new JSONArray();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            JSONObject jsonObject = new JSONObject();
                                            try {
                                                jsonObject.put("title", document.getString("title"));
                                                jsonObject.put("uri", document.getString("uri"));
                                                jsonArray.put(jsonObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        // Convert JSONArray to String
                                        String weightLossData = jsonArray.toString();

                                        Gson gson1 = new Gson();
                                        Type type = new TypeToken<ArrayList<Workout>>() {
                                        }.getType();
                                        ArrayList<Workout> workoutArrayList = gson1.fromJson(weightLossData, type);

                                        RecyclerView recyclerView1 = root.findViewById(R.id.recyclerView1);
                                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                                        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                        recyclerView1.setLayoutManager(linearLayoutManager);
                                        recyclerView1.setAdapter(new WorkoutAdapter(workoutArrayList));

                                    } else {
                                        Log.e("FirestoreError", "Error getting documents", task.getException());
                                    }
                                }
                            });

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle case where nothing is selected
            }
        });

        checkBrightnessLevel();

        return root;
    }

    private void checkBrightnessLevel() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        try {
            int brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);

            if (brightness > BRIGHTNESS_THRESHOLD) {
                showBrightnessAlert();
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showBrightnessAlert() {
        new AlertDialog.Builder(getContext())
                .setTitle("Brightness Level is High")
                .setMessage("Do you want to lower the brightness level?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lowerBrightness();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing, user chose not to lower brightness
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void lowerBrightness() {
        if (Settings.System.canWrite(getContext())) {
            ContentResolver contentResolver = getActivity().getContentResolver();
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_THRESHOLD);

            // Apply the brightness change to the current window
            WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
            layoutParams.screenBrightness = BRIGHTNESS_THRESHOLD / 255.0f; // Brightness value is between 0 and 1
            getActivity().getWindow().setAttributes(layoutParams);

        } else {
            // Request permission from the user
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getContext().getPackageName()));
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Settings.System.canWrite(getContext())) {
            lowerBrightness();
        }
    }



    class WorkoutAdapter extends RecyclerView.Adapter<WorkoutViewHolder> {

        ArrayList<Workout> workoutArrayList;

        public WorkoutAdapter(ArrayList<Workout> workoutArrayList) {
            this.workoutArrayList = workoutArrayList;
        }

        @NonNull
        @Override
        public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View workoutView = layoutInflater.inflate(R.layout.workout_item, parent, false);
            WorkoutViewHolder workoutViewHolder = new WorkoutViewHolder(workoutView);
            return workoutViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
            Workout workout = workoutArrayList.get(position);
            holder.textViewTitle.setText(String.valueOf(workout.getTitle()));
            holder.buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(workout.getUri());
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return workoutArrayList.size();
        }
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;

        Button buttonView;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textView21);
            buttonView = itemView.findViewById(R.id.button7);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}