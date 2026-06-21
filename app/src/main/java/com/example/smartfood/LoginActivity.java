package com.example.smartfood;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.smartfood.data.models.UserProfile;
import com.example.smartfood.data.repository.FirestoreRepository;

public class LoginActivity extends AppCompatActivity {

    private boolean isLogin = true;

    private TextView tvTitle, tvSubtitle, tvForgotPassword, tvTogglePrompt, tvToggleAction;
    private EditText etName, etEmail, etPassword; // Added etName here
    private Button btnSubmit, btnGoogle, btnApple;
    private View emojiPizza, emojiSalad, emojiNoodle;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupAnimations();
        setupListeners();
        updateUIState();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvTogglePrompt = findViewById(R.id.tvTogglePrompt);
        tvToggleAction = findViewById(R.id.tvToggleAction);

        etName = findViewById(R.id.etName); // Linked Name Field
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnSubmit = findViewById(R.id.btnSubmit);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnApple = findViewById(R.id.btnApple);

        emojiPizza = findViewById(R.id.emojiPizza);
        emojiSalad = findViewById(R.id.emojiSalad);
        emojiNoodle = findViewById(R.id.emojiNoodle);
    }

    private void setupAnimations() {
        animateEmoji(emojiPizza, 10f, 2000, 0);
        animateEmoji(emojiSalad, -10f, 2000, 200);
        animateEmoji(emojiNoodle, 10f, 2000, 400);
    }

    private void animateEmoji(View view, float angle, long duration, long delay) {
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, angle, -angle, 0f)
        );
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void setupListeners() {
        tvToggleAction.setOnClickListener(v -> {
            isLogin = !isLogin;
            updateUIState();
        });

        // Submit Button Click
        btnSubmit.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isLogin) {
                loginUser(email, password);
            } else {
                // Check if name is provided during sign up
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(name, email, password); // Pass the name to registerUser
            }
        });

        View.OnClickListener skipToMain = v -> {
            Toast.makeText(LoginActivity.this, "Social login coming soon!", Toast.LENGTH_SHORT).show();
        };
        btnGoogle.setOnClickListener(skipToMain);
        btnApple.setOnClickListener(skipToMain);
    }

    private void updateUIState() {
        if (isLogin) {
            tvTitle.setText("Welcome Back!");
            tvSubtitle.setText("Sign in to continue your food journey");
            tvForgotPassword.setVisibility(View.VISIBLE);
            etName.setVisibility(View.GONE); // Hide Name field during Login
            btnSubmit.setText("Sign In");
            tvTogglePrompt.setText("Don't have an account? ");
            tvToggleAction.setText("Sign up");
        } else {
            tvTitle.setText("Join Us!");
            tvSubtitle.setText("Create an account to get started");
            tvForgotPassword.setVisibility(View.GONE);
            etName.setVisibility(View.VISIBLE); // Show Name field during Sign Up
            btnSubmit.setText("Create Account");
            tvTogglePrompt.setText("Already have an account? ");
            tvToggleAction.setText("Sign in");
        }
    }

    private void loginUser(String email, String password) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Signing In...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Sign In");
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Updated to accept 'name' parameter
    private void registerUser(String name, String email, String password) {
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            // Replaced hardcoded "Food Explorer" with the user's actual name
                            UserProfile newUserProfile = new UserProfile(
                                    firebaseUser.getUid(),
                                    name,
                                    "Not specified"
                            );

                            FirestoreRepository firestoreRepo = new FirestoreRepository();
                            firestoreRepo.saveUserProfile(newUserProfile);
                        }

                        Toast.makeText(LoginActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Create Account");
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}