// -------------------------------------------------------------
//
// This Activity is used to monitor user's current speed and report violations.
// Application uses two parameters to determine a speed violation:
//      1. Speed limit: speed limit value, provided by the Firebase
//      2. Warning limit: 10% greater than speed limit value, used to inform
//      users that they have exceeded the limit and a violation occurs.
// If user's speed surpasses the warning limit, a violation record is created.
// User can navigate to rest application activities using the top right menu.
// Location permissions are required.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.speedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.violation.Violation;

import java.util.Date;

public class SpeedometerActivity extends AppCompatActivity implements LocationListener {

    private final static int REQ_CODE = 765;
    private Float speedLimit;
    private Float warningSpeed;
    private Location speedLimitExceedingLocation;
    private DatabaseReference userViolationsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer);
        speedometerInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.speedometer_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;
        if (id == R.id.userViolations) {
            intent = new Intent(this, UserViolationsListActivity.class);
        } else if (id == R.id.allViolations) {
            intent = new Intent(this, AllViolationsMapActivity.class);
        } else {
            finish();
        }
        if (intent != null) {
            startActivity(intent);
        }
        return true;
    }

    // Activity initialization method.
    // Firebase value event listeners for the speed limit is created.
    // User's violations Firebase reference is initialized.
    // Application checks appropriate location permissions.
    private void speedometerInit() {
        Log.i("message","SpeedometerInit method started.");
        try {
            speedLimit = Float.parseFloat("60.00"); // Default speed limit value.
            warningSpeed = speedLimit * Float.parseFloat("1.10"); // Default warning speed value.
            DatabaseReference speedLimitRef = FirebaseDatabase.getInstance().getReference("configuration/speed_limit");
            speedLimitRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        speedLimit = Float.parseFloat(dataSnapshot.getValue().toString());
                        warningSpeed = speedLimit * Float.parseFloat("1.10");
                    }
                    Log.i("message", "Speed limit value set to: " + String.format("%.2f", speedLimit) + " km/h");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve speed limit value. Error: " + databaseError.toException());
                    Toast.makeText(getApplicationContext(), "Failed to retrieve speed limit value, check log file for more information.", Toast.LENGTH_SHORT).show();
                }
            });
            userViolationsReference = FirebaseDatabase.getInstance().getReference("violations/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            checkLocationPermission();
            Log.i("message","SpeedometerInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during SpeedometerInit method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Check location permissions.
    // If permissions are not granted, application requests them.
    // User is informed on the permissions status via a message box.
    private void checkLocationPermission() {
        Log.i("message","CheckLocationPermission method started.");
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_CODE);
            } else {
                ((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
                ((TextView) findViewById(R.id.messageView)).setText(R.string.permission_not_granted);
                checkLocationPermission();
            } else {
                ((LocationManager) getSystemService(LOCATION_SERVICE)).requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
            Log.i("message","OnRequestPermissionsResult method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnRequestPermissionsResult method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // On location changed, user's speed is calculated in km/h.
    // Speed text color changes and an appropriate message appears, depending on user's speed.
    // When user's speed is greater than the warning limit, a violation is recorded.
    @Override
    public void onLocationChanged(Location location) {
        //Log.i("message","OnLocationChanged method started.");
        try {
            Float speed = (18 * location.getSpeed()) / 5;
            if (speed > warningSpeed) {
                if (speedLimitExceedingLocation == null) {
                    speedLimitExceedingLocation = location;
                    Violation violation = new Violation.Builder()
                                                        .withLatitude(speedLimitExceedingLocation.getLatitude())
                                                        .withLongitude(speedLimitExceedingLocation.getLongitude())
                                                        .withSpeed(speed)
                                                        .withTimestamp(new Date())
                                                        .build();
                    Log.i("message", "Speed limit exceeded! Violation data: " + violation.toString());
                    userViolationsReference.push().setValue(violation);
                }
                ((TextView) findViewById(R.id.speedView)).setTextColor(0xffcc0000);
                ((TextView) findViewById(R.id.messageView)).setTextColor(0xffcc0000);
                ((TextView) findViewById(R.id.messageView)).setText(R.string.speed_limit_exceeded);
            } else if (speed <= warningSpeed && speed > speedLimit) {
                speedLimitExceedingLocation = null;
                ((TextView) findViewById(R.id.speedView)).setTextColor(0xffff8800);
                ((TextView) findViewById(R.id.messageView)).setTextColor(0xffff8800);
                ((TextView) findViewById(R.id.messageView)).setText(R.string.speed_limit_warning);
            } else if (speed <= speedLimit) {
                speedLimitExceedingLocation = null;
                ((TextView) findViewById(R.id.speedView)).setTextColor(0xffaaaaaa);
                ((TextView) findViewById(R.id.messageView)).setTextColor(0xffff8800);
                ((TextView) findViewById(R.id.messageView)).setText(null);
            }
            ((TextView) findViewById(R.id.speedView)).setText(String.format("%.2f", speed) + " km/h");
            //Log.i("message","OnLocationChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnLocationChanged method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // On Location Provider status changed, speedometer is reset and user is informed via a message box.
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("message","OnStatusChanged method started.");
        try {
            speedLimitExceedingLocation = null;
            ((TextView) findViewById(R.id.speedView)).setText(R.string.zero_speed);
            ((TextView) findViewById(R.id.messageView)).setText(R.string.provider_status_changed);
            Log.i("message","OnStatusChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnStatusChanged method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // On Location provider enable, speedometer is reset.
    @Override
    public void onProviderEnabled(String provider) {
        Log.i("message","OnProviderEnabled method started.");
        try {
            speedLimitExceedingLocation = null;
            ((TextView) findViewById(R.id.speedView)).setText(R.string.zero_speed);
            ((TextView) findViewById(R.id.messageView)).setText(null);
            Log.i("message","OnProviderEnabled method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnProviderEnabled method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // On Location Provider disable, speedometer is reset and user is informed via a message box.
    @Override
    public void onProviderDisabled(String provider) {
        Log.i("message","OnProviderDisabled method started.");
        try {
            speedLimitExceedingLocation = null;
            ((TextView) findViewById(R.id.speedView)).setText(R.string.zero_speed);
            ((TextView) findViewById(R.id.messageView)).setText(R.string.provider_disabled);
            Log.i("message","OnProviderDisabled method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnProviderDisabled method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }
}