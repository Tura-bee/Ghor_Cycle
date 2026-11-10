package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;

/**
 * ProductDetailsActivity displays comprehensive details for an item in GhorCycle.
 *
 * Implements:
 * 1. Extraction and presentation of Firestore document fields.
 * 2. Visual handling of priced vs FREE community items.
 * 3. Dynamic category icon or custom photo display.
 * 4. Strict owner protection: Only listing owner can see and use Edit & Delete actions.
 * 5. Public privacy: Never exposes private user credentials or phone numbers.
 * 6. Interactive click listeners for buyer-seller workflows.
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
    public static final String EXTRA_PRODUCT_IMAGE_RES = "com.ghorcycle.extra.PRODUCT_IMAGE_RES";
    public static final String EXTRA_PRODUCT_IS_FREE = "com.ghorcycle.extra.PRODUCT_IS_FREE";
    public static final String EXTRA_PRODUCT_OWNER_ID = "com.ghorcycle.extra.PRODUCT_OWNER_ID";
    public static final String EXTRA_PRODUCT_IMAGE_URL = "com.ghorcycle.extra.PRODUCT_IMAGE_URL";

    private String productId;
    private String productName;
    private String productPrice;
    private String productCategory;
    private String productSeller;
    private String productCondition;
    private String productDescription;
    private int productImageRes = 0;
    private boolean isProductFree = false;
    private String ownerId = "";
    private String imageUrl = "";

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ProductDetailsActivity: onCreate() - Extracting Intent extras and inflating UI");
        setContentView(R.layout.activity_product_details);

        // Register edit launcher to refresh on edit/delete
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null && result.getData().hasExtra(AddItemActivity.EXTRA_EDIT_PRODUCT)) {
                            Product updated = (Product) result.getData().getSerializableExtra(AddItemActivity.EXTRA_EDIT_PRODUCT);
                            if (updated != null) {
                                refreshUIWithProduct(updated);
                            }
                        }
                    } else if (result.getResultCode() == RESULT_FIRST_USER) {
                        // Deleted
                        finish();
                    }
                }
        );

        // Extract extras passed from caller Intent
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
            productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
            productPrice = getIntent().getStringExtra(EXTRA_PRODUCT_PRICE);
            productCategory = getIntent().getStringExtra(EXTRA_PRODUCT_CATEGORY);
            productSeller = getIntent().getStringExtra(EXTRA_PRODUCT_SELLER);
            productCondition = getIntent().getStringExtra(EXTRA_PRODUCT_CONDITION);
            productDescription = getIntent().getStringExtra(EXTRA_PRODUCT_DESCRIPTION);
            productImageRes = getIntent().getIntExtra(EXTRA_PRODUCT_IMAGE_RES, 0);
            isProductFree = getIntent().getBooleanExtra(EXTRA_PRODUCT_IS_FREE, false);
            ownerId = getIntent().getStringExtra(EXTRA_PRODUCT_OWNER_ID);
            imageUrl = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE_URL);
        }

        // Apply fallbacks if any extra was not supplied
        if (TextUtils.isEmpty(productId)) productId = "GC-DEMO-00";
        if (TextUtils.isEmpty(productName)) productName = "GhorCycle Community Item";
        if (TextUtils.isEmpty(productPrice)) productPrice = "৳ 0";
        if (TextUtils.isEmpty(productCategory)) productCategory = "General";
        if (TextUtils.isEmpty(productSeller)) productSeller = "Dhanmondi, Dhaka";
        if (TextUtils.isEmpty(productCondition)) productCondition = "Pre-loved";
        if (TextUtils.isEmpty(productDescription)) {
            productDescription = "Quality verified household good looking for a second life.";
        }

        if (productPrice.equalsIgnoreCase("FREE") || productPrice.equalsIgnoreCase("৳ 0") || isProductFree) {
            isProductFree = true;
            productPrice = "FREE";
        }

        Log.d(TAG, "ProductDetailsActivity: Displaying item [ID=" + productId + ", Name=" + productName + "]");

        bindViewsAndPopulate();
        setupOwnerAndBuyerActions();

        // Register custom Back-press dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleNavigateBack();
            }
        });
    }

    private void bindViewsAndPopulate() {
        TextView tvIdBadge = findViewById(R.id.tvDetailIdBadge);
        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        TextView tvFreeBadge = findViewById(R.id.tvDetailFreeBadge);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvCondition = findViewById(R.id.tvDetailCondition);
        TextView tvSeller = findViewById(R.id.tvDetailSeller);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        ImageView ivImage = findViewById(R.id.ivDetailProductImage);

        Button btnBack = findViewById(R.id.btnDetailBack);
        Button btnBackHome = findViewById(R.id.btnDetailBackHome);

        tvIdBadge.setText("ID: " + productId);
        tvName.setText(productName);
        tvCategory.setText("Category: " + productCategory);
        tvCondition.setText(productCondition);
        tvSeller.setText("Location: " + productSeller);
        tvDescription.setText(productDescription);

        // Price & Free formatting
        if (isProductFree) {
            tvPrice.setText("FREE");
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.eco_free_badge));
            tvFreeBadge.setVisibility(View.VISIBLE);
        } else {
            tvPrice.setText(productPrice);
            tvPrice.setTextColor(ContextCompat.getColor(this, R.color.eco_green_primary));
            tvFreeBadge.setVisibility(View.GONE);
        }

        // Image handling: custom photo URI or category placeholder
        if (!TextUtils.isEmpty(imageUrl)) {
            try {
                ivImage.setImageURI(Uri.parse(imageUrl));
                ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                ivImage.setImageResource(FirestoreProductService.getCategoryIconRes(productCategory));
            }
        } else if (productImageRes != 0) {
            ivImage.setImageResource(productImageRes);
        } else {
            ivImage.setImageResource(FirestoreProductService.getCategoryIconRes(productCategory));
        }
        ivImage.setContentDescription("Photo and illustration for " + productName);

        btnBack.setOnClickListener(v -> handleNavigateBack());
        btnBackHome.setOnClickListener(v -> handleNavigateBack());
    }

    private void setupOwnerAndBuyerActions() {
        LinearLayout layoutOwnerActions = findViewById(R.id.layoutOwnerActions);
        LinearLayout layoutBuyerActions = findViewById(R.id.layoutBuyerActions);

        Button btnEditListing = findViewById(R.id.btnDetailEditListing);
        Button btnDeleteListing = findViewById(R.id.btnDetailDeleteListing);
        Button btnContactSeller = findViewById(R.id.btnDetailContactSeller);
        Button btnMakeOffer = findViewById(R.id.btnDetailMakeOffer);

        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        boolean isOwner = currentUser != null && !TextUtils.isEmpty(ownerId) && currentUser.getUid().equals(ownerId);

        if (isOwner) {
            layoutOwnerActions.setVisibility(View.VISIBLE);
            layoutBuyerActions.setVisibility(View.GONE);

            btnEditListing.setOnClickListener(v -> {
                Log.d(TAG, "ProductDetailsActivity: Owner opening Edit mode for ID=" + productId);
                Product editProduct = new Product(
                        productId,
                        ownerId,
                        productName,
                        productDescription,
                        productCategory,
                        productCondition,
                        isProductFree ? Product.TYPE_FREE : Product.TYPE_SELL,
                        isProductFree ? 0.0 : parsePrice(productPrice),
                        productSeller,
                        imageUrl,
                        Product.STATUS_AVAILABLE
                );

                Intent editIntent = new Intent(ProductDetailsActivity.this, AddItemActivity.class);
                editIntent.putExtra(AddItemActivity.EXTRA_EDIT_PRODUCT, editProduct);
                editLauncher.launch(editIntent);
            });

            btnDeleteListing.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Listing")
                        .setMessage("Are you sure you want to permanently delete this listing?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirestoreProductService.deleteProduct(productId, ownerId, new FirestoreProductService.ProductDeleteCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ProductDetailsActivity.this, "Listing deleted.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        } else {
            layoutOwnerActions.setVisibility(View.GONE);
            layoutBuyerActions.setVisibility(View.VISIBLE);

            btnContactSeller.setOnClickListener(v -> {
                Log.d(TAG, "ProductDetailsActivity: Contact seller clicked");
                Toast.makeText(this, "Connecting to seller... (Messaging feature coming soon)", Toast.LENGTH_SHORT).show();
            });

            btnMakeOffer.setOnClickListener(v -> {
                Log.d(TAG, "ProductDetailsActivity: Make offer clicked for " + productName);
                Toast.makeText(this, "Offer feature for " + productName + " coming soon!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private double parsePrice(String priceString) {
        if (TextUtils.isEmpty(priceString)) return 0.0;
        try {
            String clean = priceString.replaceAll("[^0-9.]", "");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void refreshUIWithProduct(Product p) {
        this.productName = p.getTitle();
        this.productCategory = p.getCategory();
        this.productCondition = p.getCondition();
        this.productPrice = p.getFormattedPrice();
        this.productSeller = p.getLocation();
        this.productDescription = p.getDescription();
        this.isProductFree = p.isFree();
        this.imageUrl = p.getImageUrl();

        bindViewsAndPopulate();
        setupOwnerAndBuyerActions();
    }

    private void handleNavigateBack() {
        Log.d(TAG, "ProductDetailsActivity: Navigating back to parent catalog");
        finish();
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
        outState.putBoolean("product_is_free", isProductFree);
        outState.putString("product_owner_id", ownerId);
        outState.putString("product_image_url", imageUrl);
    }
}
