package com.example.myapplication;

import java.io.Serializable;
import java.util.Set;

/**
 * Model summarizing verified user eco-impact statistics for GhorCycle.
 *
 * Implements:
 * 1. Counts computed strictly from verified transaction records.
 * 2. Deduplication through verified transaction ID sets.
 * 3. Defensible, transparent environmental indicators with documented formulas.
 */
public class EcoImpactSummary implements Serializable {

    private final String userId;
    private final int successfulDonationsCount;
    private final int completedExchangesCount;
    private final int completedSalesReuseCount;
    private final int totalItemsReused;

    private final double estimatedWasteDivertedKg;
    private final double estimatedCo2AvoidedKg;
    private final double estimatedWaterSavedLiters;

    private final Set<String> verifiedTransactionIds;

    public EcoImpactSummary(String userId, int successfulDonationsCount,
                            int completedExchangesCount, int completedSalesReuseCount,
                            double estimatedWasteDivertedKg, double estimatedCo2AvoidedKg,
                            double estimatedWaterSavedLiters, Set<String> verifiedTransactionIds) {
        this.userId = userId;
        this.successfulDonationsCount = successfulDonationsCount;
        this.completedExchangesCount = completedExchangesCount;
        this.completedSalesReuseCount = completedSalesReuseCount;
        this.totalItemsReused = successfulDonationsCount + completedExchangesCount + completedSalesReuseCount;
        this.estimatedWasteDivertedKg = estimatedWasteDivertedKg;
        this.estimatedCo2AvoidedKg = estimatedCo2AvoidedKg;
        this.estimatedWaterSavedLiters = estimatedWaterSavedLiters;
        this.verifiedTransactionIds = verifiedTransactionIds;
    }

    public String getUserId() {
        return userId;
    }

    public int getSuccessfulDonationsCount() {
        return successfulDonationsCount;
    }

    public int getCompletedExchangesCount() {
        return completedExchangesCount;
    }

    public int getCompletedSalesReuseCount() {
        return completedSalesReuseCount;
    }

    public int getTotalItemsReused() {
        return totalItemsReused;
    }

    public double getEstimatedWasteDivertedKg() {
        return estimatedWasteDivertedKg;
    }

    public double getEstimatedCo2AvoidedKg() {
        return estimatedCo2AvoidedKg;
    }

    public double getEstimatedWaterSavedLiters() {
        return estimatedWaterSavedLiters;
    }

    public Set<String> getVerifiedTransactionIds() {
        return verifiedTransactionIds;
    }

    public int getVerifiedTransactionsCount() {
        return verifiedTransactionIds != null ? verifiedTransactionIds.size() : 0;
    }
}
