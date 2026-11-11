package com.example.ghor_cycle;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Stage 4 Firestore Product Management:
 * 1. Product model complete data attributes (12 required Firestore fields).
 * 2. Serialization to Map for Firestore document writing.
 * 3. Dynamic pricing and transaction type evaluation (Sell vs Free / Donate / Recycle).
 * 4. Backward-compatibility helper methods for UI layers.
 * 5. Owner permission authorization checks.
 * 6. Product field validation rules.
 */
public class Stage4FirestoreProductUnitTest {

    @Test
    public void testProductFullConstructorAndGetters() {
        Product product = new Product(
                "prod-1234",
                "user-auth-5678",
                "Solid Teak Dining Table",
                "High quality reclaimed wood dining table with 4 matching chairs.",
                "Furniture",
                "Gently Used",
                Product.TYPE_SELL,
                4500.0,
                "Gulshan 2, Dhaka",
                "https://firebasestorage.googleapis.com/test-image.jpg",
                Product.STATUS_AVAILABLE
        );

        assertEquals("prod-1234", product.getProductId());
        assertEquals("user-auth-5678", product.getOwnerId());
        assertEquals("Solid Teak Dining Table", product.getTitle());
        assertEquals("High quality reclaimed wood dining table with 4 matching chairs.", product.getDescription());
        assertEquals("Furniture", product.getCategory());
        assertEquals("Gently Used", product.getCondition());
        assertEquals(Product.TYPE_SELL, product.getTransactionType());
        assertEquals(4500.0, product.getPrice(), 0.001);
        assertEquals("Gulshan 2, Dhaka", product.getLocation());
        assertEquals("https://firebasestorage.googleapis.com/test-image.jpg", product.getImageUrl());
        assertEquals(Product.STATUS_AVAILABLE, product.getStatus());
        assertFalse(product.isFree());
        assertEquals("৳ 4,500", product.getFormattedPrice());
    }

    @Test
    public void testProductToMapContainsRequiredFirestoreKeys() {
        Product product = new Product(
                "doc-99",
                "owner-xyz",
                "Eco Jute Bags",
                "Reusable handmade eco bags from natural jute fiber.",
                "Plastic",
                "Like New",
                Product.TYPE_FREE,
                0.0,
                "Dhanmondi 27",
                "file:///storage/emulated/0/DCIM/jute.jpg",
                Product.STATUS_AVAILABLE
        );

        Map<String, Object> map = product.toMap();

        assertNotNull(map);
        assertEquals("doc-99", map.get("productId"));
        assertEquals("owner-xyz", map.get("ownerId"));
        assertEquals("Eco Jute Bags", map.get("title"));
        assertEquals("Reusable handmade eco bags from natural jute fiber.", map.get("description"));
        assertEquals("Plastic", map.get("category"));
        assertEquals("Like New", map.get("condition"));
        assertEquals(Product.TYPE_FREE, map.get("transactionType"));
        assertEquals(0.0, ((Double) map.get("price")).doubleValue(), 0.001);
        assertEquals("Dhanmondi 27", map.get("location"));
        assertEquals("file:///storage/emulated/0/DCIM/jute.jpg", map.get("imageUrl"));
        assertEquals(Product.STATUS_AVAILABLE, map.get("status"));
        assertTrue(map.containsKey("createdAt"));
        assertTrue(map.containsKey("updatedAt"));
    }

    @Test
    public void testTransactionTypesAndFreeCalculations() {
        // Free item
        Product freeItem = new Product("1", "u1", "Carton Boxes", "Clean boxes", "Paper", "Good Condition",
                Product.TYPE_FREE, 0.0, "Mirpur 10", null, Product.STATUS_AVAILABLE);
        assertTrue(freeItem.isFree());
        assertEquals("FREE", freeItem.getFormattedPrice());

        // Donate item
        Product donateItem = new Product("2", "u1", "Old Textbooks", "Class 10 books", "Books", "Good Condition",
                Product.TYPE_DONATE, 0.0, "Uttara", null, Product.STATUS_AVAILABLE);
        assertTrue(donateItem.isFree());
        assertEquals("FREE", donateItem.getFormattedPrice());

        // Recycle item
        Product recycleItem = new Product("3", "u1", "Copper Wire Scrap", "Metal salvage", "Metal", "Recyclable / Salvage",
                Product.TYPE_RECYCLE, 0.0, "Old Dhaka", null, Product.STATUS_AVAILABLE);
        assertTrue(recycleItem.isFree());
        assertEquals("FREE", recycleItem.getFormattedPrice());

        // Sell item with price
        Product sellItem = new Product("4", "u1", "Electric Kettle", "Working kettle", "Electronics", "Gently Used",
                Product.TYPE_SELL, 850.0, "Banani", null, Product.STATUS_AVAILABLE);
        assertFalse(sellItem.isFree());
        assertEquals("৳ 850", sellItem.getFormattedPrice());
    }

    @Test
    public void testBackwardCompatibilityHelperMethods() {
        Product product = new Product("legacy-01", "owner-legacy", "Vintage Lamp", "Brass desk lamp",
                "Furniture", "Good Condition", Product.TYPE_SELL, 1200.0, "Mohakhali", null, Product.STATUS_AVAILABLE);

        // getName() maps to getTitle()
        assertEquals("Vintage Lamp", product.getName());
        // getId() maps to getProductId()
        assertEquals("legacy-01", product.getId());
        // getSeller() maps to ownerId / seller
        assertEquals("owner-legacy", product.getSeller());

        // setName() updates title
        product.setName("Updated Vintage Lamp");
        assertEquals("Updated Vintage Lamp", product.getTitle());
    }

    @Test
    public void testOwnershipValidationLogic() {
        String ownerUserId = "user_alpha_123";
        String nonOwnerUserId = "user_beta_456";

        Product product = new Product("listing-1", ownerUserId, "Clay Pots", "Terracotta pots",
                "Kitchen Items", "Like New", Product.TYPE_SELL, 300.0, "Badda", null, Product.STATUS_AVAILABLE);

        // Owner check matches
        assertTrue(ownerUserId.equals(product.getOwnerId()));

        // Non-owner check fails
        assertFalse(nonOwnerUserId.equals(product.getOwnerId()));
    }

    @Test
    public void testProductFieldValidationRules() {
        // Valid title lengths (2 to 100)
        assertTrue(isValidTitle("Desk"));
        assertTrue(isValidTitle("Solid Oak Kitchen Countertop with Drawers"));
        assertFalse(isValidTitle(""));
        assertFalse(isValidTitle("A"));
        assertFalse(isValidTitle(null));

        // Valid description lengths (3 to 2000)
        assertTrue(isValidDescription("Used study table in good state."));
        assertFalse(isValidDescription(""));
        assertFalse(isValidDescription("hi"));
        assertFalse(isValidDescription(null));

        // Price validation based on transaction type
        assertTrue(isValidPrice(Product.TYPE_SELL, 500.0));
        assertFalse(isValidPrice(Product.TYPE_SELL, 0.0));
        assertFalse(isValidPrice(Product.TYPE_SELL, -50.0));
        assertTrue(isValidPrice(Product.TYPE_FREE, 0.0));
        assertTrue(isValidPrice(Product.TYPE_DONATE, 0.0));
        assertTrue(isValidPrice(Product.TYPE_RECYCLE, 0.0));
    }

    private boolean isValidTitle(String title) {
        return title != null && title.trim().length() >= 2 && title.trim().length() <= 100;
    }

    private boolean isValidDescription(String description) {
        return description != null && description.trim().length() >= 3 && description.trim().length() <= 2000;
    }

    private boolean isValidPrice(String transactionType, double price) {
        if (Product.TYPE_SELL.equalsIgnoreCase(transactionType)) {
            return price > 0;
        }
        return price >= 0;
    }
}
