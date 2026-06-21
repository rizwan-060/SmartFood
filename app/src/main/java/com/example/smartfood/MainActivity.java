package com.example.smartfood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartfood.data.api.ApiClient;
import com.example.smartfood.data.api.WeatherApiService;
import com.example.smartfood.data.models.Recipe;
import com.example.smartfood.data.models.WeatherResponse;
import com.example.smartfood.data.repository.RecipeRepository;
import com.example.smartfood.ui.adapters.RecipeAdapter;
import com.example.smartfood.utils.NavigationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.smartfood.data.models.UserProfile;
import com.example.smartfood.data.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private TextView tvWeatherSuggestion;
    private RecyclerView rvTrending, rvRecommended, rvFilters;
    private RecipeAdapter recipeAdapter;
    private RecipeRepository recipeRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        recipeRepository = new RecipeRepository();
        initViews();
        setupFilters();
        setupBottomNav();
        fetchWeatherSuggestion();
        fetchRecipes();
    }

    private void initViews() {
        tvWeatherSuggestion = findViewById(R.id.tvWeatherSuggestion);

        // 1. Setup Filters (Horizontal)
        rvFilters = findViewById(R.id.rvFilters);
        rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 2. Setup Trending (Horizontal)
        rvTrending = findViewById(R.id.rvTrending);
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 3. Setup Recommended (2-Column Grid)
        rvRecommended = findViewById(R.id.rvRecommended);
        rvRecommended.setLayoutManager(new GridLayoutManager(this, 2));

        // 4. Restaurant Click Listener
        findViewById(R.id.cardRestaurant).setOnClickListener(v -> {
            Toast.makeText(this, "Opening Map...", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to Map/Restaurant Activity here
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        // Tell the helper this is the Home screen
        NavigationHelper.setupBottomNav(this, bottomNav, R.id.nav_home);
    }

    private void setupFilters() {
        List<String> filters = Arrays.asList("All", "Italian", "Asian", "Mexican", "Healthy", "Desserts");

        RecyclerView.Adapter<RecyclerView.ViewHolder> filterAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(MainActivity.this);
                // Applies your pill shape background to the filter chips
                tv.setBackgroundResource(R.drawable.bg_filter);
                tv.setPadding(40, 20, 40, 20);
                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                tv.setLayoutParams(params);
                return new RecyclerView.ViewHolder(tv) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText(filters.get(position));
            }

            @Override
            public int getItemCount() {
                return filters.size();
            }
        };
        rvFilters.setAdapter(filterAdapter);
    }

    private void fetchWeatherSuggestion() {
        String city = "Lahore";
        String apiKey = "484acafb11d7e90214769aec7478c099";

        WeatherApiService weatherApi = ApiClient.getWeatherClient().create(WeatherApiService.class);

        weatherApi.getWeatherByCity(city, apiKey, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    double temp = response.body().getMain().getTemp();
                    if (temp < 20) {
                        tvWeatherSuggestion.setText("Hot Soup & Comfort Food 🍜");
                    } else {
                        tvWeatherSuggestion.setText("Fresh Salads & Cold Drinks 🥗");
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e("API_ERROR", "Weather API failed: " + t.getMessage());
                tvWeatherSuggestion.setText("Could not load weather.");
            }
        });
    }

    private void fetchRecipes() {
        // Fetch 9 total recipes
        recipeRepository.getRandomRecipes(9).observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                if (recipes != null && recipes.size() >= 9) {
                    // Split for Horizontal Scrolling (5 items)
                    List<Recipe> trendingList = recipes.subList(0, 5);
                    rvTrending.setAdapter(new RecipeAdapter(MainActivity.this, trendingList));

                    // Split for 2x2 Grid (4 items)
                    List<Recipe> recommendedList = recipes.subList(5, 9);
                    rvRecommended.setAdapter(new RecipeAdapter(MainActivity.this, recommendedList));
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load all recipes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}