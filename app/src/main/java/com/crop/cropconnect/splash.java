package com.crop.cropconnect;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splash extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        TextView appNameText = findViewById(R.id.appNameText);
        appNameText.setTranslationY(1000f);

        // Create animation for moving up
        ObjectAnimator moveUpAnimator = ObjectAnimator.ofFloat(
                appNameText,
                "translationY",
                1000f,
                0f
        );
        moveUpAnimator.setDuration(1500);
        moveUpAnimator.setInterpolator(new DecelerateInterpolator());

        // Create animation for fading in
        ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(
                appNameText,
                "alpha",
                0f,
                1f
        );
        fadeAnimator.setDuration(1500);

        // Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveUpAnimator, fadeAnimator);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                // Check authentication after animation completes
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // User is signed in, go to dashboard
                    startActivity(new Intent(splash.this, dashboard.class));
                } else {
                    // No user signed in, go to MainActivity
                    startActivity(new Intent(splash.this, MainActivity.class));
                }
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();
    }

    // Remove onStart override as we don't want to check authentication until animation completes
}