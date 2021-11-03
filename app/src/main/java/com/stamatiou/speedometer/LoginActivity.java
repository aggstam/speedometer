// -------------------------------------------------------------
//
// This is the main Activity, used to authenticate users before
// using the application.
//
// Author: Aggelos Stamatiou, July 2020
//
// --------------------------------------------------------------

package com.stamatiou.speedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginInit();
    }

    // Activity initialization method.
    // If user is already authenticated, user is redirected to SpeedometerActivity.
    private void loginInit() {
        Log.i("message","LoginInit method started.");
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                Log.i("message","User is already logged in.");
                ((EditText) findViewById(R.id.emailEditText)).setText(user.getEmail());
                Intent intent = new Intent(this, SpeedometerActivity.class);
                startActivity(intent);
            }
            findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginAction();
                }
            });
            findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    signUpAction();
                }
            });
            findViewById(R.id.passwordResetButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    passwordResetAction();
                }
            });
            Log.i("message","LoginInit method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during LoginInit method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // User Firebase login method.
    // On successful login, user is redirected to SpeedometerActivity.
    private void loginAction() {
        Log.i("message","LoginAction method started.");
        try {
            if (checkNetworkProvider() && validateFields()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(((EditText) findViewById(R.id.emailEditText)).getText().toString(), ((EditText) findViewById(R.id.passwordEditText)).getText().toString())
                                          .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                              @Override
                                              public void onComplete(@NonNull Task<AuthResult> task) {
                                                  if (task.isSuccessful()){
                                                      Log.i("message","Login action was successful!");
                                                      if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                                                          Toast.makeText(getApplicationContext(), "You have been successfully logged in!", Toast.LENGTH_SHORT).show();
                                                          Intent intent = new Intent(getApplicationContext(), SpeedometerActivity.class);
                                                          startActivity(intent);
                                                      } else {
                                                          Toast.makeText(getApplicationContext(), "Account has not been verified! Please check your emails!", Toast.LENGTH_SHORT).show();
                                                      }
                                                  } else {
                                                      Log.i("message","Login action was unsuccessful. Error:" + task.getException().getMessage());
                                                      Toast.makeText(getApplicationContext(), "Login was unsuccessful. Error message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                                          });
            }

            Log.i("message","LoginAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during LoginAction method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // New Firebase user creation.
    // New users must verify their email address to validate their account.
    private void signUpAction() {
        Log.i("message","SignUpAction method started.");
        try {
            if (checkNetworkProvider() && validateFields()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(((EditText) findViewById(R.id.emailEditText)).getText().toString(), ((EditText) findViewById(R.id.passwordEditText)).getText().toString())
                                          .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                              @Override
                                              public void onComplete(@NonNull Task<AuthResult> task) {
                                                  if (task.isSuccessful()){
                                                      Log.i("message","Sign up action was successful!");
                                                      FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                                                      Toast.makeText(getApplicationContext(), "You have been successfully sign up! Please check your emails to verify your account!", Toast.LENGTH_SHORT).show();
                                                  } else {
                                                      Log.i("message","Sign up action was unsuccessful. Error:" + task.getException().getMessage());
                                                      Toast.makeText(getApplicationContext(), "Sign up was unsuccessful. Error message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                  }
                                              }
                                          });
            }
            Log.i("message","SignUpAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during SignUpAction method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Firebase user password reset.
    // An email is sent to user's mail address with instructions to change their password.
    private void passwordResetAction() {
        Log.i("message","PasswordResetAction method started.");
        try {
            EditText emailEditText = findViewById(R.id.emailEditText);
            if (emailEditText.getText().toString().trim().length() == 0) {
                emailEditText.setError("Email cannot be empty!");
            } else if (checkNetworkProvider()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.getText().toString());
                Toast.makeText(getApplicationContext(), "Password reset email has been sent! Please check your emails!", Toast.LENGTH_SHORT).show();
            }
            Log.i("message","PasswordResetAction method completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("message","Exception during PasswordResetAction method:" + e.getMessage());
            Toast.makeText(this, "Exception occurred, check log file for more information.", Toast.LENGTH_SHORT).show();
        }
    }

    // Validates user's submitted email and password.
    // Fields cannot be empty.
    private Boolean validateFields() {
        Boolean valid = true;
        EditText emailEditText = findViewById(R.id.emailEditText);
        if (emailEditText.getText().toString().trim().length() == 0) {
            emailEditText.setError("Email cannot be empty!");
            valid = false;
        }
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        if (passwordEditText.getText().toString().trim().length() == 0) {
            passwordEditText.setError("Password cannot be empty!");
            valid = false;
        }
        return valid;
    }

    // Check network provider availability.
    // On disabled network, user is informed with a toast message.
    private Boolean checkNetworkProvider() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null || cm.getActiveNetworkInfo() == null || !cm.getActiveNetworkInfo().isConnected()) {
            Toast.makeText(this, R.string.internet_provider_disabled, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // On activity restart, user is unauthenticated and has to re-login.
    @Override
    protected void onRestart() {
        super.onRestart();
        ((EditText) findViewById(R.id.passwordEditText)).getText().clear();
        FirebaseAuth.getInstance().signOut();
    }
}