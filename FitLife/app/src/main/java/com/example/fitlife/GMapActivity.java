package com.example.fitlife;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GMapActivity extends AppCompatActivity {

    private GoogleMap googleMap;
    private String city;
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gmap);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get city and country from the intent
        Intent intent = getIntent();
        city = intent.getStringExtra("city");
        country = intent.getStringExtra("country");

        // Initialize map fragment
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayout1, supportMapFragment);
        fragmentTransaction.commit();

        // Load map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap map) {
                googleMap = map;

                // Load the location based on city and country
                if (city != null && country != null) {
                    loadLocation(city, country);
                } else {
                    // Default location (Colombo, Sri Lanka)
                    LatLng defaultLocation = new LatLng(6.9271, 79.8612);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 18));
                    googleMap.addMarker(
                            new MarkerOptions()
                                    .position(defaultLocation)
                                    .title("Delivery Location")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.giftbox))
                    );
                }
            }
        });
    }

    // Load location using Geocoder
    private void loadLocation(String city, String country) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Combine city and country into a single query
            String query = city + ", " + country;

            // Get list of addresses matching the query
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Move camera to the location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

                // Add a marker for the location
                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title(query)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.giftbox))
                ).showInfoWindow();
            } else {
                Toast.makeText(this, "Location not found: " + query, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoding failed", Toast.LENGTH_SHORT).show();
        }
    }
}