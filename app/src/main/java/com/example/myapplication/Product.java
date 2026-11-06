package com.example.myapplication;

import java.io.Serializable;
import java.util.Locale;

/**
 * Product model representing an item listed on the GhorCycle marketplace.
 *
 * Supports priced items as well as zero-price items identified by the Free label,
 * categorized across all eco-recycling and reuse sectors.
 */
public class Product implements Serializable {

    private String id;
    private String name;
    private String category;
    private String condition;
    private double price;
    private boolean isFree;
    private String seller;
    private String location;
    private String description;
    private int imageResId;
    private boolean isFavorite;

    public Product(String id, String name, String category, String condition,
                   double price, String seller, String location, String description,
                   int imageResId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.condition = condition;
        this.price = price;
        this.isFree = price <= 0.0;
        this.seller = seller;
        this.location = location;
        this.description = description;
        this.imageResId = imageResId;
        this.isFavorite = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.isFree = price <= 0.0;
    }

    public boolean isFree() {
        return isFree || price <= 0.0;
    }

    public void setFree(boolean free) {
        isFree = free;
        if (free) {
            this.price = 0.0;
        }
    }

    public String getFormattedPrice() {
        if (isFree()) {
            return "FREE";
        }
        return String.format(Locale.US, "৳ %,.0f", price);
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
