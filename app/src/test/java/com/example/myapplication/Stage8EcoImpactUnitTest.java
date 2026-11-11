package com.example.myapplication;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Stage 8: Recycling Directory and Eco-Impact Dashboard.
 *
 * Verifies:
 * 1. Recycling center filtering by material and location query.
 * 2. Recycling center geo coordinates validity for navigation intents.
 * 3. Eco-impact calculation:
 *    - Strict verification (only ACCEPTED requests and COMPLETED products counted).
 *    - Pending, rejected, and unverified records excluded.
 *    - Deduplication: duplicate IDs never double-count.
 *    - Defensible mathematical formulas for waste, CO2e, and water.
 */
public class Stage8EcoImpactUnitTest {

    @Test
    public void testRecyclingDataProvider_hasSampleCenters() {
        List<RecyclingCenter> centers = RecyclingDataProvider.getSampleCenters();
        assertNotNull(centers);
        assertTrue("Should contain at least 5 sample recycling centers in Dhaka", centers.size() >= 5);

        for (RecyclingCenter center : centers) {
            assertNotNull(center.getName());
            assertNotNull(center.getAddressOrArea());
            assertNotNull(center.getAcceptedMaterials());
            assertFalse(center.getAcceptedMaterials().isEmpty());
            assertTrue("Sample centers must be transparently tagged", center.isSampleData());
        }
    }

    @Test
    public void testRecyclingDataProvider_coordinatesAndDirectionsIntent() {
        List<RecyclingCenter> centers = RecyclingDataProvider.getSampleCenters();
        for (RecyclingCenter center : centers) {
            assertTrue("Center must have valid lat/lng coordinates", center.hasValidCoordinates());
            assertTrue("Latitude within Dhaka range", center.getLatitude() > 23.0 && center.getLatitude() < 24.5);
            assertTrue("Longitude within Dhaka range", center.getLongitude() > 90.0 && center.getLongitude() < 91.0);
        }
    }

    @Test
    public void testRecyclingDataProvider_filtering() {
        // 1. Filter by material "Electronics"
        List<RecyclingCenter> electronics = RecyclingDataProvider.filterCenters("Electronics", "");
        assertFalse(electronics.isEmpty());
        for (RecyclingCenter c : electronics) {
            assertTrue(c.acceptsMaterial("Electronics"));
        }

        // 2. Filter by area "Mirpur"
        List<RecyclingCenter> mirpurCenters = RecyclingDataProvider.filterCenters("All", "Mirpur");
        assertFalse(mirpurCenters.isEmpty());
        for (RecyclingCenter c : mirpurCenters) {
            assertTrue(c.getAddressOrArea().toLowerCase().contains("mirpur") ||
                    c.getName().toLowerCase().contains("mirpur"));
        }

        // 3. Filter with non-matching query
        List<RecyclingCenter> emptyResults = RecyclingDataProvider.filterCenters("All", "NonExistentPlaceXYZ123");
        assertTrue(emptyResults.isEmpty());
    }

    @Test
    public void testEcoImpactCalculator_strictVerificationAndExclusions() {
        String testUser = "user_test_101";

        List<ItemRequest> requests = new ArrayList<>();

        // 1. Accepted donation request (MUST be counted)
        ItemRequest req1 = new ItemRequest("req_1", "prod_1", "donor_A", testUser,
                ItemRequest.TYPE_DONATION, "Need books", ItemRequest.STATUS_ACCEPTED);
        requests.add(req1);

        // 2. Pending donation request (MUST BE EXCLUDED)
        ItemRequest req2 = new ItemRequest("req_2", "prod_2", "donor_B", testUser,
                ItemRequest.TYPE_DONATION, "Pending request", ItemRequest.STATUS_PENDING);
        requests.add(req2);

        // 3. Rejected exchange request (MUST BE EXCLUDED)
        ItemRequest req3 = new ItemRequest("req_3", "prod_3", testUser, "requester_C",
                ItemRequest.TYPE_EXCHANGE, "Rejected offer", ItemRequest.STATUS_REJECTED);
        requests.add(req3);

        // 4. Accepted exchange request where user is owner (MUST be counted)
        ItemRequest req4 = new ItemRequest("req_4", "prod_4", testUser, "requester_D",
                ItemRequest.TYPE_EXCHANGE, "Exchange cycle", ItemRequest.STATUS_ACCEPTED);
        requests.add(req4);

        // 5. Unrelated request where test user is neither owner nor requester (MUST BE EXCLUDED)
        ItemRequest req5 = new ItemRequest("req_5", "prod_5", "other_1", "other_2",
                ItemRequest.TYPE_DONATION, "Other", ItemRequest.STATUS_ACCEPTED);
        requests.add(req5);

        List<Product> products = new ArrayList<>();
        // 6. User product marked COMPLETED (MUST be counted)
        Product p1 = new Product();
        p1.setProductId("prod_completed_1");
        p1.setOwnerId(testUser);
        p1.setTitle("Old Chair");
        p1.setStatus(Product.STATUS_COMPLETED);
        p1.setTransactionType(Product.TYPE_DONATE);
        products.add(p1);

        // 7. User product marked AVAILABLE (not completed yet, MUST BE EXCLUDED)
        Product p2 = new Product();
        p2.setProductId("prod_avail_2");
        p2.setOwnerId(testUser);
        p2.setTitle("Desk Lamp");
        p2.setStatus(Product.STATUS_AVAILABLE);
        p2.setTransactionType(Product.TYPE_FREE);
        products.add(p2);

        EcoImpactSummary summary = EcoImpactCalculator.calculateUserImpact(testUser, requests, products);

        assertNotNull(summary);
        // Count should be exactly: req1 (donation) + req4 (exchange) + p1 (donation) = 3 total
        assertEquals(2, summary.getSuccessfulDonationsCount()); // req1 + p1
        assertEquals(1, summary.getCompletedExchangesCount());  // req4
        assertEquals(0, summary.getCompletedSalesReuseCount());
        assertEquals(3, summary.getTotalItemsReused());
        assertEquals(3, summary.getVerifiedTransactionsCount());

        // Test math benchmarks
        double expectedWasteKg = 3 * EcoImpactCalculator.AVG_WASTE_DIVERTED_KG_PER_ITEM; // 3 * 2.5 = 7.5
        double expectedCo2Kg = expectedWasteKg * EcoImpactCalculator.CO2E_SAVED_PER_KG_MATERIAL; // 7.5 * 1.8 = 13.5
        double expectedWaterL = 3 * EcoImpactCalculator.WATER_SAVINGS_LITERS_PER_ITEM; // 3 * 250 = 750.0

        assertEquals(expectedWasteKg, summary.getEstimatedWasteDivertedKg(), 0.001);
        assertEquals(expectedCo2Kg, summary.getEstimatedCo2AvoidedKg(), 0.001);
        assertEquals(expectedWaterL, summary.getEstimatedWaterSavedLiters(), 0.001);
    }

    @Test
    public void testEcoImpactCalculator_deduplicationPreventsInflation() {
        String testUser = "user_test_202";

        List<ItemRequest> requests = new ArrayList<>();

        // Add the exact same accepted request 3 times
        ItemRequest req1 = new ItemRequest("req_dup_1", "prod_dup_1", testUser, "other_buyer",
                ItemRequest.TYPE_DONATION, "First note", ItemRequest.STATUS_ACCEPTED);
        requests.add(req1);
        requests.add(req1);
        requests.add(req1);

        List<Product> products = new ArrayList<>();
        // Add the exact same completed product 4 times
        Product p1 = new Product();
        p1.setProductId("prod_dup_1");
        p1.setOwnerId(testUser);
        p1.setStatus(Product.STATUS_COMPLETED);
        p1.setTransactionType(Product.TYPE_SELL);
        products.add(p1);
        products.add(p1);
        products.add(p1);
        products.add(p1);

        EcoImpactSummary summary = EcoImpactCalculator.calculateUserImpact(testUser, requests, products);

        assertNotNull(summary);
        // Even though requests were added 3 times and products 4 times,
        // deduplication ensures 1 request and 1 product = 2 verified transactions!
        assertEquals("Deduplication must prevent duplicate requests from inflating stats", 1, summary.getSuccessfulDonationsCount());
        assertEquals("Deduplication must prevent duplicate products from inflating stats", 1, summary.getCompletedSalesReuseCount());
        assertEquals(2, summary.getTotalItemsReused());
        assertEquals(2, summary.getVerifiedTransactionsCount());
    }
}
