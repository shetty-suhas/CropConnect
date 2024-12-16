package com.crop.cropconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        MaterialButton loginButton = findViewById(R.id.loginButton);
        MaterialButton newUserButton = findViewById(R.id.newUserButton);
        MaterialButton forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(v -> attemptLogin());

        newUserButton.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        forgotPasswordButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(login.this,
                        "Please enter your email address",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
//
    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.loginButton).setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);
                    findViewById(R.id.loginButton).setEnabled(true);

                    if (task.isSuccessful()) {
                        // Login success
                        Intent intent = new Intent(login.this, dashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Login failed
                        Toast.makeText(login.this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(login.this,
                                "Password reset email sent",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(login.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}