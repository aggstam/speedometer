// -------------------------------------------------------------
//
// This Activity is used to monitor user's violations list.
// When a new violation occurs, list is refreshed.
// Network permissions are required.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.speedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stamatiou.violation.Violation;
import com.stamatiou.violation.ViolationAdapter;
import com.treebo.internetavailabilitychecker.InternetAvailabilityChecker;
import com.treebo.internetavailabilitychecker.InternetConnectivityListener;

import java.util.ArrayList;
import java.util.List;

public class UserViolationsListActivity extends AppCompatActivity implements InternetConnectivityListener {

    private DatabaseReference userViolationsReference;
    private List<Violation> violations;
    private ViolationAdapter violationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_violations_list);
        InternetAvailabilityChecker.init(this);
        InternetAvailabilityChecker.getInstance().addInternetConnectivityListener(this);
    }

    // User Violations List initialization method.
    // Firebase value event listener for the user's violations is created.
    // Violations list is refreshed in a live manner.
    private void userViolationsListInit() {
        Log.i("message","UserViolationsListInit method started.");
        try {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            violations = new ArrayList<>();
            violationsAdapter = new ViolationAdapter(violations);
            recyclerView.setAdapter(violationsAdapter);

            userViolationsReference = FirebaseDatabase.getInstance().getReference("violations/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            userViolationsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    violations.clear();
                    for (DataSnapshot violation : dataSnapshot.getChildren()) {
                        violations.add(0, violation.getValue(Violation.class));
                    }
                    refreshViolations();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("message", "Failed to retrieve user violations. Error: " + databaseError.toException());
                    violations.clear();
                    refreshViolations();
                    Toast.makeText(getApplicationContext(), "Failed to retrieve user violations, check log file for more information.", Toast.LENGTH_SHORT).show();
                }
            });
            Log.i("message","UserViolationsListInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during UserViolationsListInit method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Application listens to internet connectivity status.
    // When internet provider is disabled, user is informed via a message box.
    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        Log.i("message","OnInternetConnectivityChanged method started.");
        try {
            if (isConnected) {
                findViewById(R.id.recyclerView).setVisibility(View.VISIBLE);
                userViolationsListInit();
            } else {
                findViewById(R.id.recyclerView).setVisibility(View.INVISIBLE);
                ((TextView) findViewById(R.id.titleView)).setText(R.string.internet_provider_disabled);
            }
            Log.i("message","OnInternetConnectivityChanged method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during OnInternetConnectivityChanged method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Refreshes Activity displayed list.
    private void refreshViolations() {
        if (!violations.isEmpty()) {
            ((TextView) findViewById(R.id.titleView)).setText("Violations (" + violations.size() + "): ");
        } else {
            ((TextView) findViewById(R.id.titleView)).setText(R.string.no_violations_message);
        }
        violationsAdapter.notifyDataSetChanged();
    }
}