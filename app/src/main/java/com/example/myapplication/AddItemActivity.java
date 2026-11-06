package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

/**
 * AddItemActivity enables authenticated community members to list household goods.
 *
 * Implements:
 * 1. Authentication guard: Unauthenticated users are redirected to LoginActivity.
 * 2. Form input validation for item details.
 * 3. Explicit Intent construction to ProductDetailsActivity with custom user-entered data.
 * 4. Cancel/Back navigation to MainActivity.
 * 5. Complete Activity lifecycle logging.
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_Lifecycle";

    private EditText etTitle;
    private EditText etCategory;
    private EditText etPrice;
    private EditText etCondition;
    private EditText etSeller;
    private EditText etDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddItemActivity: onCreate() - Checking authentication status");

        // Requirement 8: Prevent unauthenticated users from accessing authenticated-only features
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "AddItemActivity: Unauthenticated user attempted access, redirecting to LoginActivity");
            Toast.makeText(this, "Please sign in to list items on GhorCycle.", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(AddItemActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_add_item);

        etTitle = findViewById(R.id.etItemTitle);
        etCategory = findViewById(R.id.etItemCategory);
        etPrice = findViewById(R.id.etItemPrice);
        etCondition = findViewById(R.id.etItemCondition);
        etSeller = findViewById(R.id.etItemSeller);
        etDescription = findViewById(R.id.etItemDescription);

        // Pre-fill seller with logged-in user profile
        if (etSeller != null) {
            String authorName = !TextUtils.isEmpty(currentUser.getDisplayName())
                    ? currentUser.getDisplayName()
                    : currentUser.getEmail();
            etSeller.setText(authorName);
        }

        Button btnBack = findViewById(R.id.btnAddItemBack);
        Button btnPublish = findViewById(R.id.btnPublishItem);
        Button btnCancel = findViewById(R.id.btnCancelAddItem);

        // Click listeners
        btnBack.setOnClickListener(v -> handleBackNavigation());
        btnCancel.setOnClickListener(v -> handleBackNavigation());
        btnPublish.setOnClickListener(v -> handlePublishItem());

        // Back press dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void handlePublishItem() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
        String price = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String condition = etCondition.getText() != null ? etCondition.getText().toString().trim() : "";
        String seller = etSeller.getText() != null ? etSeller.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Item title is required");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(category)) {
            etCategory.setError("Category is required");
            etCategory.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(price)) {
            etPrice.setError("Price is required (enter 0 for FREE)");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(condition)) {
            condition = "Gently Used";
        }

        if (TextUtils.isEmpty(seller)) {
            seller = "Community Member";
        }

        if (TextUtils.isEmpty(description)) {
            description = "Quality household item ready for a new home.";
        }

        String generatedId = "GC-USER-" + (System.currentTimeMillis() % 10000);
        boolean isFree = price.equals("0") || price.equalsIgnoreCase("free");
        String formattedPrice = isFree ? "FREE" : (price.startsWith("৳") ? price : "৳ " + price);

        Log.d(TAG, "AddItemActivity: Publishing new item ID=" + generatedId + ", Title=" + title);
        Toast.makeText(this, "Item listed successfully! Opening preview...", Toast.LENGTH_SHORT).show();

        // Pass newly created product data to ProductDetailsActivity via explicit Intent
        Intent detailIntent = new Intent(AddItemActivity.this, ProductDetailsActivity.class);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, generatedId);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_NAME, title);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_PRICE, formattedPrice);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY, category);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION, condition);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_SELLER, seller);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION, description);
        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IS_FREE, isFree);

        startActivity(detailIntent);
        finish(); // Finish AddItemActivity so back button from details returns to catalog
    }

    private void handleBackNavigation() {
        Log.d(TAG, "AddItemActivity: User cancelled listing, returning to MainActivity");
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "AddItemActivity: onStart() - Activity becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "AddItemActivity: onResume() - Activity focused and ready for input");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "AddItemActivity: onPause() - Activity pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "AddItemActivity: onStop() - Activity stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "AddItemActivity: onRestart() - Activity restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AddItemActivity: onDestroy() - Activity resources destroyed");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "AddItemActivity: onSaveInstanceState() - Saving pending form state");
        if (etTitle != null) outState.putString("saved_title", etTitle.getText().toString());
        if (etCategory != null) outState.putString("saved_category", etCategory.getText().toString());
        if (etPrice != null) outState.putString("saved_price", etPrice.getText().toString());
    }
}
