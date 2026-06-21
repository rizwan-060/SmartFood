package com.example.smartfood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Don't forget to import your new Helper and BottomNavigationView!
import com.example.smartfood.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail, tvInitials;
    private ChipGroup cgDietPreferences;
    private View menuEditProfile, menuPreferences, menuNotifications;
    private View toggleDarkMode, togglePushNotifications;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initViews();
        setupMenuItems();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            tvProfileEmail.setText(currentUser.getEmail());
            loadUserData();
        } else {
            sendToLogin();
        }

        setupListeners();
        setupBottomNav();
    }

    private void initViews() {
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvInitials = findViewById(R.id.tvInitials);
        cgDietPreferences = findViewById(R.id.cgDietPreferences);
        btnLogout = findViewById(R.id.btnLogout);

        menuEditProfile = findViewById(R.id.menuEditProfile);
        menuPreferences = findViewById(R.id.menuPreferences);
        menuNotifications = findViewById(R.id.menuNotifications);
        toggleDarkMode = findViewById(R.id.toggleDarkMode);
        togglePushNotifications = findViewById(R.id.togglePushNotifications);
    }

    private void setupMenuItems() {
        // Edit Profile
        TextView title1 = menuEditProfile.findViewById(R.id.menuTitle);
        TextView sub1 = menuEditProfile.findViewById(R.id.menuSubtitle);
        title1.setText("Edit Profile");
        sub1.setText("Update your personal info");

        // Preferences
        TextView title2 = menuPreferences.findViewById(R.id.menuTitle);
        TextView sub2 = menuPreferences.findViewById(R.id.menuSubtitle);
        title2.setText("Preferences");
        sub2.setText("Dietary restrictions & allergies");

        // Notifications
        TextView title3 = menuNotifications.findViewById(R.id.menuTitle);
        TextView sub3 = menuNotifications.findViewById(R.id.menuSubtitle);
        title3.setText("Notifications");
        sub3.setText("Manage notification settings");

        // Dark Mode Toggle
        TextView tDark = toggleDarkMode.findViewById(R.id.toggleTitle);
        TextView subDark = toggleDarkMode.findViewById(R.id.toggleSubtitle);
        tDark.setText("Dark Mode");
        subDark.setText("Switch theme appearance");

        // Push Notifications Toggle
        TextView tPush = togglePushNotifications.findViewById(R.id.toggleTitle);
        TextView subPush = togglePushNotifications.findViewById(R.id.toggleSubtitle);
        tPush.setText("Push Notifications");
        subPush.setText("Receive recipe updates");
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("Users").document(currentUserId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Fetch fields based on your UserProfile model
                String name = documentSnapshot.getString("name");
                String diet = documentSnapshot.getString("diet");

                if (name != null) {
                    tvProfileName.setText(name);
                    // Set initials
                    if (name.length() > 0) {
                        tvInitials.setText(String.valueOf(name.charAt(0)).toUpperCase());
                    }
                }

                // Check the correct diet chip
                if (diet != null) {
                    checkDietChip(diet);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // Logout user securely
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            sendToLogin();
        });

        // Update database instantly when a new diet preference is clicked
        cgDietPreferences.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int chipId = checkedIds.get(0);
                Chip selectedChip = findViewById(chipId);
                String newDiet = selectedChip.getText().toString();

                // Update Firestore
                db.collection("Users").document(currentUserId)
                        .update("diet", newDiet)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(ProfileActivity.this, "Diet updated!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
            }
        });
        menuEditProfile.setOnClickListener(v -> showEditProfileDialog());
        menuPreferences.setOnClickListener(v -> showPreferencesDialog());
        menuNotifications.setOnClickListener(v -> showNotificationsDialog());
    }

    private void checkDietChip(String diet) {
        for (int i = 0; i < cgDietPreferences.getChildCount(); i++) {
            Chip chip = (Chip) cgDietPreferences.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase(diet)) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void sendToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.setupBottomNav(this, bottomNav, R.id.nav_profile);
    }


    private void showEditProfileDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_edit_profile);

        EditText etName = dialog.findViewById(R.id.etEditName);
        EditText etEmail = dialog.findViewById(R.id.etEditEmail);
        EditText etPassword = dialog.findViewById(R.id.etEditPassword);
        Button btnSave = dialog.findViewById(R.id.btnSaveProfile);

        // Pre-fill current data
        etName.setText(tvProfileName.getText().toString());
        etEmail.setText(tvProfileEmail.getText().toString());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            if (newName.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }

            // Update Name in Firestore
            db.collection("Users").document(currentUserId).update("name", newName)
                    .addOnSuccessListener(aVoid -> {
                        tvProfileName.setText(newName);
                        tvInitials.setText(String.valueOf(newName.charAt(0)).toUpperCase());
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                    });

            // Update Password in Firebase Auth if they typed a new one
            if (!newPassword.isEmpty()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && newPassword.length() >= 6) {
                    user.updatePassword(newPassword).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(this, "Password update failed. Try logging in again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (newPassword.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    return;
                }
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showPreferencesDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_preferences);

        ChipGroup chipGroup = dialog.findViewById(R.id.cgPopupPreferences);
        Button btnSave = dialog.findViewById(R.id.btnSavePreferences);

        // The 20+ dietary preferences array
        String[] diets = {
                "None", "Vegetarian", "Vegan", "Pescatarian", "Keto", "Paleo",
                "Gluten-Free", "Dairy-Free", "Nut-Free", "Halal", "Kosher",
                "Low-Carb", "Low-Fat", "High-Protein", "Mediterranean",
                "Low-FODMAP", "Sugar-Free", "Organic", "Plant-Based",
                "Carnivore", "Flexitarian", "Whole30"
        };

        // Dynamically add chips to the view
        for (String diet : diets) {
            Chip chip = new Chip(this);
            chip.setText(diet);
            chip.setCheckable(true);
            chip.setClickable(true);
            chipGroup.addView(chip);
        }

        // Pre-select their current diet from the database
        db.collection("Users").document(currentUserId).get().addOnSuccessListener(document -> {
            String currentDiet = document.getString("diet");
            if (currentDiet != null) {
                for (int i = 0; i < chipGroup.getChildCount(); i++) {
                    Chip c = (Chip) chipGroup.getChildAt(i);
                    if (c.getText().toString().equals(currentDiet)) {
                        c.setChecked(true);
                        break;
                    }
                }
            }
        });

        btnSave.setOnClickListener(v -> {
            int selectedId = chipGroup.getCheckedChipId();
            if (selectedId != View.NO_ID) {
                Chip selectedChip = dialog.findViewById(selectedId);
                String selectedDiet = selectedChip.getText().toString();

                // Save new preference to database
                db.collection("Users").document(currentUserId).update("diet", selectedDiet)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();
                            // Optional: Update the chips on the main profile screen too!
                            checkDietChip(selectedDiet);
                            dialog.dismiss();
                        });
            } else {
                Toast.makeText(this, "Please select a preference", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showNotificationsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_notifications);

        com.google.android.material.switchmaterial.SwitchMaterial switchPush = dialog.findViewById(R.id.switchPush);
        com.google.android.material.switchmaterial.SwitchMaterial switchEmail = dialog.findViewById(R.id.switchEmail);
        com.google.android.material.switchmaterial.SwitchMaterial switchReminders = dialog.findViewById(R.id.switchReminders);
        Button btnSave = dialog.findViewById(R.id.btnSaveNotifications);

        // Load existing notification settings from Firestore
        db.collection("Users").document(currentUserId).get().addOnSuccessListener(doc -> {
            if (doc.contains("notif_push")) switchPush.setChecked(Boolean.TRUE.equals(doc.getBoolean("notif_push")));
            if (doc.contains("notif_email")) switchEmail.setChecked(Boolean.TRUE.equals(doc.getBoolean("notif_email")));
            if (doc.contains("notif_reminders")) switchReminders.setChecked(Boolean.TRUE.equals(doc.getBoolean("notif_reminders")));
        });

        btnSave.setOnClickListener(v -> {
            // Save settings back to database
            db.collection("Users").document(currentUserId)
                    .update(
                            "notif_push", switchPush.isChecked(),
                            "notif_email", switchEmail.isChecked(),
                            "notif_reminders", switchReminders.isChecked()
                    ).addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Notification settings saved!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }
}