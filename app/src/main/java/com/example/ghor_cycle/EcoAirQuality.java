package com.example.ghor_cycle;

import java.io.Serializable;

/**
 * Model representing live environmental and air quality metrics for GhorCycle.
 *
 * Transformed from JSON response returned by the public Open-Meteo REST API.
 */
public class EcoAirQuality implements Serializable {

    private final double latitude;
    private final double longitude;
    private final String timestamp;
    private final double pm25;
    private final double pm10;
    private final int europeanAqi;
    private final boolean isFromLiveApi;
    private final int httpStatusCode;

    public EcoAirQuality(double latitude, double longitude, String timestamp,
                         double pm25, double pm10, int europeanAqi,
                         boolean isFromLiveApi, int httpStatusCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.pm25 = pm25;
        this.pm10 = pm10;
        this.europeanAqi = europeanAqi;
        this.isFromLiveApi = isFromLiveApi;
        this.httpStatusCode = httpStatusCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getPm25() {
        return pm25;
    }

    public double getPm10() {
        return pm10;
    }

    public int getEuropeanAqi() {
        return europeanAqi;
    }

    public boolean isFromLiveApi() {
        return isFromLiveApi;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Determines descriptive health label based on European Air Quality Index (AQI).
     */
    public String getAqiCategory() {
        if (europeanAqi <= 20) return "Good (Clean Air)";
        if (europeanAqi <= 40) return "Fair";
        if (europeanAqi <= 60) return "Moderate";
        if (europeanAqi <= 80) return "Poor";
        if (europeanAqi <= 100) return "Very Poor";
        return "Hazardous";
    }

    /**
     * Contextual eco recommendation for community reuse/recycling.
     */
    public String getEcoRecommendation() {
        if (europeanAqi <= 40) {
            return "Air quality is good. Great day for neighborhood pickup and bike-drop-off deliveries!";
        } else if (europeanAqi <= 80) {
            return "Moderate pollution levels. Consolidate your recycling drop-offs and minimize vehicle trips.";
        } else {
            return "Elevated particulate matter. Upcycling goods locally reduces urban industrial emissions.";
        }
    }
}
