package com.example.ghor_cycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defensible, transparent environmental impact calculator for GhorCycle.
 *
 * Implements:
 * 1. Strict verification: Computes metrics only from verified transaction records
 *    (ACCEPTED requests and COMPLETED/CYCLED product handovers).
 * 2. Strict deduplication: Uses unique transaction/product IDs to prevent duplicate
 *    records from inflating environmental statistics.
 * 3. Documented, conservative benchmarks:
 *    - Waste Diverted: 2.5 kg avg weight per reused household good.
 *    - CO2e Avoidance: 1.8 kg CO2e avoided per kg of reused material (based on manufacturing avoidance).
 *    - Water Conserved: 250 Liters per textile/paper/kitchen good reused.
 * 4. Clear labeling of all values as ESTIMATES rather than exact measurements.
 */
public class EcoImpactCalculator {

    // Documented conservative environmental impact benchmarks
    public static final double AVG_WASTE_DIVERTED_KG_PER_ITEM = 2.5;
    public static final double CO2E_SAVED_PER_KG_MATERIAL = 1.8;
    public static final double WATER_SAVINGS_LITERS_PER_ITEM = 250.0;

    public static final String METHODOLOGY_ASSUMPTION_NOTE =
            "Methodology & Assumptions:\n" +
            "1. Verified Transactions Only: Only requests with status 'ACCEPTED' and items marked 'COMPLETED' are calculated. Drafts, pending requests, and rejected claims are strictly excluded.\n" +
            "2. Deduplication: Unique transaction IDs ensure an item is only counted once even across multiple user records.\n" +
            "3. Landfill Diversion (Est.): Assumes an empirical average of ~2.5 kg per household good (weighted across furniture, clothes, books, plastics, metals).\n" +
            "4. CO2e Emissions Avoided (Est.): Based on lifecycle avoidance factors (~1.8 kg CO2e per kg of reused durable goods avoiding virgin manufacturing).\n" +
            "5. Water Conservation (Est.): Accounts for embodied water in textiles and paper products (~250L per diverted unit).\n" +
            "* All environmental metrics are conservative estimates for educational and community awareness.";

    /**
     * Computes verified EcoImpactSummary for a given user.
     *
     * @param userId Current user UID.
     * @param requests List of ItemRequest records from Cloud Firestore or local cache.
     * @param userProducts List of Product records owned by user.
     * @return Deduplicated, verified EcoImpactSummary.
     */
    public static EcoImpactSummary calculateUserImpact(String userId,
                                                        List<ItemRequest> requests,
                                                        List<Product> userProducts) {
        if (userId == null) userId = "";

        int donations = 0;
        int exchanges = 0;
        int salesReuses = 0;

        Set<String> verifiedTransactionIds = new HashSet<>();

        // 1. Process Item Requests
        if (requests != null) {
            for (ItemRequest req : requests) {
                if (req == null) continue;

                // User must be involved as either owner or requester
                boolean isUserParty = userId.equals(req.getOwnerId()) || userId.equals(req.getRequesterId());
                if (!isUserParty) continue;

                // Must be verified (ACCEPTED)
                if (!ItemRequest.STATUS_ACCEPTED.equalsIgnoreCase(req.getStatus())) {
                    continue;
                }

                String txKey = "REQ-" + req.getRequestId();
                if (verifiedTransactionIds.contains(txKey)) {
                    continue; // Skip duplicates
                }
                verifiedTransactionIds.add(txKey);

                if (ItemRequest.TYPE_DONATION.equalsIgnoreCase(req.getRequestType())) {
                    donations++;
                } else if (ItemRequest.TYPE_EXCHANGE.equalsIgnoreCase(req.getRequestType())) {
                    exchanges++;
                } else {
                    salesReuses++;
                }
            }
        }

        // 2. Process Completed Products
        if (userProducts != null) {
            for (Product p : userProducts) {
                if (p == null) continue;

                if (!userId.equals(p.getOwnerId())) continue;

                // Must be marked Completed or Cycled
                if (!p.isCompleted()) continue;

                String prodKey = "PROD-" + p.getProductId();
                if (verifiedTransactionIds.contains(prodKey)) {
                    continue; // Skip duplicates
                }
                verifiedTransactionIds.add(prodKey);

                if (Product.TYPE_DONATE.equalsIgnoreCase(p.getTransactionType()) || Product.TYPE_FREE.equalsIgnoreCase(p.getTransactionType())) {
                    donations++;
                } else if (Product.TYPE_RECYCLE.equalsIgnoreCase(p.getTransactionType())) {
                    exchanges++;
                } else {
                    salesReuses++;
                }
            }
        }

        int totalReused = donations + exchanges + salesReuses;
        double wasteDiverted = totalReused * AVG_WASTE_DIVERTED_KG_PER_ITEM;
        double co2Avoided = wasteDiverted * CO2E_SAVED_PER_KG_MATERIAL;
        double waterSaved = totalReused * WATER_SAVINGS_LITERS_PER_ITEM;

        return new EcoImpactSummary(
                userId,
                donations,
                exchanges,
                salesReuses,
                wasteDiverted,
                co2Avoided,
                waterSaved,
                verifiedTransactionIds
        );
    }
}
