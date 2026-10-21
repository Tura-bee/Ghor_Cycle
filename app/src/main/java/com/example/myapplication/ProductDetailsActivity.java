package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ProductDetailsActivity displays comprehensive details for an item in GhorCycle.
 *
 * Demonstrates:
 * 1. Extraction and presentation of Intent extras passed from MainActivity or AddItemActivity.
 * 2. Explicit handling of missing/default Intent extras.
 * 3. Interactive click listeners for buyer-seller workflows (placeholders).
 * 4. Proper Up and Back navigation to return to the catalog.
 * 5. Complete Activity lifecycle logging.
 */
public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    public static final String EXTRA_PRODUCT_ID = "com.ghorcycle.extra.PRODUCT_ID";
    public static final String EXTRA_PRODUCT_NAME = "com.ghorcycle.extra.PRODUCT_NAME";
    public static final String EXTRA_PRODUCT_PRICE = "com.ghorcycle.extra.PRODUCT_PRICE";
    public static final String EXTRA_PRODUCT_CATEGORY = "com.ghorcycle.extra.PRODUCT_CATEGORY";
    public static final String EXTRA_PRODUCT_SELLER = "com.ghorcycle.extra.PRODUCT_SELLER";
    public static final String EXTRA_PRODUCT_CONDITION = "com.ghorcycle.extra.PRODUCT_CONDITION";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "com.ghorcycle.extra.PRODUCT_DESCRIPTION";

    private String productId;
    private String productName;
    private String productPrice;
    private String productCategory;
    private String productSeller;
    private String productCondition;
    private String productDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProductDetailsActivity: onCreate() - Extracting Intent extras and inflating UI");
        setContentView(R.layout.activity_product_details);

        // Extract extras passed from caller Intent
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            productPrice = getIntent().getStringExtra(EXTRA_PRODUCT_PRICE);
            productCategory = getIntent().getStringExtra(EXTRA_PRODUCT_CATEGORY);
            productSeller = getIntent().getStringExtra(EXTRA_PRODUCT_SELLER);
            productCondition = getIntent().getStringExtra(EXTRA_PRODUCT_CONDITION);
            productDescription = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);
        }

        // Apply fallbacks if any extra was not supplied
        if (TextUtils.isEmpty(productId)) productId = "GC-DEMO-00";
        if (TextUtils.isEmpty(productName)) productName = "GhorCycle Community Item";
        if (TextUtils.isEmpty(productPrice)) productPrice = "৳ 0";
        if (TextUtils.isEmpty(productCategory)) productCategory = "General";
        if (TextUtils.isEmpty(productSeller)) productSeller = "Community Neighbor";
        if (TextUtils.isEmpty(productCondition)) productCondition = "Pre-loved";
        if (TextUtils.isEmpty(productDescription)) {
            productDescription = "Quality verified household good looking for a second life.";
        }

        Log.d(TAG, "ProductDetailsActivity: Displaying item [ID=" + productId + ", Name=" + productName + "]");

        // Bind Views
        TextView tvIdBadge = findViewById(R.id.tvDetailIdBadge);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvCondition = findViewById(R.id.tvDetailCondition);
        TextView tvSeller = findViewById(R.id.tvDetailSeller);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);

        Button btnBack = findViewById(R.id.btnDetailBack);
        Button btnBackHome = findViewById(R.id.btnDetailBackHome);
        Button btnContactSeller = findViewById(R.id.btnDetailContactSeller);
        Button btnMakeOffer = findViewById(R.id.btnDetailMakeOffer);

        // Populate Views with Intent data
        tvIdBadge.setText("ID: " + productId);
        tvName.setText(productName);
        tvPrice.setText(productPrice);
        tvCategory.setText("Category: " + productCategory);
        tvCondition.setText(productCondition);
        tvSeller.setText("Listed by: " + productSeller);
        tvDescription.setText(productDescription);

        // Navigation click listeners
        btnBack.setOnClickListener(v -> handleNavigateBack());
        btnBackHome.setOnClickListener(v -> handleNavigateBack());

        btnContactSeller.setOnClickListener(v -> {
            Log.d(TAG, "ProductDetailsActivity: Contact seller clicked for " + productSeller);
            Toast.makeText(this, "Connecting to seller: " + productSeller + " (Feature in Stage 2)", Toast.LENGTH_SHORT).show();
        });

        btnMakeOffer.setOnClickListener(v -> {
            Log.d(TAG, "ProductDetailsActivity: Make offer clicked for " + productName);
            Toast.makeText(this, "Offer feature for " + productName + " coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Register custom Back-press dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleNavigateBack();
            }
        });
    }

    private void handleNavigateBack() {
        Log.d(TAG, "ProductDetailsActivity: Navigating back to parent catalog");
        finish(); // Finishes current Activity and smoothly returns to caller
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "ProductDetailsActivity: onStart() - Activity visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ProductDetailsActivity: onResume() - Activity focused");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ProductDetailsActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "ProductDetailsActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "ProductDetailsActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ProductDetailsActivity: onDestroy() - Activity destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "ProductDetailsActivity: onSaveInstanceState() - Preserving product state");
        outState.putString("product_id", productId);
        outState.putString("product_name", productName);
        outState.putString("product_price", productPrice);
    }
}
