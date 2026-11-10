package com.example.myapplication;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity is the primary GhorCycle marketplace discovery hub.
 *
 * Implements:
 * 1. Eco-friendly green-and-white theme layout with search and category filtering.
 * 2. RecyclerView with adaptive 2-column grid layout on portrait and 3-column on landscape.
 * 3. Real-time product search by name and category filtering across 8 core eco-categories.
 * 4. Empty, loading, and error UI states.
 * 5. Bottom navigation interface for Home, Explore, Add Item, Favorites, and Profile.
 * 6. Explicit Intent navigation to ProductDetailsActivity, AddItemActivity, and ProfileActivity.
 * 7. Comprehensive Activity lifecycle logging.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    private String currentUserName = "Neighbor";
    private String selectedCategory = SampleDataProvider.CATEGORY_ALL;
    private String currentQuery = "";
    private long lastBackPressTime = 0;

    private List<Product> masterProductList = new ArrayList<>();
    private List<Product> displayedProductList = new ArrayList<>();

    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    // Views
    private EditText etSearch;
    private ImageButton btnSearchClear;
    private RecyclerView rvCategories;
    private RecyclerView rvProducts;
    private TextView tvResultCount;
    private TextView btnResetFilters;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoadingState;
    private LinearLayout layoutErrorState;
    private TextView tvEmptyMessage;
    private BottomNavigationView bottomNav;
    private ListenerRegistration productListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate() - Initializing marketplace feed and adapters");
        setContentView(R.layout.activity_main);

        // Extract user session name if provided
        if (getIntent() != null) {
            String passedName = getIntent().getStringExtra(LoginActivity.EXTRA_USER_NAME);
            if (!TextUtils.isEmpty(passedName)) {
                currentUserName = passedName;
            }
        }

        bindViews();
        setupHeader();
        setupRecyclerViews();
        setupSearchAndFilters();
        setupBottomNavigation();
        setupBackNavigation();

        loadMarketplaceData();
    }

    private void bindViews() {
        etSearch = findViewById(R.id.etMainSearch);
        btnSearchClear = findViewById(R.id.btnMainSearchClear);
        rvCategories = findViewById(R.id.rvCategories);
        rvProducts = findViewById(R.id.rvProducts);
        tvResultCount = findViewById(R.id.tvResultCount);
        btnResetFilters = findViewById(R.id.btnResetFilters);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutLoadingState = findViewById(R.id.layoutLoadingState);
        layoutErrorState = findViewById(R.id.layoutErrorState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        bottomNav = findViewById(R.id.bottomNavigation);
    }

    private void setupHeader() {
        TextView tvGreeting = findViewById(R.id.tvMainGreeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Hello, " + currentUserName + " 👋");
        }

        Button btnHeaderAdd = findViewById(R.id.btnHeaderAddItem);
        if (btnHeaderAdd != null) {
            btnHeaderAdd.setOnClickListener(v -> {
                Log.d(TAG, "MainActivity: Explicit navigation -> AddItemActivity");
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupRecyclerViews() {
        // 1. Categories Horizontal RecyclerView
        LinearLayoutManager categoryLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoryLayoutManager);

        categoryAdapter = new CategoryAdapter(SampleDataProvider.getCategories(), (category, position) -> {
            Log.d(TAG, "MainActivity: Category selected -> " + category.getName());
            selectedCategory = category.getName();
            applyFilters();
        });
        rvCategories.setAdapter(categoryAdapter);

        // 2. Products Grid RecyclerView (adaptive 2-col portrait / 3-col landscape)
        int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        GridLayoutManager productLayoutManager = new GridLayoutManager(this, spanCount);
        rvProducts.setLayoutManager(productLayoutManager);

        productAdapter = new ProductAdapter(displayedProductList, product -> {
            Log.d(TAG, "MainActivity: Product card tapped -> ID=" + product.getId() + ", Name=" + product.getName());
            openProductDetails(product);
        });

        productAdapter.setOnFavoriteClickListener((product, position) -> {
            String msg = product.isFavorite()
                    ? "Added \"" + product.getName() + "\" to saved favorites"
                    : "Removed from favorites";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        rvProducts.setAdapter(productAdapter);
    }

    private void setupSearchAndFilters() {
        // Search text watcher for real-time filter
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentQuery = s != null ? s.toString() : "";
                btnSearchClear.setVisibility(currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Search clear button
        btnSearchClear.setOnClickListener(v -> {
            etSearch.setText("");
            currentQuery = "";
            applyFilters();
        });

        // Reset filter buttons
        View.OnClickListener resetClickListener = v -> resetAllFilters();
        btnResetFilters.setOnClickListener(resetClickListener);

        Button btnEmptyReset = findViewById(R.id.btnEmptyReset);
        if (btnEmptyReset != null) {
            btnEmptyReset.setOnClickListener(resetClickListener);
        }

        Button btnErrorRetry = findViewById(R.id.btnErrorRetry);
        if (btnErrorRetry != null) {
            btnErrorRetry.setOnClickListener(v -> loadMarketplaceData());
        }
    }

    private void resetAllFilters() {
        etSearch.setText("");
        currentQuery = "";
        selectedCategory = SampleDataProvider.CATEGORY_ALL;
        categoryAdapter.setSelectedCategory(SampleDataProvider.CATEGORY_ALL);
        rvCategories.smoothScrollToPosition(0);
        applyFilters();
    }

    private void loadMarketplaceData() {
        layoutLoadingState.setVisibility(View.VISIBLE);
        layoutErrorState.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        rvProducts.setVisibility(View.GONE);

        if (productListenerRegistration != null) {
            productListenerRegistration.remove();
            productListenerRegistration = null;
        }

        productListenerRegistration = FirestoreProductService.listenToProducts(new FirestoreProductService.ProductListCallback() {
            @Override
            public void onLoaded(List<Product> products) {
                layoutLoadingState.setVisibility(View.GONE);
                layoutErrorState.setVisibility(View.GONE);
                masterProductList = products != null ? products : new ArrayList<>();
                applyFilters();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error loading marketplace data: " + errorMessage);
                layoutLoadingState.setVisibility(View.GONE);
                if (masterProductList == null || masterProductList.isEmpty()) {
                    layoutErrorState.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilters() {
        displayedProductList = SampleDataProvider.filter(masterProductList, selectedCategory, currentQuery);
        productAdapter.updateProducts(displayedProductList);

        boolean hasActiveFilter = !selectedCategory.equalsIgnoreCase(SampleDataProvider.CATEGORY_ALL)
                || !currentQuery.trim().isEmpty();

        btnResetFilters.setVisibility(hasActiveFilter ? View.VISIBLE : View.GONE);

        if (displayedProductList.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            tvResultCount.setText("0 items found");

            if (!currentQuery.trim().isEmpty()) {
                tvEmptyMessage.setText(String.format(Locale.getDefault(),
                        "No products found for \"%s\" in %s. Try another keyword or reset filters.",
                        currentQuery, selectedCategory));
            } else {
                tvEmptyMessage.setText(String.format(Locale.getDefault(),
                        "No products currently listed under %s. Check back soon or list an item!",
                        selectedCategory));
            }
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            String countLabel = String.format(Locale.getDefault(),
                    "Showing %d %s",
                    displayedProductList.size(),
                    displayedProductList.size() == 1 ? "product" : "products");
            if (!selectedCategory.equalsIgnoreCase(SampleDataProvider.CATEGORY_ALL)) {
                countLabel += " in " + selectedCategory;
            }
            tvResultCount.setText(countLabel);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on Home: scroll to top
                rvProducts.smoothScrollToPosition(0);
                return true;
            } else if (itemId == R.id.nav_explore) {
                // Clearly labeled placeholder
                Log.d(TAG, "MainActivity: Explore bottom nav selected (placeholder)");
                Toast.makeText(MainActivity.this,
                        "Explore placeholder — Community recycling map coming in a future update.",
                        Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_add_item) {
                Log.d(TAG, "MainActivity: Explicit navigation -> AddItemActivity");
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                startActivity(intent);
                return false; // Keep Home visually active on return
            } else if (itemId == R.id.nav_favorites) {
                // Clearly labeled placeholder
                Log.d(TAG, "MainActivity: Favorites bottom nav selected (placeholder)");
                Toast.makeText(MainActivity.this,
                        "Favorites placeholder — Cloud wishlist sync coming in a future update.",
                        Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_profile) {
                Log.d(TAG, "MainActivity: Explicit navigation -> ProfileActivity");
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_PROFILE_NAME, currentUserName);
                startActivity(intent);
                return false; // Keep Home visually active on return
            }
            return false;
        });
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If search is active or filter is selected, back button clears them first
                if (!currentQuery.isEmpty() || !selectedCategory.equalsIgnoreCase(SampleDataProvider.CATEGORY_ALL)) {
                    resetAllFilters();
                    Toast.makeText(MainActivity.this, "Filters reset", Toast.LENGTH_SHORT).show();
                    return;
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBackPressTime < 2000) {
                    Log.d(TAG, "MainActivity: Double back confirmed, exiting app");
                    finishAffinity();
                } else {
                    lastBackPressTime = currentTime;
                    Toast.makeText(MainActivity.this, "Press BACK again to exit GhorCycle", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openProductDetails(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, product.getProductId());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_NAME, product.getTitle());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_PRICE, product.getFormattedPrice());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY, product.getCategory());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION, product.getCondition());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_SELLER, product.getLocation());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION, product.getDescription());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IMAGE_RES, product.getImageResId());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IS_FREE, product.isFree());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_OWNER_ID, product.getOwnerId());
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IMAGE_URL, product.getImageUrl());
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart() - Activity becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume() - Marketplace feed active");
        // Ensure Home item is selected when returning from other activities
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "MainActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity: onDestroy() - Releasing resources");
        if (productListenerRegistration != null) {
            productListenerRegistration.remove();
            productListenerRegistration = null;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "MainActivity: onSaveInstanceState() - Preserving filter and search state");
        outState.putString("saved_query", currentQuery);
        outState.putString("saved_category", selectedCategory);
        outState.putString("saved_user", currentUserName);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "MainActivity: onRestoreInstanceState() - Restoring filter and search state");
        currentQuery = savedInstanceState.getString("saved_query", "");
        selectedCategory = savedInstanceState.getString("saved_category", SampleDataProvider.CATEGORY_ALL);
        currentUserName = savedInstanceState.getString("saved_user", currentUserName);

        if (etSearch != null && !currentQuery.isEmpty()) {
            etSearch.setText(currentQuery);
        }
        if (categoryAdapter != null) {
            categoryAdapter.setSelectedCategory(selectedCategory);
        }
        applyFilters();
    }
}
