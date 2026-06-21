package com.example.smartfood;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.repository.RecipeRepository;
import com.example.smartfood.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryActivity extends AppCompatActivity {

    private EditText etSearchInput;
    private ImageButton btnToggleFilters;
    private CardView cvAdvancedFilters;
    private ChipGroup cgCuisines, cgDiets;
    private Button btnApplyFilters;
    private TextView tvResultsCount;
    private RecyclerView rvRecipes;

    private RecipeAdapter adapter;
    private RecipeRepository repository;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userDiet = ""; // Store their saved diet here

    private final String[] cuisines = {"All", "Italian", "Asian", "Mexican", "American", "Mediterranean"};
    private final String[] diets = {"All Diets", "Vegetarian", "Vegan", "Keto", "Paleo", "Gluten-Free"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        repository = new RecipeRepository();

        initViews();
        setupBottomNav();
        setupChips();
        setupRecyclerView();
        setupListeners();

        // 1. First, fetch their diet from Firebase, THEN load the recipes
        fetchUserDietAndLoadRecipes();
    }

    private void initViews() {
        etSearchInput = findViewById(R.id.etSearchInput);
        btnToggleFilters = findViewById(R.id.btnToggleFilters);
        cvAdvancedFilters = findViewById(R.id.cvAdvancedFilters);
        cgCuisines = findViewById(R.id.cgCuisines);
        cgDiets = findViewById(R.id.cgDiets);
        btnApplyFilters = findViewById(R.id.btnApplyFilters);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        rvRecipes = findViewById(R.id.rvRecipes);
    }

    private void fetchUserDietAndLoadRecipes() {
        tvResultsCount.setText("Loading your recommendations...");

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists() && document.getString("diet") != null) {
                            userDiet = document.getString("diet");
                        }
                        // Fetch initial random recipes using the diet we just found
                        fetchRecipes("random");
                    })
                    .addOnFailureListener(e -> {
                        fetchRecipes("random");
                    });
        } else {
            fetchRecipes("random");
        }
    }

    private void setupChips() {
        for (String cuisine : cuisines) {
            Chip chip = new Chip(this);
            chip.setText(cuisine);
            chip.setCheckable(true);
            if (cuisine.equals("All")) chip.setChecked(true);
            cgCuisines.addView(chip);
        }

        for (String diet : diets) {
            Chip chip = new Chip(this);
            chip.setText(diet);
            chip.setCheckable(true);
            if (diet.equals("All Diets")) chip.setChecked(true);
            cgDiets.addView(chip);
        }
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter();
        rvRecipes.setLayoutManager(new GridLayoutManager(this, 2));
        rvRecipes.setAdapter(adapter);
    }

    private void setupListeners() {
        btnToggleFilters.setOnClickListener(v -> {
            cvAdvancedFilters.setVisibility(cvAdvancedFilters.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        btnApplyFilters.setOnClickListener(v -> cvAdvancedFilters.setVisibility(View.GONE));

        etSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearchInput.getText().toString().trim();
                if (!query.isEmpty()) fetchRecipes(query);
                return true;
            }
            return false;
        });

        cgCuisines.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip chip = findViewById(checkedId);
                String selected = chip.getText().toString();
                fetchRecipes(selected.equals("All") ? "random" : selected);
            }
        });
    }

    private void fetchRecipes(String query) {
        tvResultsCount.setText("Fetching recipes...");
        String tagsToSearch = "";

        // 1. Add their saved Firebase diet to the search tags (if they have one)
        if (!userDiet.isEmpty() && !userDiet.equalsIgnoreCase("None") && !userDiet.equalsIgnoreCase("Not specified")) {
            tagsToSearch = userDiet.toLowerCase();
        }

        // 2. Add the selected chip (like "Italian" or a search term) to the tags
        if (!query.isEmpty() && !query.equalsIgnoreCase("random") && !query.equalsIgnoreCase("All")) {
            if (!tagsToSearch.isEmpty()) {
                tagsToSearch += "," + query.toLowerCase(); // Spoonacular uses comma-separated tags
            } else {
                tagsToSearch = query.toLowerCase();
            }
        }

        // 3. Make the API Call
        if (tagsToSearch.isEmpty()) {
            // No diet saved, and no chips clicked -> completely random
            repository.getRandomRecipes(10).observe(this, this::updateUI);
        } else {
            // Filtered random by diet and/or clicked chip!
            repository.getRandomRecipesWithTags(10, tagsToSearch).observe(this, this::updateUI);
        }
    }

    private void updateUI(List<Recipe> recipes) {
        if (recipes != null && !recipes.isEmpty()) {
            adapter.setRecipes(recipes);
            tvResultsCount.setText("Found " + recipes.size() + " recipes");
        } else {
            adapter.setRecipes(new ArrayList<>());
            tvResultsCount.setText("No recipes found.");
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationHelper.setupBottomNav(this, bottomNav, R.id.nav_recipes);
    }

    // ==========================================
    // RECYCLER VIEW ADAPTER
    // ==========================================
    class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
        private List<Recipe> recipes = new ArrayList<>();

        public void setRecipes(List<Recipe> newRecipes) {
            this.recipes = newRecipes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Recipe recipe = recipes.get(position);
            holder.tvTitle.setText(recipe.getTitle());

            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImage())
                    .centerCrop()
                    .into(holder.ivImage);

            holder.itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(DiscoveryActivity.this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipe.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvTitle;

            ViewHolder(View itemView) {
                super(itemView);
                ivImage = itemView.findViewById(R.id.ivRecipeImage);
                tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            }
        }
    }
}