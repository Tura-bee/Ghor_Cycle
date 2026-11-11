package com.example.myapplication;

import java.io.Serializable;

/**
 * Model representing a recycling guideline item parsed from an XML document.
 * Demonstrates XML data extraction and transformation into Java objects.
 */
public class EcoGuideline implements Serializable {

    private final String id;
    private final String title;
    private final String category;
    private final String impactCo2;
    private final String instructions;

    public EcoGuideline(String id, String title, String category, String impactCo2, String instructions) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.impactCo2 = impactCo2;
        this.instructions = instructions;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getImpactCo2() {
        return impactCo2;
    }

    public String getInstructions() {
        return instructions;
    }
}
