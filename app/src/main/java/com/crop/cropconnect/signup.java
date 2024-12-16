package com.crop.cropconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class signup extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton signupButton;
    private View progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.progressBar);
        MaterialButton alreadyHaveAccountButton = findViewById(R.id.alreadyHaveAccountButton);

        // Set click listeners
        signupButton.setOnClickListener(v -> validateAndSignup());

        alreadyHaveAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(signup.this, login.class);
            startActivity(intent);
            finish();
        });

        // Add focus change listeners to clear errors
        nameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                nameLayout.setError(null);
            }
        });

        emailInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                emailLayout.setError(null);
            }
        });

        passwordInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                passwordLayout.setError(null);
            }
        });
    }

    private void validateAndSignup() {
        // Clear previous errors
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate name
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name is required");
            return;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            return;
        }

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        // Create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Update profile
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    progressBar.setVisibility(View.GONE);
                                    signupButton.setEnabled(true);

                                    if (profileTask.isSuccessful()) {
                                        // Navigate to dashboard
                                        Intent intent = new Intent(signup.this, dashboard.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        emailLayout.setError("Registration failed: " + task.getException().getMessage());
                    }
                });
    }
}