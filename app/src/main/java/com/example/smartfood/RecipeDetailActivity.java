package com.example.smartfood;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.repository.RecipeRepository;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView ivDetailImage, ivHeartImage, ivHeartBottom;
    private TextView tvDetailTitle, tvDetailTime, tvDetailServings, tvProgressText;
    private TextView tvPro, tvCarb, tvFat, tvCalSummary, tvTotalCalories, tvHeartBottom;
    private Button tabIngredients, tabSteps, btnMealPlanBottom;
    private FrameLayout btnBack, btnLikeImage, btnShare;
    private LinearLayout btnLikeBottom, containerSteps;
    private LinearProgressIndicator progressStep;
    private RecyclerView rvIngredients, rvSteps;

    private RecipeRepository repository;
    private StepAdapter stepAdapter;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        initViews();
        repository = new RecipeRepository();
        setupRecyclerViews();

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        if (recipeId != -1) {
            fetchRecipeData(recipeId);
        } else {
            Toast.makeText(this, "Error loading recipe", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initViews() {
        ivDetailImage = findViewById(R.id.ivDetailImage);
        ivHeartImage = findViewById(R.id.ivHeartImage);
        ivHeartBottom = findViewById(R.id.ivHeartBottom);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailTime = findViewById(R.id.tvDetailTime);
        tvDetailServings = findViewById(R.id.tvDetailServings);
        tvProgressText = findViewById(R.id.tvProgressText);

        tvPro = findViewById(R.id.tvPro);
        tvCarb = findViewById(R.id.tvCarb);
        tvFat = findViewById(R.id.tvFat);
        tvCalSummary = findViewById(R.id.tvCalSummary);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvHeartBottom = findViewById(R.id.tvHeartBottom);

        tabIngredients = findViewById(R.id.tabIngredients);
        tabSteps = findViewById(R.id.tabSteps);
        btnMealPlanBottom = findViewById(R.id.btnMealPlanBottom);

        btnBack = findViewById(R.id.btnBack);
        btnLikeImage = findViewById(R.id.btnLikeImage);
        btnShare = findViewById(R.id.btnShare);
        btnLikeBottom = findViewById(R.id.btnLikeBottom);

        containerSteps = findViewById(R.id.containerSteps);
        progressStep = findViewById(R.id.progressStep);

        rvIngredients = findViewById(R.id.rvIngredients);
        rvSteps = findViewById(R.id.rvSteps);
    }

    private void setupRecyclerViews() {
        // Ingredients List
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvIngredients.setNestedScrollingEnabled(false); // Keeps ScrollView smooth

        // Cooking Steps List
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setNestedScrollingEnabled(false);
    }

    private void fetchRecipeData(int id) {
        repository.getRecipeDetails(id).observe(this, recipe -> {
            if (recipe != null) {
                populateUI(recipe);
            } else {
                Toast.makeText(this, "Failed to load details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(Recipe recipe) {
        tvDetailTitle.setText(recipe.getTitle());
        tvDetailTime.setText("Duration: " + recipe.getReadyInMinutes() + " min");
        tvDetailServings.setText("Servings: " + recipe.getServings());

        Glide.with(this).load(recipe.getImage()).into(ivDetailImage);

        // Map Nutrition summary (Protein, Carbs, Fat, Calories)
        if (recipe.getNutrition() != null && recipe.getNutrition().getNutrients() != null) {
            for (Recipe.Nutrient n : recipe.getNutrition().getNutrients()) {
                String amountText = Math.round(n.getAmount()) + "";
                if (n.getName().equals("Protein")) tvPro.setText(amountText + "g\nProtein");
                if (n.getName().equals("Carbohydrates")) tvCarb.setText(amountText + "g\nCarbs");
                if (n.getName().equals("Fat")) tvFat.setText(amountText + "g\nFat");
                if (n.getName().equals("Calories")) {
                    tvCalSummary.setText(amountText + "\nCalories");
                    tvTotalCalories.setText("Estimated: " + amountText + " Calories");
                }
            }
        }

        // Map Ingredients with new item layout
        if (recipe.getExtendedIngredients() != null) {
            IngredientAdapter ingredientAdapter = new IngredientAdapter(recipe.getExtendedIngredients());
            rvIngredients.setAdapter(ingredientAdapter);
        }

        // Map Instructions with interactive check-off logic
        if (recipe.getAnalyzedInstructions() != null && !recipe.getAnalyzedInstructions().isEmpty()) {
            List<CookingStepModel> stepModels = new ArrayList<>();
            for (Recipe.Step step : recipe.getAnalyzedInstructions().get(0).getSteps()) {
                stepModels.add(new CookingStepModel(step.getStepText(), step.getNumber()));
            }
            stepAdapter = new StepAdapter(stepModels);
            rvSteps.setAdapter(stepAdapter);
            updateProgressText(0, stepModels.size());
        } else {
            tvTotalCalories.setText("No detailed instructions provided.");
            progressStep.setVisibility(View.GONE);
            tvProgressText.setVisibility(View.GONE);
        }
    }

    private void updateProgressText(int completed, int total) {
        tvProgressText.setText(completed + " / " + total);
        if (total > 0) {
            int progress = (int) (((float) completed / (float) total) * 100);
            ObjectAnimator.ofInt(progressStep, "progress", progress).setDuration(300).start();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> Toast.makeText(this, "Link copied to clipboard! 🔗", Toast.LENGTH_SHORT).show());

        btnMealPlanBottom.setOnClickListener(v -> Toast.makeText(this, "Navigating to Meal Planner...", Toast.LENGTH_SHORT).show());

        View.OnClickListener likeClick = v -> {
            isLiked = !isLiked;
            if (isLiked) {
                // Image heart (white border, red fill)
                ivHeartImage.setImageResource(R.drawable.ic_heart);
                ivHeartImage.setColorFilter(Color.RED);

                // Bottom heart (filled red)
                ivHeartBottom.setImageResource(R.drawable.ic_heart);
                tvHeartBottom.setText("Favorited!");
                Toast.makeText(this, "Added to favorites! ❤️", Toast.LENGTH_SHORT).show();
            } else {
                // Reset image heart (white vector)
                ivHeartImage.setImageResource(R.drawable.ic_heart);
                ivHeartImage.setColorFilter(Color.WHITE);

                // Reset bottom heart
                ivHeartBottom.setImageResource(R.drawable.ic_heart);
                tvHeartBottom.setText("Add to Favorites");
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        };
        btnLikeImage.setOnClickListener(likeClick);
        btnLikeBottom.setOnClickListener(likeClick);

        // Tab Logic (Orange for active, white/black for inactive)
        tabIngredients.setOnClickListener(v -> {
            rvIngredients.setVisibility(View.VISIBLE);
            containerSteps.setVisibility(View.GONE);
            tabIngredients.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_red));
            tabIngredients.setTextColor(Color.WHITE);
            tabSteps.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
            tabSteps.setTextColor(Color.BLACK);
        });

        tabSteps.setOnClickListener(v -> {
            rvIngredients.setVisibility(View.GONE);
            containerSteps.setVisibility(View.VISIBLE);
            tabSteps.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_red));
            tabSteps.setTextColor(Color.WHITE);
            tabIngredients.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
            tabIngredients.setTextColor(Color.BLACK);
        });
    }

    // ==========================================
    // INGREDIENT RECYCLER ADAPTER
    // ==========================================
    class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {
        private List<Recipe.Ingredient> ingredients;

        public IngredientAdapter(List<Recipe.Ingredient> ingredients) {
            this.ingredients = ingredients;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Recipe.Ingredient ingredient = ingredients.get(position);
            holder.tvName.setText(ingredient.getOriginal());
        }

        @Override
        public int getItemCount() { return ingredients.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvIngredientName);
            }
        }
    }

    // ==========================================
    // COOKING STEP RECYCLER ADAPTER (Inner Class)
    // ==========================================
    class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {
        private List<CookingStepModel> steps;

        public StepAdapter(List<CookingStepModel> steps) {
            this.steps = steps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cooking_step, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CookingStepModel step = steps.get(position);
            holder.tvNumber.setText(step.number + "");
            holder.tvDescription.setText(step.text);

            // Handle checkbox state and update model
            holder.cbComplete.setOnCheckedChangeListener(null); // Prevents recycled view issues
            holder.cbComplete.setChecked(step.isCompleted);
            holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                step.isCompleted = isChecked;
                calculateProgress();
            });
        }

        @Override
        public int getItemCount() { return steps.size(); }

        private void calculateProgress() {
            int completedCount = 0;
            for (CookingStepModel step : steps) {
                if (step.isCompleted) completedCount++;
            }
            updateProgressText(completedCount, steps.size());
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNumber, tvDescription;
            CheckBox cbComplete;

            ViewHolder(View itemView) {
                super(itemView);
                tvNumber = itemView.findViewById(R.id.tvStepNumber);
                tvDescription = itemView.findViewById(R.id.tvStepDescription);
                cbComplete = itemView.findViewById(R.id.cbCompleteStep);
            }
        }
    }

    // Simple wrapper model for interactive steps
    static class CookingStepModel {
        String text;
        int number;
        boolean isCompleted;

        public CookingStepModel(String text, int number) {
            this.text = text;
            this.number = number;
            this.isCompleted = false;
        }
    }
}