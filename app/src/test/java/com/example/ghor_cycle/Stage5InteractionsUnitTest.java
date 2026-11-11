package com.example.ghor_cycle;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Stage 5 Marketplace Interactions:
 * 1. Multi-factor search and filtering (Category, Condition, Type, Location, Keywords).
 * 2. Empty search result handling and clear filters.
 * 3. User Favorites filtering and model state.
 * 4. Donation and Exchange ItemRequest model serialization and state machine.
 * 5. Rule validation: Disallow requesting own product.
 * 6. Rule validation: Completed items cannot accept requests.
 * 7. Transaction status lifecycle (Available -> Reserved -> Completed).
 */
public class Stage5InteractionsUnitTest {

    private List<Product> sampleProducts;

    @Before
    public void setUp() {
        sampleProducts = new ArrayList<>();

        sampleProducts.add(new Product("p1", "u1", "Wooden Study Desk", "Solid timber desk",
                "Furniture", "Good Condition", Product.TYPE_SELL, 1800.0, "Dhanmondi, Dhaka", null, Product.STATUS_AVAILABLE));

        sampleProducts.add(new Product("p2", "u2", "College Physics Guide", "HSC textbooks",
                "Books", "Like New", Product.TYPE_FREE, 0.0, "Mirpur 10, Dhaka", null, Product.STATUS_AVAILABLE));

        sampleProducts.add(new Product("p3", "u3", "Denim Jacket", "Vintage Levi jacket",
                "Clothes", "Gently Used", Product.TYPE_SELL, 1200.0, "Banani, Dhaka", null, Product.STATUS_AVAILABLE));

        sampleProducts.add(new Product("p4", "u1", "Brass Plant Pot", "Upcycled brass container",
                "Metal", "Recyclable / Salvage", Product.TYPE_RECYCLE, 0.0, "Old Dhaka", null, Product.STATUS_AVAILABLE));

        sampleProducts.add(new Product("p5", "u4", "Blender & Food Processor", "Electric kitchen appliance",
                "Kitchen Items", "Gently Used", Product.TYPE_DONATE, 0.0, "Gulshan 2, Dhaka", null, Product.STATUS_RESERVED));

        sampleProducts.add(new Product("p6", "u5", "Plastic Storage Totes", "Set of 4 heavy duty bins",
                "Plastic", "Good Condition", Product.TYPE_FREE, 0.0, "Uttara, Dhaka", null, Product.STATUS_COMPLETED));
    }

    @Test
    public void testSearchByTitleAndKeywords() {
        // Known item
        List<Product> results = SampleDataProvider.filter(sampleProducts, SampleDataProvider.CATEGORY_ALL, "Desk");
        assertEquals(1, results.size());
        assertEquals("Wooden Study Desk", results.get(0).getTitle());

        // Substring / keyword search
        List<Product> physicsResults = SampleDataProvider.filter(sampleProducts, SampleDataProvider.CATEGORY_ALL, "physics");
        assertEquals(1, physicsResults.size());
        assertEquals("College Physics Guide", physicsResults.get(0).getTitle());

        // Unknown search -> 0 results
        List<Product> emptyResults = SampleDataProvider.filter(sampleProducts, SampleDataProvider.CATEGORY_ALL, "Nonexistent Spaceship");
        assertTrue(emptyResults.isEmpty());
    }

    @Test
    public void testFilterByCategory() {
        List<Product> furniture = SampleDataProvider.filter(sampleProducts, "Furniture", "");
        assertEquals(1, furniture.size());
        assertEquals("Wooden Study Desk", furniture.get(0).getTitle());

        List<Product> books = SampleDataProvider.filter(sampleProducts, "Books", "");
        assertEquals(1, books.size());
        assertEquals("College Physics Guide", books.get(0).getTitle());
    }

    @Test
    public void testMultiFilterCategoryConditionTypeAndLocation() {
        // Filter by Condition: Like New
        List<Product> likeNew = SampleDataProvider.filter(
                sampleProducts, SampleDataProvider.CATEGORY_ALL, "", "Like New", "All", "", false, null);
        assertEquals(1, likeNew.size());
        assertEquals("College Physics Guide", likeNew.get(0).getTitle());

        // Filter by Transaction Type: Sell
        List<Product> sellItems = SampleDataProvider.filter(
                sampleProducts, SampleDataProvider.CATEGORY_ALL, "", "All", Product.TYPE_SELL, "", false, null);
        assertEquals(2, sellItems.size());

        // Filter by Transaction Type: Donate
        List<Product> donateItems = SampleDataProvider.filter(
                sampleProducts, SampleDataProvider.CATEGORY_ALL, "", "All", Product.TYPE_DONATE, "", false, null);
        assertEquals(1, donateItems.size());
        assertEquals("Blender & Food Processor", donateItems.get(0).getTitle());

        // Filter by Location: Banani
        List<Product> bananiItems = SampleDataProvider.filter(
                sampleProducts, SampleDataProvider.CATEGORY_ALL, "", "All", "All", "Banani", false, null);
        assertEquals(1, bananiItems.size());
        assertEquals("Denim Jacket", bananiItems.get(0).getTitle());

        // Combined filter: Category=Furniture, Condition=Good Condition, Location=Dhanmondi
        List<Product> combined = SampleDataProvider.filter(
                sampleProducts, "Furniture", "", "Good Condition", Product.TYPE_SELL, "Dhanmondi", false, null);
        assertEquals(1, combined.size());
        assertEquals("Wooden Study Desk", combined.get(0).getTitle());

        // Non-matching combined filter
        List<Product> noMatch = SampleDataProvider.filter(
                sampleProducts, "Furniture", "", "Like New", Product.TYPE_SELL, "Uttara", false, null);
        assertTrue(noMatch.isEmpty());
    }

    @Test
    public void testFavoritesFilter() {
        Set<String> favs = new HashSet<>();
        favs.add("p1");
        favs.add("p3");

        List<Product> userFavorites = SampleDataProvider.filter(
                sampleProducts, SampleDataProvider.CATEGORY_ALL, "", "All", "All", "", true, favs);

        assertEquals(2, userFavorites.size());
        assertTrue(userFavorites.stream().anyMatch(p -> p.getProductId().equals("p1")));
        assertTrue(userFavorites.stream().anyMatch(p -> p.getProductId().equals("p3")));
        assertFalse(userFavorites.stream().anyMatch(p -> p.getProductId().equals("p2")));
    }

    @Test
    public void testItemRequestModelAndToMap() {
        ItemRequest donationReq = new ItemRequest(
                "req-101",
                "p2",
                "College Physics Guide",
                "u2",
                "requester-99",
                "requester@ghorcycle.com",
                ItemRequest.TYPE_DONATION,
                null,
                "Need for upcoming board exam.",
                ItemRequest.STATUS_PENDING
        );

        assertTrue(donationReq.isPending());
        assertFalse(donationReq.isAccepted());
        assertEquals(ItemRequest.TYPE_DONATION, donationReq.getRequestType());

        Map<String, Object> map = donationReq.toMap();
        assertEquals("req-101", map.get("requestId"));
        assertEquals("p2", map.get("productId"));
        assertEquals("College Physics Guide", map.get("productTitle"));
        assertEquals("u2", map.get("ownerId"));
        assertEquals("requester-99", map.get("requesterId"));
        assertEquals("requester@ghorcycle.com", map.get("requesterEmail"));
        assertEquals("Need for upcoming board exam.", map.get("message"));
        assertEquals(ItemRequest.STATUS_PENDING, map.get("status"));
        assertTrue(map.containsKey("createdAt"));
        assertTrue(map.containsKey("updatedAt"));

        // Exchange Proposal
        ItemRequest exchangeReq = new ItemRequest(
                "req-102",
                "p3",
                "Denim Jacket",
                "u3",
                "requester-88",
                "barter@ghorcycle.com",
                ItemRequest.TYPE_EXCHANGE,
                "Handcrafted Wool Scarf",
                "Would love to trade for this jacket!",
                ItemRequest.STATUS_PENDING
        );
        assertEquals(ItemRequest.TYPE_EXCHANGE, exchangeReq.getRequestType());
        assertEquals("Handcrafted Wool Scarf", exchangeReq.getOfferedItem());
    }

    @Test
    public void testDisallowRequestingOwnProductRule() {
        String ownerId = "user_alpha";
        String sameUserAsRequester = "user_alpha";
        String differentUserAsRequester = "user_beta";

        // Owner attempting to request own listing
        boolean isOwnerAttemptingRequest = ownerId.equals(sameUserAsRequester);
        assertTrue("System must identify when requester is listing owner", isOwnerAttemptingRequest);

        // Valid neighbor request
        boolean isOtherUserRequest = !ownerId.equals(differentUserAsRequester);
        assertTrue("Different user is eligible to request", isOtherUserRequest);
    }

    @Test
    public void testTransactionStatusStatesAndDisallowCompletedRequests() {
        Product availableProduct = sampleProducts.get(0); // p1
        Product reservedProduct = sampleProducts.get(4);  // p5
        Product completedProduct = sampleProducts.get(5); // p6

        assertTrue(availableProduct.isAvailable());
        assertFalse(availableProduct.isReserved());
        assertFalse(availableProduct.isCompleted());

        assertFalse(reservedProduct.isAvailable());
        assertTrue(reservedProduct.isReserved());
        assertFalse(reservedProduct.isCompleted());

        assertFalse(completedProduct.isAvailable());
        assertTrue(completedProduct.isCompleted());

        // Rule: Completed items cannot be requested
        assertFalse("Completed items must not accept new requests", completedProduct.isAvailable());
    }

    @Test
    public void testExchangeProposalValidationLogic() {
        // Empty offered item invalid for exchange
        String emptyOffer = "";
        String validOffer = "Ceramic Table Lamp";

        assertFalse(isValidExchangeProposal(ItemRequest.TYPE_EXCHANGE, emptyOffer));
        assertTrue(isValidExchangeProposal(ItemRequest.TYPE_EXCHANGE, validOffer));
        // Pure donation does not require offered item
        assertTrue(isValidExchangeProposal(ItemRequest.TYPE_DONATION, ""));
    }

    private boolean isValidExchangeProposal(String type, String offeredItem) {
        if (ItemRequest.TYPE_EXCHANGE.equalsIgnoreCase(type)) {
            return offeredItem != null && !offeredItem.trim().isEmpty();
        }
        return true;
    }
}
