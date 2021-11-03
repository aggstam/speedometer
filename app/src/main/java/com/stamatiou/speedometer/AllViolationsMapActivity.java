// -------------------------------------------------------------
//
// This Activity is used to monitor violations of all users, in a live map.
// When a new violation occurs, a new Marker is created in the map.
// Markers contain only violation's timestamp and speed.
// Map starting position is based on user's last known position,
// if Location permissions have been granted.
// Network permissions are required.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.speedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.violation.Violation;

import java.text.SimpleDateFormat;

public class AllViolationsMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private final static int REQ_CODE = 765;
    private Boolean locationPermissionGranted;
    private GoogleMap violationsMap;
    private DatabaseReference violationsReference;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_violations_map);
        allViolationsMapInit();
    }

    // Activity initialization method.
    // Application checks Network availability and informs user of its status.
    // Google Map fragment is initialized.
    // If user has granted Location permissions, map camera is moved to user's
    // last known location.
    private void allViolationsMapInit() {
        Log.i("message","AllViolationsMapInit method started.");
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null || cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
                Toast.makeText(this, R.string.internet_provider_disabled, Toast.LENGTH_LONG).show();
            }
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            checkLocationPermission();
            if (locationPermissionGranted) {
                LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && violationsMap != null) {
                            LatLng lastKnownLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            violationsMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, 10.0f));
                        }
                    }
                });
            }

            Log.i("message","AllViolationsMapInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during AllViolationsMapInit method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Google Map fragment post initialization method.
    // Firebase value event listener for violations is created.
    // Map markers are created in a live manner.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("message","OnMapReady method started.");
        try {
            violationsMap = googleMap;
            violationsReference = FirebaseDatabase.getInstance().getReference("violations");
            violationsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    violationsMap.clear();
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        for (DataSnapshot userViolation : user.getChildren()) {
                            Violation violation = userViolation.getValue(Violation.class);
                            LatLng violationLocation = new LatLng(violation.getLatitude(), violation.getLongitude());
                            violationsMap.addMarker(new MarkerOptions().position(violationLocation)
                                                                       .title(dateFormatter.format(violation.getTimestamp()))
                                                                       .snippet("Speed: " + String.format("%.2f", violation.getSpeed()) + " km/h"));

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve user violations. Error: " + databaseError.toException());
                    violationsMap.clear();
                    Toast.makeText(getApplicationContext(), "Failed to retrieve user violations, check log file for more information.", Toast.LENGTH_SHORT).show();
                }
            });
            Log.i("message","OnMapReady method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnMapReady method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Check location permissions.
    // If permissions are not granted, application requests them.
    private void checkLocationPermission() {
        Log.i("message","CheckLocationPermission method started.");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_CODE);
            } else {
                locationPermissionGranted = true;
            }
            Log.i("message","CheckLocationPermission method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during CheckLocationPermission method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("message","OnRequestPermissionsResult method started.");
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                checkLocationPermission();
            } else {
                locationPermissionGranted = true;
            }
            Log.i("message","OnRequestPermissionsResult method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnRequestPermissionsResult method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }
}