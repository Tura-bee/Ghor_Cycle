package com.example.myapplication;

/**
 * Category model for the horizontal marketplace filter chips.
 */
public class Category {

    private String name;
    private int iconResId;
    private boolean isSelected;

    public Category(String name, int iconResId, boolean isSelected) {
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = isSelected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
