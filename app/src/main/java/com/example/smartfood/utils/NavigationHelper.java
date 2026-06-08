package com.example.smartfood.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.smartfood.DiscoveryActivity;
import com.example.smartfood.MainActivity;
import com.example.smartfood.ProfileActivity;
import com.example.smartfood.R;
import com.example.smartfood.ScannerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {

    public static void setupBottomNav(Activity activity, BottomNavigationView bottomNav, int currentItemId) {
        // Highlight the correct tab for the screen we are currently on
        bottomNav.setSelectedItemId(currentItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // If user clicks the tab they are already on, do nothing
            if (id == currentItemId) {
                return true;
            }

            if (id == R.id.nav_home) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            } else if (id == R.id.nav_scan) {
                activity.startActivity(new Intent(activity, ScannerActivity.class));
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            } else if (id == R.id.nav_profile) {
                activity.startActivity(new Intent(activity, ProfileActivity.class));
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            } else if (id == R.id.nav_recipes) {
                activity.startActivity(new Intent(activity, DiscoveryActivity.class));
                activity.overridePendingTransition(0, 0);
                activity.finish();
                return true;
            } else if (id == R.id.nav_places) {
                Toast.makeText(activity, "Places Screen Coming Soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}