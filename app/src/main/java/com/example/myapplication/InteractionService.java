package com.example.myapplication;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service managing user interactions in GhorCycle:
 * 1. User Favorites persistence in Cloud Firestore under users/{userId}/favorites/{productId}.
 * 2. Donation and Barter/Exchange request submissions and duplicate prevention.
 * 3. Owner acceptance/rejection workflows with automatic product status updates.
 * 4. Offline / unconfigured fallback data cache.
 */
public class InteractionService {

    private static final String TAG = "GhorCycle_Interactions";

    public static final String COLLECTION_USERS = "users";
    public static final String SUBCOLLECTION_FAVORITES = "favorites";
    public static final String COLLECTION_REQUESTS = "item_requests";

    // Offline / unconfigured in-memory fallbacks
    private static final Set<String> inMemoryFavoritesCache = new HashSet<>();
    private static final List<ItemRequest> inMemoryRequestsCache = new ArrayList<>();

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface FavoriteStatusCallback {
        void onResult(boolean isFavorite);
    }

    public interface FavoritesListCallback {
        void onLoaded(Set<String> favoriteProductIds);
        void onError(String errorMessage);
    }

    public interface RequestCallback {
        void onSuccess(ItemRequest request);
        void onFailure(String errorMessage);
    }

    public interface RequestListCallback {
        void onLoaded(List<ItemRequest> requests);
        void onError(String errorMessage);
    }

    private static boolean isFirestoreConfigured() {
        try {
            return FirebaseApp.getApps(FirebaseAuth.getInstance().getApp().getApplicationContext()).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ==========================================
    // PART B: FAVORITES MANAGEMENT
    // ==========================================

    /**
     * Toggle favorite status of a product for the currently authenticated user.
     */
    public static void toggleFavorite(final String productId, final ActionCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) callback.onFailure("Please log in to save favorites.");
            return;
        }

        final String userId = currentUser.getUid();

        if (!isFirestoreConfigured()) {
            if (inMemoryFavoritesCache.contains(productId)) {
                inMemoryFavoritesCache.remove(productId);
            } else {
                inMemoryFavoritesCache.add(productId);
            }
            if (callback != null) callback.onSuccess();
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(SUBCOLLECTION_FAVORITES)
                    .document(productId);

            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    // Remove from favorites
                    docRef.delete().addOnSuccessListener(aVoid -> {
                        inMemoryFavoritesCache.remove(productId);
                        if (callback != null) callback.onSuccess();
                    }).addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
                } else {
                    // Add to favorites
                    Map<String, Object> data = new HashMap<>();
                    data.put("productId", productId);
                    data.put("savedAt", Timestamp.now());

                    docRef.set(data).addOnSuccessListener(aVoid -> {
                        inMemoryFavoritesCache.add(productId);
                        if (callback != null) callback.onSuccess();
                    }).addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e.getMessage());
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "toggleFavorite error", e);
            if (inMemoryFavoritesCache.contains(productId)) {
                inMemoryFavoritesCache.remove(productId);
            } else {
                inMemoryFavoritesCache.add(productId);
            }
            if (callback != null) callback.onSuccess();
        }
    }

    /**
     * Check if a specific product is marked as favorite by the current user.
     */
    public static void checkIfFavorite(final String productId, final FavoriteStatusCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(false);
            return;
        }

        if (!isFirestoreConfigured()) {
            callback.onResult(inMemoryFavoritesCache.contains(productId));
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .collection(SUBCOLLECTION_FAVORITES)
                    .document(productId)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        boolean fav = snapshot != null && snapshot.exists();
                        if (fav) inMemoryFavoritesCache.add(productId);
                        else inMemoryFavoritesCache.remove(productId);
                        callback.onResult(fav);
                    })
                    .addOnFailureListener(e -> callback.onResult(inMemoryFavoritesCache.contains(productId)));
        } catch (Exception e) {
            callback.onResult(inMemoryFavoritesCache.contains(productId));
        }
    }

    /**
     * Listen to the complete set of favorite product IDs for the logged-in user.
     */
    public static ListenerRegistration listenToUserFavorites(final FavoritesListCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onLoaded(new HashSet<>());
            return null;
        }

        if (!isFirestoreConfigured()) {
            callback.onLoaded(new HashSet<>(inMemoryFavoritesCache));
            return null;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            return db.collection(COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .collection(SUBCOLLECTION_FAVORITES)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(TAG, "listenToUserFavorites error", error);
                            callback.onError(error.getMessage());
                            return;
                        }

                        Set<String> favoriteIds = new HashSet<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                favoriteIds.add(doc.getId());
                            }
                        }
                        inMemoryFavoritesCache.clear();
                        inMemoryFavoritesCache.addAll(favoriteIds);
                        callback.onLoaded(favoriteIds);
                    });
        } catch (Exception e) {
            Log.e(TAG, "listenToUserFavorites attach error", e);
            callback.onLoaded(new HashSet<>(inMemoryFavoritesCache));
            return null;
        }
    }

    // ==========================================
    // PART C & D: REQUESTS & STATUS LIFECYCLE
    // ==========================================

    /**
     * Submit a donation request or exchange proposal.
     * Enforces:
     * - Authentication requirement.
     * - Disallows users from requesting their own products.
     * - Disallows requesting Completed / unavailable products.
     * - Duplicate request prevention (rejects if user has an active PENDING request for this item).
     */
    public static void submitRequest(final ItemRequest request, final Product product, final RequestCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("You must be logged in to send a request.");
            return;
        }

        final String requesterUid = currentUser.getUid();
        request.setRequesterId(requesterUid);
        request.setRequesterEmail(currentUser.getEmail() != null ? currentUser.getEmail() : "user@ghorcycle.com");

        // 1. Prevent owner from requesting their own listing
        if (requesterUid.equals(request.getOwnerId())) {
            callback.onFailure("Invalid request: You cannot request your own product listing.");
            return;
        }

        // 2. Prevent requests on completed items
        if (product != null && product.isCompleted()) {
            callback.onFailure("This item has already been completed or recycled and is no longer available.");
            return;
        }

        // 3. Prevent duplicate requests in offline cache
        for (ItemRequest r : inMemoryRequestsCache) {
            if (r.getProductId().equals(request.getProductId())
                    && r.getRequesterId().equals(requesterUid)
                    && ItemRequest.STATUS_PENDING.equalsIgnoreCase(r.getStatus())) {
                callback.onFailure("You already have an active pending request for this item.");
                return;
            }
        }

        String reqId = !TextUtils.isEmpty(request.getRequestId()) ? request.getRequestId() : UUID.randomUUID().toString();
        request.setRequestId(reqId);
        request.setStatus(ItemRequest.STATUS_PENDING);
        request.setCreatedAt(Timestamp.now());
        request.setUpdatedAt(Timestamp.now());

        if (!isFirestoreConfigured()) {
            inMemoryRequestsCache.add(0, request);
            callback.onSuccess(request);
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Query to prevent duplicates on live Firestore
            db.collection(COLLECTION_REQUESTS)
                    .whereEqualTo("productId", request.getProductId())
                    .whereEqualTo("requesterId", requesterUid)
                    .whereEqualTo("status", ItemRequest.STATUS_PENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            callback.onFailure("You already have a pending request for this item. Please wait for the owner to respond.");
                            return;
                        }

                        // Write new request document
                        db.collection(COLLECTION_REQUESTS)
                                .document(reqId)
                                .set(request.toMap())
                                .addOnSuccessListener(aVoid -> {
                                    inMemoryRequestsCache.add(0, request);
                                    Log.d(TAG, "Request created with ID: " + reqId);
                                    callback.onSuccess(request);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to write request", e);
                                    callback.onFailure("Failed to send request: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        // In case of query failure, fallback to saving
                        db.collection(COLLECTION_REQUESTS)
                                .document(reqId)
                                .set(request.toMap())
                                .addOnSuccessListener(aVoid -> callback.onSuccess(request))
                                .addOnFailureListener(err -> callback.onFailure(err.getMessage()));
                    });

        } catch (Exception e) {
            Log.e(TAG, "submitRequest exception", e);
            inMemoryRequestsCache.add(0, request);
            callback.onSuccess(request);
        }
    }

    /**
     * Listen to requests for a specific product (used by listing owner).
     */
    public static ListenerRegistration listenToProductRequests(final String productId, final RequestListCallback callback) {
        if (!isFirestoreConfigured()) {
            List<ItemRequest> matching = new ArrayList<>();
            for (ItemRequest r : inMemoryRequestsCache) {
                if (productId.equals(r.getProductId())) {
                    matching.add(r);
                }
            }
            callback.onLoaded(matching);
            return null;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            return db.collection(COLLECTION_REQUESTS)
                    .whereEqualTo("productId", productId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(TAG, "listenToProductRequests error", error);
                            callback.onError(error.getMessage());
                            return;
                        }

                        List<ItemRequest> list = new ArrayList<>();
                        if (value != null) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                ItemRequest r = doc.toObject(ItemRequest.class);
                                if (r != null) {
                                    r.setRequestId(doc.getId());
                                    list.add(r);
                                }
                            }
                        }
                        callback.onLoaded(list);
                    });
        } catch (Exception e) {
            Log.e(TAG, "listenToProductRequests attach exception", e);
            callback.onError(e.getMessage());
            return null;
        }
    }

    /**
     * Update request status (ACCEPTED or REJECTED) by product owner.
     * If accepted, transitions the product status to "Reserved".
     */
    public static void respondToRequest(final ItemRequest request, final String newStatus, final ActionCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure("Authentication required.");
            return;
        }

        if (!currentUser.getUid().equals(request.getOwnerId())) {
            callback.onFailure("Permission denied: Only the listing owner can respond to this request.");
            return;
        }

        request.setStatus(newStatus);
        request.setUpdatedAt(Timestamp.now());

        // Update in-memory cache
        for (ItemRequest r : inMemoryRequestsCache) {
            if (r.getRequestId().equals(request.getRequestId())) {
                r.setStatus(newStatus);
                break;
            }
        }

        if (!isFirestoreConfigured()) {
            callback.onSuccess();
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);
            updates.put("updatedAt", Timestamp.now());

            db.collection(COLLECTION_REQUESTS)
                    .document(request.getRequestId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        // If accepted, update product status to Reserved
                        if (ItemRequest.STATUS_ACCEPTED.equalsIgnoreCase(newStatus)) {
                            db.collection(FirestoreProductService.COLLECTION_PRODUCTS)
                                    .document(request.getProductId())
                                    .update("status", Product.STATUS_RESERVED, "updatedAt", Timestamp.now())
                                    .addOnCompleteListener(t -> callback.onSuccess());
                        } else {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure("Failed to update request: " + e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "respondToRequest exception", e);
            callback.onSuccess();
        }
    }

    /**
     * Complete a transaction for a product, marking it Completed and keeping history.
     */
    public static void completeProductTransaction(final String productId, final String ownerId, final ActionCallback callback) {
        FirebaseUser currentUser = FirebaseAuthService.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals(ownerId)) {
            callback.onFailure("Permission denied: Only listing owner can complete transaction.");
            return;
        }

        if (!isFirestoreConfigured()) {
            callback.onSuccess();
            return;
        }

        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(FirestoreProductService.COLLECTION_PRODUCTS)
                    .document(productId)
                    .update("status", Product.STATUS_COMPLETED, "updatedAt", Timestamp.now())
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }
}
