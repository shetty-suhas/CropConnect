package com.crop.cropconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Start MainActivity after animation ends
                Intent intent = new Intent(splash.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        animatorSet.start();
    }

}