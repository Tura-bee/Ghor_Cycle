package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service managing Cloud Firestore CRUD operations and image reference handling for GhorCycle.
 *
 * Implements:
 * 1. Create, Read, Update, and Delete (CRUD) on the "products" collection.
 * 2. Strict owner validation checks before update/delete operations.
 * 3. Graceful fallback for Firebase Storage (stores local content URI if Storage bucket is not provisioned).
 * 4. Offline / unconfigured fallback to in-memory dataset when google-services.json is pending.
 */
public class FirestoreProductService {

    private static final String TAG = "GhorCycle_Firestore";
    public static final String COLLECTION_PRODUCTS = "products";

    // In-memory fallback cache when Cloud Firestore is offline or unconfigured
    private static final List<Product> inMemoryFallbackCache = new ArrayList<>();

    static {
        // Initialize fallback cache with sample items
        inMemoryFallbackCache.addAll(SampleDataProvider.getSampleProducts());
    }

    public interface ProductOperationCallback {
        void onSuccess(Product product);
        void onFailure(String errorMessage);
    }

    public interface ProductListCallback {
        void onLoaded(List<Product> products);
        void onError(String errorMessage);
    }

    public interface ProductDeleteCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public static boolean isFirestoreConfigured() {
        try {
            return FirebaseApp.getApps(FirebaseAuth.getInstance().getApp().getApplicationContext()).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create a new product listing in Cloud Firestore.
     */
    public static void createProduct(final Product product, final Uri imageUri, final Context context, final ProductOperationCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("You must be logged in to create a product listing.");
            return;
        }

        // Set ownership and IDs
        String id = !TextUtils.isEmpty(product.getProductId()) ? product.getProductId() : UUID.randomUUID().toString();
        product.setProductId(id);
        product.setOwnerId(currentUser.getUid());
        product.setCreatedAt(Timestamp.now());
        product.setUpdatedAt(Timestamp.now());

        if (!isFirestoreConfigured()) {
            Log.w(TAG, "Firestore is unconfigured. Storing in local cache.");
            if (imageUri != null) {
                product.setImageUrl(imageUri.toString());
            }
            inMemoryFallbackCache.add(0, product);
            callback.onSuccess(product);
            return;
        }

        // Check if image upload to Firebase Storage is possible
        if (imageUri != null) {
            uploadImageIfPossible(imageUri, id, new ImageUploadCallback() {
                @Override
                public void onComplete(String imageUrl) {
                    product.setImageUrl(imageUrl);
                    saveDocumentToFirestore(product, callback);
                }
            });
        } else {
            saveDocumentToFirestore(product, callback);
        }
    }

    private static void saveDocumentToFirestore(final Product product, final ProductOperationCallback callback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(COLLECTION_PRODUCTS)
                    .document(product.getProductId())
                    .set(product.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Product created in Firestore with ID: " + product.getProductId());
                        callback.onSuccess(product);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Firestore product creation failed", e);
                        callback.onFailure("Failed to create product listing: " + e.getMessage());
                    });
        } catch (Exception e) {
            Log.e(TAG, "Firestore write exception", e);
            inMemoryFallbackCache.add(0, product);
            callback.onSuccess(product);
        }
    }

    /**
     * Update an existing product listing.
     * Enforces that only the owner can modify the document.
     */
    public static void updateProduct(final Product product, final Uri newImageUri, final ProductOperationCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("You must be signed in to edit this product.");
            return;
        }

        // Enforce ownership check
        if (!currentUser.getUid().equals(product.getOwnerId())) {
            callback.onFailure("Permission denied: You can only edit your own listings.");
            return;
        }

        product.setUpdatedAt(Timestamp.now());

        if (!isFirestoreConfigured()) {
            for (int i = 0; i < inMemoryFallbackCache.size(); i++) {
                if (inMemoryFallbackCache.get(i).getProductId().equals(product.getProductId())) {
                    if (newImageUri != null) {
                        product.setImageUrl(newImageUri.toString());
                    }
                    inMemoryFallbackCache.set(i, product);
                    callback.onSuccess(product);
                    return;
                }
            }
            callback.onFailure("Product not found in local cache.");
            return;
        }

        if (newImageUri != null) {
            uploadImageIfPossible(newImageUri, product.getProductId(), new ImageUploadCallback() {
                @Override
                public void onComplete(String imageUrl) {
                    product.setImageUrl(imageUrl);
                    commitProductUpdate(product, callback);
                }
            });
        } else {
            commitProductUpdate(product, callback);
        }
    }

    private static void commitProductUpdate(final Product product, final ProductOperationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = product.toMap();

        db.collection(COLLECTION_PRODUCTS)
                .document(product.getProductId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product updated in Firestore with ID: " + product.getProductId());
                    callback.onSuccess(product);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update product", e);
                    callback.onFailure("Update failed: " + e.getMessage());
                });
    }

    /**
     * Delete an existing product listing.
     * Enforces that only the owner can delete the document.
     */
    public static void deleteProduct(final String productId, final String ownerId, final ProductDeleteCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("You must be signed in to delete this listing.");
            return;
        }

        // Enforce ownership check
        if (!currentUser.getUid().equals(ownerId)) {
            callback.onFailure("Permission denied: You can only delete your own listings.");
            return;
        }

        if (!isFirestoreConfigured()) {
            for (int i = 0; i < inMemoryFallbackCache.size(); i++) {
                if (inMemoryFallbackCache.get(i).getProductId().equals(productId)) {
                    inMemoryFallbackCache.remove(i);
                    callback.onSuccess();
                    return;
                }
            }
            callback.onFailure("Product not found in local cache.");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Product deleted from Firestore ID: " + productId);
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete product from Firestore", e);
                    callback.onFailure("Delete failed: " + e.getMessage());
                });
    }

    /**
     * Listen for real-time updates on products from Cloud Firestore.
     */
    public static ListenerRegistration listenToProducts(final ProductListCallback callback) {
        if (!isFirestoreConfigured()) {
            callback.onLoaded(new ArrayList<>(inMemoryFallbackCache));
            return null;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            return db.collection(COLLECTION_PRODUCTS)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Firestore listen error", error);
                            callback.onError("Failed to load products: " + error.getMessage());
                            return;
                        }

                        if (value != null) {
                            List<Product> products = new ArrayList<>();
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                Product p = doc.toObject(Product.class);
                                if (p != null) {
                                    p.setProductId(doc.getId());
                                    // Map category to a fallback icon if none set
                                    p.setImageResId(getCategoryIconRes(p.getCategory()));
                                    products.add(p);
                                }
                            }

                            // If Firestore collection has no items yet, merge with sample items
                            if (products.isEmpty()) {
                                products.addAll(inMemoryFallbackCache);
                            }
                            callback.onLoaded(products);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error attaching Firestore listener", e);
            callback.onLoaded(new ArrayList<>(inMemoryFallbackCache));
            return null;
        }
    }

    /**
     * Get category icon resource ID for visual presentation.
     */
    public static int getCategoryIconRes(String category) {
        if (category == null) return R.drawable.ic_category_all;
        switch (category) {
            case SampleDataProvider.CATEGORY_FURNITURE:
                return R.drawable.ic_category_furniture;
            case SampleDataProvider.CATEGORY_CLOTHES:
                return R.drawable.ic_category_clothes;
            case SampleDataProvider.CATEGORY_BOOKS:
                return R.drawable.ic_category_books;
            case SampleDataProvider.CATEGORY_KITCHEN:
                return R.drawable.ic_category_kitchen;
            case SampleDataProvider.CATEGORY_ELECTRONICS:
                return R.drawable.ic_category_electronics;
            case SampleDataProvider.CATEGORY_PLASTIC:
                return R.drawable.ic_category_plastic;
            case SampleDataProvider.CATEGORY_PAPER:
                return R.drawable.ic_category_paper;
            case SampleDataProvider.CATEGORY_METAL:
                return R.drawable.ic_category_metal;
            default:
                return R.drawable.ic_category_all;
        }
    }

    private interface ImageUploadCallback {
        void onComplete(String imageUrl);
    }

    /**
     * Helper to safely attempt Firebase Storage upload or fallback to local content URI string.
     */
    private static void uploadImageIfPossible(final Uri imageUri, final String productId, final ImageUploadCallback callback) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference ref = storage.getReference().child("products/" + productId + ".jpg");

            ref.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUri -> callback.onComplete(downloadUri.toString()))
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Firebase Storage upload unavailable. Using local URI reference: " + e.getMessage());
                        callback.onComplete(imageUri.toString());
                    });
        } catch (Exception e) {
            Log.w(TAG, "Firebase Storage not provisioned. Storing local URI reference.");
            callback.onComplete(imageUri.toString());
        }
    }
}
