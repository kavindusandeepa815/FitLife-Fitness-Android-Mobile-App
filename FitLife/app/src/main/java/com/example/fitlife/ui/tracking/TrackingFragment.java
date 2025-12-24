package com.example.fitlife.ui.tracking;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fitlife.R;
import com.example.fitlife.databinding.FragmentTrackingBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import model.SQLiteHelper;
import model.StepData;

public class TrackingFragment extends Fragment {

    private FragmentTrackingBinding binding;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private SensorEventListener stepListener;
    private boolean isSensorAvailable;
    private int initialStepCount = -1; // Store initial step count

    SQLiteHelper sqLiteHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTrackingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Calendar calendar = Calendar.getInstance();
        String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.getTime());

        Log.i("DateInfo", "Today is: " + dayName);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.example.fitlife.data", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("tracking", null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<StepData>>() {}.getType();
        ArrayList<StepData> stepDataArrayList = gson.fromJson(json, type);

        if (stepDataArrayList != null && !stepDataArrayList.isEmpty()) {
            BarChart barChart1 = root.findViewById(R.id.barChat1);

            ArrayList<BarEntry> barEntryArrayList = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            // Loop through the list and add data points
            for (int i = 0; i < stepDataArrayList.size(); i++) {
                StepData stepData = stepDataArrayList.get(i);
                barEntryArrayList.add(new BarEntry(i, stepData.getStepCount()));
                labels.add(stepData.getDay()); // Store day labels
            }

            BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "Steps");

            // Define colors
            ArrayList<Integer> colorArrayList = new ArrayList<>();
            colorArrayList.add(getActivity().getColor(R.color.red));
            colorArrayList.add(getActivity().getColor(R.color.purple));
            colorArrayList.add(getActivity().getColor(R.color.orange));
            colorArrayList.add(getActivity().getColor(R.color.blue));
            colorArrayList.add(getActivity().getColor(R.color.green));
            colorArrayList.add(getActivity().getColor(R.color.gray));
            colorArrayList.add(getActivity().getColor(R.color.pink));

            barDataSet.setColors(colorArrayList);

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.5f);

            barChart1.setData(barData);
            barChart1.setFitBars(true);
            barChart1.setPinchZoom(false);
            barChart1.setScaleEnabled(false);
            barChart1.animateY(1000, Easing.EaseInCubic);
            barChart1.setDescription(null);

            // Configure X-axis labels
            XAxis xAxis = barChart1.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            xAxis.setGranularity(1f);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            // Refresh the chart
            barChart1.invalidate();
        } else {
            Log.e("StepTracker", "No step data found in SharedPreferences.");
        }


        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepCounterSensor != null) {
            isSensorAvailable = true;
            stepListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    int stepCount = (int) event.values[0];

                    if (initialStepCount == -1) {
                        initialStepCount = stepCount;
                    }

                    int stepsTaken = stepCount - initialStepCount;
                    binding.textView37.setText("Steps: " + stepsTaken);

                    Log.i("StepTracker", "Total Steps: " + stepCount + " | Session Steps: " + stepsTaken);

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.example.fitlife.data", Context.MODE_PRIVATE);
                    String json = sharedPreferences.getString("tracking", null);

                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<StepData>>() {}.getType();
                    ArrayList<StepData> stepDataArrayList = gson.fromJson(json, type);

                    if (stepDataArrayList == null) {
                        stepDataArrayList = new ArrayList<>();
                    }

                    boolean dayExists = false;

                    for (StepData stepData : stepDataArrayList) {
                        if (stepData.getDay().equals(dayName)) {
                            // **Add new steps instead of overwriting**
                            stepData.setStepCount(stepData.getStepCount() + stepsTaken);
                            dayExists = true;
                            break;
                        }
                    }

                    // If the day does not exist, add a new entry
                    if (!dayExists) {
                        stepDataArrayList.add(new StepData(dayName, stepsTaken));
                    }

                    String updatedDataJson = gson.toJson(stepDataArrayList);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("tracking", updatedDataJson);
                    editor.apply();
                }


                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // Not needed for basic step tracking
                }
            };

            sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            isSensorAvailable = false;
            binding.textView37.setText("Step counter sensor not available");
            Log.e("StepTracker", "Step counter sensor not found.");
        }

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isSensorAvailable) {
            sensorManager.unregisterListener(stepListener);
        }
        binding = null;
    }
}
