package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

/**
 * AddItemActivity enables authenticated users to create and edit product listings
 * in Cloud Firestore with system photo selection.
 *
 * Implements:
 * 1. Photo picker using Android's standard ActivityResultContracts.GetContent.
 * 2. Form input validation with strict constraints matching Firestore security rules.
 * 3. Dynamic price control based on transaction type (Sell vs Free/Donate/Recycle).
 * 4. Dual Create and Edit modes (with deletion support for listing owners).
 * 5. Asynchronous operation feedback with loading indicators.
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_AddItem";

    public static final String EXTRA_EDIT_PRODUCT = "extra_edit_product";

    private MaterialCardView cardImagePicker;
    private LinearLayout layoutPhotoPlaceholder;
    private ImageView ivSelectedPhoto;
    private TextView tvChangePhotoBadge;
    private TextView tvAddItemTitle;

    private EditText etTitle;
    private Spinner spinnerCategory;
    private Spinner spinnerTransactionType;
    private Spinner spinnerCondition;
    private EditText etPrice;
    private EditText etLocation;
    private EditText etDescription;
    private ProgressBar pbLoading;

    private Button btnPublish;
    private Button btnDelete;
    private Button btnCancel;
    private Button btnBack;

    private ActivityResultLauncher<String> photoPickerLauncher;
    private Uri selectedImageUri = null;

    private boolean isEditMode = false;
    private Product existingProduct = null;

    private static final String[] CATEGORIES = new String[]{
            SampleDataProvider.CATEGORY_FURNITURE,
            SampleDataProvider.CATEGORY_CLOTHES,
            SampleDataProvider.CATEGORY_BOOKS,
            SampleDataProvider.CATEGORY_KITCHEN,
            SampleDataProvider.CATEGORY_ELECTRONICS,
            SampleDataProvider.CATEGORY_PLASTIC,
            SampleDataProvider.CATEGORY_PAPER,
            SampleDataProvider.CATEGORY_METAL
    };

    private static final String[] TRANSACTION_TYPES = new String[]{
            Product.TYPE_SELL,
            Product.TYPE_FREE,
            Product.TYPE_DONATE,
            Product.TYPE_RECYCLE
    };

    private static final String[] CONDITIONS = new String[]{
            "Like New",
            "Gently Used",
            "Good Condition",
            "Recyclable / Salvage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AddItemActivity: onCreate() - Checking authentication status");

        // Guard: Prevent unauthenticated users from accessing listing creation
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "AddItemActivity: Unauthenticated user attempted access, redirecting to LoginActivity");
            Toast.makeText(this, "Please sign in to list or manage items on GhorCycle.", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(AddItemActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_add_item);

        bindViews();
        setupSpinners();
        setupPhotoPicker();
        checkForEditMode();
        setupActions();
        setupBackNavigation();
    }

    private void bindViews() {
        tvAddItemTitle = findViewById(R.id.tvAddItemTitle);
        cardImagePicker = findViewById(R.id.cardImagePicker);
        layoutPhotoPlaceholder = findViewById(R.id.layoutPhotoPlaceholder);
        ivSelectedPhoto = findViewById(R.id.ivSelectedPhoto);
        tvChangePhotoBadge = findViewById(R.id.tvChangePhotoBadge);

        etTitle = findViewById(R.id.etItemTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerTransactionType = findViewById(R.id.spinnerTransactionType);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        etPrice = findViewById(R.id.etItemPrice);
        etLocation = findViewById(R.id.etItemLocation);
        etDescription = findViewById(R.id.etItemDescription);
        pbLoading = findViewById(R.id.pbAddItemLoading);

        btnPublish = findViewById(R.id.btnPublishItem);
        btnDelete = findViewById(R.id.btnDeleteItem);
        btnCancel = findViewById(R.id.btnCancelAddItem);
        btnBack = findViewById(R.id.btnAddItemBack);
    }

    private void setupSpinners() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(catAdapter);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, TRANSACTION_TYPES);
        spinnerTransactionType.setAdapter(typeAdapter);

        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, CONDITIONS);
        spinnerCondition.setAdapter(condAdapter);

        // Dynamic price enablement based on transaction type
        spinnerTransactionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = TRANSACTION_TYPES[position];
                boolean isPaid = Product.TYPE_SELL.equalsIgnoreCase(selectedType);

                if (!isPaid) {
                    etPrice.setText("0");
                    etPrice.setEnabled(false);
                    etPrice.setAlpha(0.6f);
                } else {
                    if (etPrice.getText().toString().equals("0")) {
                        etPrice.setText("");
                    }
                    etPrice.setEnabled(true);
                    etPrice.setAlpha(1.0f);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupPhotoPicker() {
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Log.d(TAG, "AddItemActivity: Photo selected with URI: " + uri);

                        // Stage 6 Content Provider demonstration: resolve metadata and check stream accessibility
                        ContentProviderHelper.FileMetadata metadata = ContentProviderHelper.resolveUriMetadata(this, uri);
                        Log.d(TAG, "ContentProvider resolved: name=" + metadata.displayName
                                + ", size=" + metadata.getFormattedSize()
                                + ", accessible=" + metadata.isAccessible);

                        if (metadata.isAccessible) {
                            ivSelectedPhoto.setImageURI(uri);
                            ivSelectedPhoto.setVisibility(View.VISIBLE);
                            layoutPhotoPlaceholder.setVisibility(View.GONE);
                            tvChangePhotoBadge.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Attached: " + metadata.displayName + " (" + metadata.getFormattedSize() + ")", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Selected media could not be accessed. Please choose another file.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
        );

        cardImagePicker.setOnClickListener(v -> photoPickerLauncher.launch("image/*"));
    }

    private void checkForEditMode() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_EDIT_PRODUCT)) {
            existingProduct = (Product) getIntent().getSerializableExtra(EXTRA_EDIT_PRODUCT);
            if (existingProduct != null) {
                isEditMode = true;
                tvAddItemTitle.setText("Edit Listing");
                btnPublish.setText("Save Changes");
                btnDelete.setVisibility(View.VISIBLE);

                // Populate form fields
                etTitle.setText(existingProduct.getTitle());
                etPrice.setText(String.valueOf((int) existingProduct.getPrice()));
                etLocation.setText(existingProduct.getLocation());
                etDescription.setText(existingProduct.getDescription());

                // Set spinners
                setSpinnerSelection(spinnerCategory, CATEGORIES, existingProduct.getCategory());
                setSpinnerSelection(spinnerTransactionType, TRANSACTION_TYPES, existingProduct.getTransactionType());
                setSpinnerSelection(spinnerCondition, CONDITIONS, existingProduct.getCondition());

                // Handle existing image reference
                if (!TextUtils.isEmpty(existingProduct.getImageUrl())) {
                    try {
                        Uri uri = Uri.parse(existingProduct.getImageUrl());
                        ivSelectedPhoto.setImageURI(uri);
                        ivSelectedPhoto.setVisibility(View.VISIBLE);
                        layoutPhotoPlaceholder.setVisibility(View.GONE);
                        tvChangePhotoBadge.setVisibility(View.VISIBLE);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    private void setSpinnerSelection(Spinner spinner, String[] array, String value) {
        if (value == null) return;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> handleBackNavigation());
        btnCancel.setOnClickListener(v -> handleBackNavigation());

        btnPublish.setOnClickListener(v -> handlePublish());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Listing")
                    .setMessage("Are you sure you want to permanently delete this listing?")
                    .setPositiveButton("Delete", (dialog, which) -> handleDelete())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void handlePublish() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
        String transactionType = spinnerTransactionType.getSelectedItem() != null ? spinnerTransactionType.getSelectedItem().toString() : Product.TYPE_SELL;
        String condition = spinnerCondition.getSelectedItem() != null ? spinnerCondition.getSelectedItem().toString() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Client-side validations
        if (title.length() < 2 || title.length() > 120) {
            etTitle.setError("Title must be between 2 and 120 characters.");
            etTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            etLocation.setError("Location / area is required.");
            etLocation.requestFocus();
            return;
        }

        if (description.length() < 3 || description.length() > 2000) {
            etDescription.setError("Description must be between 3 and 2000 characters.");
            etDescription.requestFocus();
            return;
        }

        double price = 0.0;
        if (Product.TYPE_SELL.equalsIgnoreCase(transactionType)) {
            if (TextUtils.isEmpty(priceStr)) {
                etPrice.setError("Price is required for selling items.");
                etPrice.requestFocus();
                return;
            }
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    etPrice.setError("Price must be greater than 0 for items for sale.");
                    etPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etPrice.setError("Please enter a valid numeric price.");
                etPrice.requestFocus();
                return;
            }
        }

        setLoading(true);

        if (isEditMode && existingProduct != null) {
            existingProduct.setTitle(title);
            existingProduct.setCategory(category);
            existingProduct.setTransactionType(transactionType);
            existingProduct.setCondition(condition);
            existingProduct.setPrice(price);
            existingProduct.setLocation(location);
            existingProduct.setDescription(description);

            FirestoreProductService.updateProduct(existingProduct, selectedImageUri, new FirestoreProductService.ProductOperationCallback() {
                @Override
                public void onSuccess(Product updatedProduct) {
                    setLoading(false);
                    Toast.makeText(AddItemActivity.this, "Listing updated successfully!", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_EDIT_PRODUCT, updatedProduct);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(AddItemActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Product newProduct = new Product(
                    null,
                    null,
                    title,
                    description,
                    category,
                    condition,
                    transactionType,
                    price,
                    location,
                    null,
                    Product.STATUS_AVAILABLE
            );

            FirestoreProductService.createProduct(newProduct, selectedImageUri, this, new FirestoreProductService.ProductOperationCallback() {
                @Override
                public void onSuccess(Product createdProduct) {
                    setLoading(false);
                    Toast.makeText(AddItemActivity.this, "Item published to GhorCycle marketplace!", Toast.LENGTH_SHORT).show();

                    // Open details preview of created item
                    Intent detailIntent = new Intent(AddItemActivity.this, ProductDetailsActivity.class);
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_ID, createdProduct.getProductId());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_NAME, createdProduct.getTitle());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_PRICE, createdProduct.getFormattedPrice());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY, createdProduct.getCategory());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION, createdProduct.getCondition());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_SELLER, "You (" + createdProduct.getLocation() + ")");
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION, createdProduct.getDescription());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IS_FREE, createdProduct.isFree());
                    detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_OWNER_ID, createdProduct.getOwnerId());
                    if (!TextUtils.isEmpty(createdProduct.getImageUrl())) {
                        detailIntent.putExtra(ProductDetailsActivity.EXTRA_PRODUCT_IMAGE_URL, createdProduct.getImageUrl());
                    }
                    startActivity(detailIntent);
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(AddItemActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void handleDelete() {
        if (existingProduct == null) return;

        setLoading(true);
        FirestoreProductService.deleteProduct(existingProduct.getProductId(), existingProduct.getOwnerId(), new FirestoreProductService.ProductDeleteCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(AddItemActivity.this, "Listing deleted.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_FIRST_USER); // Indicates deletion
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                setLoading(false);
                Toast.makeText(AddItemActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (pbLoading != null) {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnPublish.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        if (btnDelete != null) btnDelete.setEnabled(!isLoading);
    }

    private void handleBackNavigation() {
        finish();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }
}
