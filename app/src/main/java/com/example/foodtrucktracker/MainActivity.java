package com.example.foodtrucktracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up Google Map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup Firebase
        databaseRef = FirebaseDatabase.getInstance("https://food-truck-tracker-b8f75-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("foodtrucks");

        // SearchView logic
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTruckByType(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

// ✅ Make typed text in SearchView black
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = searchView.findViewById(id);
        if (searchText != null) {
            searchText.setTextColor(getResources().getColor(android.R.color.black));
            searchText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        // FloatingActionButton logic
        com.google.android.material.floatingactionbutton.FloatingActionButton fabReport =
                findViewById(R.id.fabReport);
        fabReport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // About button logic
        Button aboutButton = findViewById(R.id.btnAbout);
        aboutButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng uitm = new LatLng(3.0738, 101.5035);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(uitm, 15));
        loadAllFoodTrucks();

        // Show more details in a bottom sheet when marker is tapped
        mMap.setOnMarkerClickListener(marker -> {
            FoodTruck truck = (FoodTruck) marker.getTag();
            if (truck != null) {
                showTruckDetailsBottomSheet(truck);
                return true; // consume the event, don't show default info window
            }
            return false;
        });
    }

    private void loadAllFoodTrucks() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                for (DataSnapshot truckSnap : snapshot.getChildren()) {
                    String type = truckSnap.child("type").getValue(String.class);
                    Double lat = truckSnap.child("latitude").getValue(Double.class);
                    Double lng = truckSnap.child("longitude").getValue(Double.class);
                    String reporter = truckSnap.child("reporter").getValue(String.class);
                    String timestamp = truckSnap.child("timestamp").getValue(String.class);

                    if (lat != null && lng != null) {
                        LatLng location = new LatLng(lat, lng);
                        FoodTruck truck = new FoodTruck(type, lat, lng, reporter, timestamp);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(location)
                                .title(type)
                                .snippet("Reported by: " + reporter + "\n" + timestamp));
                        if (marker != null) {
                            marker.setTag(truck);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchTruckByType(String query) {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                boolean found = false;

                for (DataSnapshot truckSnap : snapshot.getChildren()) {
                    String type = truckSnap.child("type").getValue(String.class);
                    if (type != null && type.equalsIgnoreCase(query)) {
                        Double lat = truckSnap.child("latitude").getValue(Double.class);
                        Double lng = truckSnap.child("longitude").getValue(Double.class);
                        String reporter = truckSnap.child("reporter").getValue(String.class);
                        String timestamp = truckSnap.child("timestamp").getValue(String.class);

                        if (lat != null && lng != null) {
                            LatLng location = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(type)
                                    .snippet("Reported by: " + reporter + "\n" + timestamp));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    Toast.makeText(MainActivity.this, "No food truck found for type: " + query, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Search error", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
