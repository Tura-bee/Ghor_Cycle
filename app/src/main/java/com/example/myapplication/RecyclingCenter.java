package com.example.myapplication;

import java.io.Serializable;
import java.util.List;

/**
 * Model representing a physical recycling center or community drop-off hub.
 *
 * Implements:
 * 1. Storage of center identity, neighborhood area, and accepted waste categories.
 * 2. Operating hours and verified contact information.
 * 3. Geolocation coordinates for external map and navigation Intents.
 * 4. Transparent labeling of sample / demonstration data.
 */
public class RecyclingCenter implements Serializable {

    private final String centerId;
    private final String name;
    private final String addressOrArea;
    private final List<String> acceptedMaterials;
    private final String contactPhone;
    private final String contactEmail;
    private final String openingHours;
    private final double latitude;
    private final double longitude;
    private final boolean isSampleData;
    private final String notes;

    public RecyclingCenter(String centerId, String name, String addressOrArea,
                           List<String> acceptedMaterials, String contactPhone,
                           String contactEmail, String openingHours,
                           double latitude, double longitude, boolean isSampleData,
                           String notes) {
        this.centerId = centerId;
        this.name = name;
        this.addressOrArea = addressOrArea;
        this.acceptedMaterials = acceptedMaterials;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.openingHours = openingHours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isSampleData = isSampleData;
        this.notes = notes;
    }

    public String getCenterId() {
        return centerId;
    }

    public String getName() {
        return name;
    }

    public String getAddressOrArea() {
        return addressOrArea;
    }

    public List<String> getAcceptedMaterials() {
        return acceptedMaterials;
    }

    public String getFormattedMaterials() {
        if (acceptedMaterials == null || acceptedMaterials.isEmpty()) return "General recyclables";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < acceptedMaterials.size(); i++) {
            sb.append(acceptedMaterials.get(i));
            if (i < acceptedMaterials.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isSampleData() {
        return isSampleData;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasValidCoordinates() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public boolean acceptsMaterial(String material) {
        if (material == null || acceptedMaterials == null) return false;
        for (String m : acceptedMaterials) {
            if (m.equalsIgnoreCase(material)) return true;
        }
        return false;
    }
}
