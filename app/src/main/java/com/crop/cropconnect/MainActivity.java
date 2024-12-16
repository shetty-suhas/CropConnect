package com.crop.cropconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ViewFlipper textFlipper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in, redirect to dashboard
            startActivity(new Intent(MainActivity.this, dashboard.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        MaterialButton loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, login.class));
            }
        });

        textFlipper = findViewById(R.id.textFlipper);

        // Set up ViewFlipper animations
        textFlipper.setInAnimation(this, R.anim.slide_in_right);
        textFlipper.setOutAnimation(this, R.anim.slide_out_left);

        // Start auto flipping
        textFlipper.setFlipInterval(3000); // Change text every 3 seconds
        textFlipper.startFlipping();
    }



}