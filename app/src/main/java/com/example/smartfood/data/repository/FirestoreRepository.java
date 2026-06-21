package com.example.smartfood.data.repository;

import android.util.Log;

import com.example.smartfood.data.models.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreRepository {

    private FirebaseFirestore db;
    private static final String TAG = "FirestoreRepo";

    public FirestoreRepository() {
        // Initialize the Firestore database instance
        db = FirebaseFirestore.getInstance();
    }

    // Method to save or update a user profile
    public void saveUserProfile(UserProfile user) {
        // We create a collection called "Users" and save the document using the user's ID
        db.collection("Users").document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "🟢 SUCCESS: Data saved to Firestore! Check your browser!"))
                .addOnFailureListener(e -> Log.e(TAG, "🔴 ERROR: Failed to save data", e));
    }
}