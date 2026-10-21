package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

/**
 * MainActivity serves as the primary home and discovery feed for GhorCycle.
 *
 * Demonstrates:
 * 1. Explicit Intent navigation to AddItemActivity, ProductDetailsActivity, and ProfileActivity.
 * 2. Explicit Intent extra passing (Sample Product ID, Name, Price, Category, Condition, Seller, Description)
 *    to ProductDetailsActivity.
 * 3. Interactive click listeners for feed discovery and navigation.
 * 4. Proper double-back to exit implementation via OnBackPressedDispatcher.
 * 5. Complete Activity lifecycle logging and state preservation.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";
    private long lastBackPressTime = 0;
    private String currentUserName = "Neighbor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity: onCreate() - Initializing home screen and data feeds");
        setContentView(R.layout.activity_main);

        // Extract user identifier if navigated from Auth flow
        if (getIntent() != null) {
            String passedName = getIntent().getStringExtra(LoginActivity.EXTRA_USER_NAME);
            if (!TextUtils.isEmpty(passedName)) {
                currentUserName = passedName;
            }
        }

        TextView tvGreeting = findViewById(R.id.tvMainGreeting);
        tvGreeting.setText("Hello, " + currentUserName + " 👋");

        // Action Buttons
        Button btnAddItem = findViewById(R.id.btnMainAddItem);
        Button btnProfileNav = findViewById(R.id.btnMainProfileNav);
        Button btnViewProfile = findViewById(R.id.btnMainViewProfile);
        Button btnLogout = findViewById(R.id.btnMainLogout);

        // Featured Cards and detail buttons
        CardView cardProduct1 = findViewById(R.id.cardProduct1);
        CardView cardProduct2 = findViewById(R.id.cardProduct2);
        CardView cardProduct3 = findViewById(R.id.cardProduct3);

        Button btnViewProduct1 = findViewById(R.id.btnViewProduct1);
        Button btnViewProduct2 = findViewById(R.id.btnViewProduct2);
        Button btnViewProduct3 = findViewById(R.id.btnViewProduct3);

        // Navigate to AddItemActivity via explicit Intent
        btnAddItem.setOnClickListener(v -> {
            Log.d(TAG, "MainActivity: Explicit navigation -> AddItemActivity");
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        // Navigate to ProfileActivity via explicit Intent
        View.OnClickListener profileClickListener = v -> {
            Log.d(TAG, "MainActivity: Explicit navigation -> ProfileActivity");
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_PROFILE_NAME, currentUserName);
            startActivity(intent);
        };
        btnProfileNav.setOnClickListener(profileClickListener);
        btnViewProfile.setOnClickListener(profileClickListener);

        // Logout and return to LoginActivity
        btnLogout.setOnClickListener(v -> {
            Log.d(TAG, "MainActivity: User logging out, explicit navigation -> LoginActivity");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Sample Product 1: Mountain Bike
        View.OnClickListener product1Listener = v -> openProductDetails(
                "GC-BIKE-101",
                "Phoenix Tornado 26\" MTB Bicycle",
                "৳ 9,500",
                "Bicycles & Commute",
                "Gently Used",
                "Rafiqul Islam (Dhanmondi)",
                "Well-maintained 21-speed mountain bike with dual disc brakes and fresh puncture-resistant tires. Perfect for daily city commuting or light trails."
        );
        cardProduct1.setOnClickListener(product1Listener);
        btnViewProduct1.setOnClickListener(product1Listener);

        // Sample Product 2: Study Desk
        View.OnClickListener product2Listener = v -> openProductDetails(
                "GC-DESK-102",
                "Solid Oak Study Desk & Ergonomic Chair",
                "৳ 5,200",
                "Furniture & Living",
                "Like New",
                "Tasnim Rahman (Uttara)",
                "Solid Malaysian oak study table with 3 lockable storage drawers and an adjustable breathable mesh chair. Gently used for only 6 months."
        );
        cardProduct2.setOnClickListener(product2Listener);
        btnViewProduct2.setOnClickListener(product2Listener);

        // Sample Product 3: Air Fryer
        View.OnClickListener product3Listener = v -> openProductDetails(
                "GC-APPL-103",
                "Philips Essential Air Fryer HD9200 (4.1L)",
                "৳ 6,800",
                "Kitchen Electronics",
                "Brand New (Open Box)",
                "Kamal Uddin (Mirpur)",
                "Authentic 1400W Rapid Air technology air fryer. Purchased from authorized distributor; includes user manual, recipe book, and warranty card."
        );
        cardProduct3.setOnClickListener(product3Listener);
        btnViewProduct3.setOnClickListener(product3Listener);

        // System Back Button: Double press to exit app
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBackPressTime < 2000) {
                    Log.d(TAG, "MainActivity: Double back pressed, exiting application");
                    finishAffinity();
                } else {
                    lastBackPressTime = currentTime;
                    Toast.makeText(MainActivity.this, "Press BACK again to exit GhorCycle", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Helper method to construct and trigger an explicit Intent to ProductDetailsActivity
     * with detailed sample product extras.
     */
    private void openProductDetails(String id, String name, String price, String category,
                                    String condition, String seller, String description) {
        Log.d(TAG, "MainActivity: Explicit navigation -> ProductDetailsActivity for Item ID=" + id);
        Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, id);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_NAME, name);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_PRICE, price);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY, category);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION, condition);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_SELLER, seller);
        intent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION, description);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart() - Activity becoming visible to user");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume() - Activity focused and interactive");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause() - Activity losing focus");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity: onStop() - Activity stopped and hidden");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "MainActivity: onRestart() - Activity restarting from background");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity: onDestroy() - Activity resources released");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "MainActivity: onSaveInstanceState() - Preserving UI state across configuration change");
        outState.putString("saved_user_name", currentUserName);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "MainActivity: onRestoreInstanceState() - Restoring UI state after rotation/restart");
        currentUserName = savedInstanceState.getString("saved_user_name", currentUserName);
        TextView tvGreeting = findViewById(R.id.tvMainGreeting);
        if (tvGreeting != null) {
            tvGreeting.setText("Hello, " + currentUserName + " 👋");
        }
    }
}