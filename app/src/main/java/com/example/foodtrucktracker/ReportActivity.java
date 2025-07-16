package com.example.foodtrucktracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.appbar.MaterialToolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    private EditText editType, editLatitude, editLongitude, editReporter;
    private Button btnSubmit;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Set up the MaterialToolbar as the app bar with back navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        editType = findViewById(R.id.editType);
        editLatitude = findViewById(R.id.editLatitude);
        editLongitude = findViewById(R.id.editLongitude);
        editReporter = findViewById(R.id.editReporter);
        btnSubmit = findViewById(R.id.btnSubmit);

        databaseRef = FirebaseDatabase.getInstance("https://food-truck-tracker-b8f75-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("foodtrucks");

        btnSubmit.setOnClickListener(v -> {
            String type = editType.getText().toString().trim();
            String latStr = editLatitude.getText().toString().trim();
            String lngStr = editLongitude.getText().toString().trim();
            String reporter = editReporter.getText().toString().trim();

            if (TextUtils.isEmpty(type) || TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lngStr) || TextUtils.isEmpty(reporter)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double latitude = Double.parseDouble(latStr);
                double longitude = Double.parseDouble(lngStr);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                // Create unique key
                String id = databaseRef.push().getKey();

                // Create food truck report object
                FoodTruck truck = new FoodTruck(type, latitude, longitude, reporter, timestamp);

                if (id != null) {
                    databaseRef.child(id).setValue(truck)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Food truck reported successfully!", Toast.LENGTH_SHORT).show();
                                finish(); // Close activity
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid coordinates", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
