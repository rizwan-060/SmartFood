package com.example.smartfood;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide ActionBar for full splash screen effect
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Animate Logo Container (Spring effect replica)
        View logoContainer = findViewById(R.id.logoContainer);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);
        PropertyValuesHolder rotation = PropertyValuesHolder.ofFloat(View.ROTATION, -180f, 0f);
        ObjectAnimator logoAnim = ObjectAnimator.ofPropertyValuesHolder(logoContainer, scaleX, scaleY, rotation);
        logoAnim.setDuration(800);
        logoAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        logoAnim.start();

        // Animate Floating Ingredients
        int[] emojiIds = {R.id.emoji1, R.id.emoji2, R.id.emoji3, R.id.emoji4};
        for (int i = 0; i < emojiIds.length; i++) {
            View emoji = findViewById(emojiIds[i]);
            ObjectAnimator floatAnim = ObjectAnimator.ofFloat(emoji, "translationY", 0f, -30f, 0f);
            floatAnim.setDuration(3000);
            floatAnim.setRepeatCount(ObjectAnimator.INFINITE);
            floatAnim.setStartDelay(i * 200L); // Stagger the animations
            floatAnim.start();
        }

        // Animate Dots
        int[] dotIds = {R.id.dot1, R.id.dot2, R.id.dot3};
        for (int i = 0; i < dotIds.length; i++) {
            View dot = findViewById(dotIds[i]);
            ObjectAnimator dotAnim = ObjectAnimator.ofFloat(dot, "alpha", 0.5f, 1f, 0.5f);
            dotAnim.setDuration(1000);
            dotAnim.setRepeatCount(ObjectAnimator.INFINITE);
            dotAnim.setStartDelay(i * 200L);
            dotAnim.start();
        }

        // Navigate to Login after 3 Seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Ensure you have created a LoginActivity class
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Prevent user from pressing back button to return to splash
            }
        }, 3000);
    }
}
