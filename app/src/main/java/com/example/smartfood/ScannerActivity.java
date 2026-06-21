package com.example.smartfood;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.repository.RecipeRepository;
import com.example.smartfood.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    private View llInitialState, llScanningState, scanLine, rlOverlayBoxes, llResults;
    private Button btnScan;
    private ImageView ivScanRotate, btnReset, ivCapturedImage;
    private RecyclerView rvDetected, rvSuggested;
    private ConstraintLayout scannerContainer;

    private ObjectAnimator scanLineAnimator;
    private ObjectAnimator rotateAnimator;

    private RecipeRepository recipeRepository;

    // 1. LAUNCHER: Opens Camera and returns the photo (MUST BE DEFINED FIRST)
    private final ActivityResultLauncher<Void> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    processCapturedImage(bitmap);
                } else {
                    Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // 2. LAUNCHER: Asks for Camera Permission (NOW IT CAN SAFELY CALL takePictureLauncher)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted! Open the camera.
                    takePictureLauncher.launch(null);
                } else {
                    Toast.makeText(this, "Camera permission is required to scan ingredients.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        recipeRepository = new RecipeRepository();

        initViews();
        setupBottomNav(); // Using your matched navigation style
        setupRecyclerViews();

        // Check for permissions when "Scan Ingredients" is clicked
        btnScan.setOnClickListener(v -> checkCameraPermissionAndScan());
        btnReset.setOnClickListener(v -> resetScan());
    }

    private void checkCameraPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Already have permission, launch camera directly
            takePictureLauncher.launch(null);
        } else {
            // Ask the user for permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initViews() {
        llInitialState = findViewById(R.id.llInitialState);
        llScanningState = findViewById(R.id.llScanningState);
        scanLine = findViewById(R.id.scanLine);
        rlOverlayBoxes = findViewById(R.id.rlOverlayBoxes);
        llResults = findViewById(R.id.llResults);
        btnScan = findViewById(R.id.btnScan);
        ivScanRotate = findViewById(R.id.ivScanRotate);
        btnReset = findViewById(R.id.btnReset);
        scannerContainer = findViewById(R.id.scannerContainer);

        rvDetected = findViewById(R.id.rvDetected);
        rvSuggested = findViewById(R.id.rvSuggested);

        ivCapturedImage = new ImageView(this);
        ivCapturedImage.setLayoutParams(new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ivCapturedImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivCapturedImage.setAlpha(0.6f);
        scannerContainer.addView(ivCapturedImage, 0);
    }

    // UPDATED NAVIGATION LOGIC
    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        // Tell the helper this is the Scan screen
        NavigationHelper.setupBottomNav(this, bottomNav, R.id.nav_scan);
    }

    private void processCapturedImage(Bitmap bitmap) {
        ivCapturedImage.setImageBitmap(bitmap);
        startScanAnimation();
        File imageFile = bitmapToFile(bitmap);

        recipeRepository.classifyFoodImage(imageFile).observe(this, response -> {
            finishScanAnimation();

            if (response != null && response.getCategory() != null) {
                int confidence = (int) (response.getProbability() * 100);
                String categoryName = response.getCategory().substring(0, 1).toUpperCase() + response.getCategory().substring(1);

                showDetectionResults(categoryName, confidence);
                fetchRecipesForIngredient(categoryName);
            } else {
                Toast.makeText(ScannerActivity.this, "Could not identify food. Try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startScanAnimation() {
        btnScan.setText("Scanning...");
        btnScan.setEnabled(false);
        llInitialState.setVisibility(View.GONE);
        llScanningState.setVisibility(View.VISIBLE);
        scanLine.setVisibility(View.VISIBLE);

        rotateAnimator = ObjectAnimator.ofFloat(ivScanRotate, "rotation", 0f, 360f);
        rotateAnimator.setDuration(2000);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.start();

        scannerContainer.post(() -> {
            float height = scannerContainer.getHeight();
            scanLineAnimator = ObjectAnimator.ofFloat(scanLine, "translationY", 0f, height);
            scanLineAnimator.setDuration(2000);
            scanLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
            scanLineAnimator.setInterpolator(new LinearInterpolator());
            scanLineAnimator.start();
        });
    }

    private void finishScanAnimation() {
        if (rotateAnimator != null) rotateAnimator.cancel();
        if (scanLineAnimator != null) scanLineAnimator.cancel();

        llScanningState.setVisibility(View.GONE);
        scanLine.setVisibility(View.GONE);
        btnScan.setVisibility(View.GONE);
    }

    private void showDetectionResults(String foodName, int confidence) {
        rlOverlayBoxes.setVisibility(View.VISIBLE);
        rlOverlayBoxes.setAlpha(0f);
        rlOverlayBoxes.animate().alpha(1f).setDuration(500).start();

        llResults.setVisibility(View.VISIBLE);
        llResults.setTranslationY(50f);
        llResults.setAlpha(0f);
        llResults.animate().alpha(1f).translationY(0f).setDuration(500).start();

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(new Ingredient(foodName, confidence, "#3FA34D"));

        if(confidence < 95) ingredients.add(new Ingredient("Similar Item", confidence - 15, "#FFA726"));

        rvDetected.setAdapter(new IngredientAdapter(ingredients));
    }

    private void fetchRecipesForIngredient(String query) {
        recipeRepository.searchRecipes(query).observe(this, recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                List<Recipe> topSuggestions = recipes.size() > 3 ? recipes.subList(0, 3) : recipes;
                rvSuggested.setAdapter(new SuggestedRecipeAdapter(topSuggestions, query));
            }
        });
    }

    private void resetScan() {
        rlOverlayBoxes.setVisibility(View.GONE);
        llResults.setVisibility(View.GONE);
        llInitialState.setVisibility(View.VISIBLE);
        btnScan.setVisibility(View.VISIBLE);
        btnScan.setEnabled(true);
        btnScan.setText("Scan Ingredients");
        ivCapturedImage.setImageDrawable(null);
    }

    private File bitmapToFile(Bitmap bitmap) {
        File file = new File(getCacheDir(), "scan_image.jpg");
        try {
            file.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void setupRecyclerViews() {
        rvDetected.setLayoutManager(new GridLayoutManager(this, 2));
        rvSuggested.setLayoutManager(new LinearLayoutManager(this));
    }

    public static class Ingredient {
        String name, color; int confidence;
        public Ingredient(String n, int c, String col) { name = n; confidence = c; color = col; }
    }

    private class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
        List<Ingredient> list;
        public IngredientAdapter(List<Ingredient> list) { this.list = list; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detected_ingredient, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ingredient item = list.get(position);
            holder.tvName.setText(item.name);
            holder.tvConfidence.setText(item.confidence + "%");
            holder.pbConfidence.setProgress(item.confidence);
            holder.ivCheck.setColorFilter(Color.parseColor(item.color));
        }

        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvConfidence; ProgressBar pbConfidence; ImageView ivCheck;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvConfidence = v.findViewById(R.id.tvConfidence);
                pbConfidence = v.findViewById(R.id.pbConfidence);
                ivCheck = v.findViewById(R.id.ivCheck);
            }
        }
    }

    private class SuggestedRecipeAdapter extends RecyclerView.Adapter<SuggestedRecipeAdapter.ViewHolder> {
        List<Recipe> list;
        String detectedTag;
        public SuggestedRecipeAdapter(List<Recipe> list, String detectedTag) { this.list = list; this.detectedTag = detectedTag; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_suggested_recipe, parent, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Recipe item = list.get(position);
            holder.tvName.setText(item.getTitle());
            holder.tvTime.setText("View Details");

            holder.llTags.removeAllViews();
            TextView tvTag = new TextView(ScannerActivity.this);
            tvTag.setText(detectedTag);
            tvTag.setBackgroundResource(R.drawable.bg_pill_green);
            tvTag.setTextColor(Color.parseColor("#3FA34D"));
            tvTag.setTextSize(12f);
            tvTag.setPadding(24, 8, 24, 8);
            holder.llTags.addView(tvTag);
        }

        @Override public int getItemCount() { return list.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvTime; LinearLayout llTags;
            public ViewHolder(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvRecipeName);
                tvTime = v.findViewById(R.id.tvRecipeTime);
                llTags = v.findViewById(R.id.llIngredientsTags);
            }
        }
    }
}