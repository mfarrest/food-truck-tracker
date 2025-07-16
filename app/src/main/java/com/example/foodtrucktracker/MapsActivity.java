package com.example.foodtrucktracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.TextView;

import com.example.foodtrucktracker.models.FoodTruck;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference trucksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Realtime DB reference
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://food-truck-tracker-b8f75-default-rtdb.asia-southeast1.firebasedatabase.app/");
        trucksRef = database.getReference("foodtrucks");


        // Load map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable zoom
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Default location (UiTM Shah Alam)
        LatLng defaultLocation = new LatLng(3.0738, 101.5039);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));

        // Load trucks from Firebase
        loadFoodTrucksFromFirebase();

        // Long click to add a food truck
        mMap.setOnMapLongClickListener(latLng -> {
            String type = "New Truck";
            String reporter = "Anonymous";
            String timestamp = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            FoodTruck truck = new FoodTruck(type, latLng.latitude, latLng.longitude, reporter, timestamp);
            trucksRef.push().setValue(truck).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Food Truck added!", Toast.LENGTH_SHORT).show();
            });
        });

        // Show more details in a bottom sheet when info window is tapped
        mMap.setOnInfoWindowClickListener(marker -> {
            FoodTruck truck = (FoodTruck) marker.getTag();
            if (truck != null) {
                showTruckDetailsBottomSheet(truck);
            }
        });
    }

    private void loadFoodTrucksFromFirebase() {
        trucksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear(); // Clear previous markers

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                int count = 0;

                for (DataSnapshot truckSnap : snapshot.getChildren()) {
                    FoodTruck truck = truckSnap.getValue(FoodTruck.class);
                    if (truck != null) {
                        LatLng pos = new LatLng(truck.latitude, truck.longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(truck.type)
                                .snippet("Reported by " + truck.reporter + " at " + truck.timestamp));
                        if (marker != null) {
                            marker.setTag(truck); // Store the truck object for later retrieval
                        }
                        builder.include(pos);
                        Log.d("FIREBASE", "Loaded: " + truck.type + " at " + truck.latitude + "," + truck.longitude);
                        count++;
                    }
                }

                if (count > 0) {
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // space around markers
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }

                Log.d("FIREBASE", "Total loaded: " + count);
                Toast.makeText(MapsActivity.this, "Loaded " + count + " food trucks", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                Log.e("FIREBASE", "Database error: " + error.getMessage());
            }
        });
    }

    // Show a bottom sheet with full food truck details
    private void showTruckDetailsBottomSheet(FoodTruck truck) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_food_truck, null);

        TextView tvType = view.findViewById(R.id.tvType);
        TextView tvReporter = view.findViewById(R.id.tvReporter);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvLocation = view.findViewById(R.id.tvLocation);

        tvType.setText(truck.type);
        tvReporter.setText("Reported by: " + truck.reporter);
        tvTime.setText("Time: " + truck.timestamp);
        tvLocation.setText("Location: " + truck.latitude + ", " + truck.longitude);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }


}
