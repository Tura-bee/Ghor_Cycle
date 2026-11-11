package com.example.myapplication;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Product model representing an item listed on the GhorCycle marketplace.
 *
 * Conforms to Cloud Firestore schema requirements:
 * 1. Default no-argument constructor for Firestore document deserialization.
 * 2. Strict field naming matching Firestore security rules and document paths.
 * 3. Backward-compatible getters for existing Activity and Adapter code.
 */
@IgnoreExtraProperties
public class Product implements Serializable {

    // Transaction Types
    public static final String TYPE_SELL = "Sell";
    public static final String TYPE_FREE = "Free";
    public static final String TYPE_DONATE = "Donate";
    public static final String TYPE_RECYCLE = "Recycle";

    // Status Types
    public static final String STATUS_AVAILABLE = "Available";
    public static final String STATUS_RESERVED = "Reserved";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_SOLD = "Sold";
    public static final String STATUS_CYCLED = "Cycled";

    private String productId;
    private String ownerId;
    private String title;
    private String description;
    private String category;
    private String condition;
    private String transactionType;
    private double price;
    private String location;
    private String imageUrl;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Transient UI helper fields
    private int imageResId;
    private boolean isFavorite;
    private String seller;

    /**
     * Required default constructor for Cloud Firestore deserialization.
     */
    public Product() {
        this.transactionType = TYPE_SELL;
        this.status = STATUS_AVAILABLE;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public Product(String productId, String ownerId, String title, String description,
                   String category, String condition, String transactionType, double price,
                   String location, String imageUrl, String status) {
        this.productId = productId;
        this.ownerId = ownerId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.transactionType = transactionType != null ? transactionType : (price <= 0.0 ? TYPE_FREE : TYPE_SELL);
        this.price = price;
        this.location = location;
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.status = status != null ? status : STATUS_AVAILABLE;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.isFavorite = false;
    }

    /**
     * Backward-compatible convenience constructor for sample data and Stage 2 adapters.
     */
    public Product(String productId, String title, String category, String condition,
                   double price, String seller, String location, String description,
                   int imageResId) {
        this.productId = productId;
        this.ownerId = "system-sample-owner";
        this.title = title;
        this.category = category;
        this.condition = condition;
        this.price = price;
        this.transactionType = price <= 0.0 ? TYPE_FREE : TYPE_SELL;
        this.seller = seller;
        this.location = location;
        this.description = description;
        this.imageResId = imageResId;
        this.imageUrl = "";
        this.status = STATUS_AVAILABLE;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
        this.isFavorite = false;
    }

    // --- Cloud Firestore Document Fields ---

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    public boolean isAvailable() {
        return status == null || STATUS_AVAILABLE.equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isReserved() {
        return STATUS_RESERVED.equalsIgnoreCase(status);
    }

    @Exclude
    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(status)
                || STATUS_SOLD.equalsIgnoreCase(status)
                || STATUS_CYCLED.equalsIgnoreCase(status);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- Backward Compatibility and UI Helpers ---

    @Exclude
    public String getId() {
        return productId;
    }

    @Exclude
    public void setId(String id) {
        this.productId = id;
    }

    @Exclude
    public String getName() {
        return title;
    }

    @Exclude
    public void setName(String name) {
        this.title = name;
    }

    @Exclude
    public String getSeller() {
        if (seller != null) {
            return seller;
        }
        return ownerId != null ? ownerId : "Community Member";
    }

    @Exclude
    public void setSeller(String seller) {
        this.seller = seller;
    }

    @Exclude
    public int getImageResId() {
        return imageResId;
    }

    @Exclude
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    @Exclude
    public boolean isFavorite() {
        return isFavorite;
    }

    @Exclude
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Exclude
    public boolean isFree() {
        return TYPE_FREE.equalsIgnoreCase(transactionType)
                || TYPE_DONATE.equalsIgnoreCase(transactionType)
                || TYPE_RECYCLE.equalsIgnoreCase(transactionType)
                || price <= 0.0;
    }

    @Exclude
    public void setFree(boolean free) {
        if (free) {
            this.transactionType = TYPE_FREE;
            this.price = 0.0;
        } else if (isFree()) {
            this.transactionType = TYPE_SELL;
        }
    }

    @Exclude
    public String getFormattedPrice() {
        if (isFree()) {
            return "FREE";
        }
        return String.format(Locale.US, "৳ %,.0f", price);
    }

    /**
     * Converts model to a Map representation for Firestore document operations.
     */
    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", productId);
        map.put("ownerId", ownerId);
        map.put("title", title);
        map.put("description", description);
        map.put("category", category);
        map.put("condition", condition);
        map.put("transactionType", transactionType);
        map.put("price", price);
        map.put("location", location);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("status", status != null ? status : STATUS_AVAILABLE);
        map.put("createdAt", createdAt != null ? createdAt : Timestamp.now());
        map.put("updatedAt", Timestamp.now());
        return map;
    }
}
