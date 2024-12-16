package com.crop.cropconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private View progressBar;
    private FirebaseAuth mAuth;
    private ImageView loginImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        MaterialButton newUserButton = findViewById(R.id.newUserButton);
        MaterialButton forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        loginImage = findViewById(R.id.loginImage);

        // Safely load the image
        try {
            loginImage.setImageResource(R.drawable.login);
        } catch (Exception e) {
            e.printStackTrace();
            loginImage.setVisibility(View.GONE); // Hide the ImageView if loading fails
        }

        // Set click listeners
        loginButton.setOnClickListener(v -> validateAndLogin());

        newUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        forgotPasswordButton.setOnClickListener(v -> handleForgotPassword());

        // Add text change listeners to clear errors
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

    private void validateAndLogin() {
        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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

        // Show progress and disable button
        progressBar.setVisibility(View.VISIBLE);
        loginButton.setEnabled(false);

        // Attempt login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    loginButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        // Login success
                        Intent intent = new Intent(login.this, dashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        emailLayout.setError("Invalid email or password");
                        passwordLayout.setError(" ");  // Space to show red outline
                    }
                });
    }

    private void handleForgotPassword() {
        String email = emailInput.getText().toString().trim();
        emailLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Please enter your email address");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email address");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        emailLayout.setHelperText("Password reset email sent");
                    } else {
                        emailLayout.setError("Failed to send reset email");
                    }
                });
    }
}