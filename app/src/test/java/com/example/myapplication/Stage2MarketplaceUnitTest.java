package com.example.myapplication;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests validating Stage 2 Marketplace requirements:
 * 1. Product model and free/priced labels.
 * 2. 8 Core eco categories present and non-empty.
 * 3. Sample data provider integrity.
 * 4. Search and category filtering algorithms.
 * 5. Empty search handling.
 */
public class Stage2MarketplaceUnitTest {

    @Test
    public void testAllRequiredCategoriesArePresent() {
        List<Category> categories = SampleDataProvider.getCategories();
        assertNotNull("Categories list must not be null", categories);
        assertTrue("Categories must contain at least 9 elements (All + 8 categories)", categories.size() >= 9);

        Set<String> categoryNames = new HashSet<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }

        List<String> requiredCategories = Arrays.asList(
                "Furniture",
                "Clothes",
                "Books",
                "Kitchen Items",
                "Electronics",
                "Plastic",
                "Paper",
                "Metal"
        );

        for (String req : requiredCategories) {
            assertTrue("Required category must be present: " + req, categoryNames.contains(req));
        }
    }

    @Test
    public void testSampleProductsContainAllRequiredCategories() {
        List<Product> products = SampleDataProvider.getSampleProducts();
        assertNotNull("Products list must not be null", products);
        assertTrue("Products list must not be empty", products.size() >= 16);

        Set<String> categoriesInProducts = new HashSet<>();
        boolean hasFreeItem = false;
        boolean hasPricedItem = false;

        for (Product p : products) {
            categoriesInProducts.add(p.getCategory());
            if (p.isFree()) {
                hasFreeItem = true;
                assertEquals("FREE", p.getFormattedPrice());
            } else {
                hasPricedItem = true;
                assertTrue("Formatted price should start with currency symbol ৳", p.getFormattedPrice().startsWith("৳"));
            }
        }

        List<String> requiredCategories = Arrays.asList(
                "Furniture",
                "Clothes",
                "Books",
                "Kitchen Items",
                "Electronics",
                "Plastic",
                "Paper",
                "Metal"
        );

        for (String req : requiredCategories) {
            assertTrue("Each category must have sample products: " + req, categoriesInProducts.contains(req));
        }

        assertTrue("Dataset must include FREE items", hasFreeItem);
        assertTrue("Dataset must include priced items", hasPricedItem);
    }

    @Test
    public void testFilterByCategory() {
        List<Product> all = SampleDataProvider.getSampleProducts();

        List<Product> furniture = SampleDataProvider.filter(all, "Furniture", "");
        assertFalse(furniture.isEmpty());
        for (Product p : furniture) {
            assertEquals("Furniture", p.getCategory());
        }

        List<Product> metal = SampleDataProvider.filter(all, "Metal", "");
        assertFalse(metal.isEmpty());
        for (Product p : metal) {
            assertEquals("Metal", p.getCategory());
        }
    }

    @Test
    public void testSearchByProductName() {
        List<Product> all = SampleDataProvider.getSampleProducts();

        // Search for existing keyword
        List<Product> airFryerResults = SampleDataProvider.filter(all, "All", "Air Fryer");
        assertFalse("Search for 'Air Fryer' should return matches", airFryerResults.isEmpty());
        assertTrue("Match should contain Air Fryer in title or description",
                airFryerResults.get(0).getName().contains("Air Fryer") ||
                        airFryerResults.get(0).getDescription().contains("Air Fryer"));

        // Search for non-existent keyword
        List<Product> nonexistent = SampleDataProvider.filter(all, "All", "NonExistentSpaceship12345");
        assertTrue("Non-existent search must yield empty list", nonexistent.isEmpty());
    }

    @Test
    public void testCombinedCategoryAndQueryFilter() {
        List<Product> all = SampleDataProvider.getSampleProducts();

        // Books category with "Textbook"
        List<Product> textbookResults = SampleDataProvider.filter(all, "Books", "Textbook");
        assertFalse(textbookResults.isEmpty());
        for (Product p : textbookResults) {
            assertEquals("Books", p.getCategory());
            assertTrue(p.getName().toLowerCase().contains("textbook") ||
                    p.getDescription().toLowerCase().contains("textbook"));
        }

        // Searching for "Chair" in "Books" category should return nothing
        List<Product> emptyMismatch = SampleDataProvider.filter(all, "Books", "Study Desk");
        assertTrue(emptyMismatch.isEmpty());
    }

    @Test
    public void testProductModelGettersAndSetters() {
        Product p = new Product("TEST-01", "Eco Wooden Stool", "Furniture", "Like New",
                800.0, "Test Seller", "Dhaka", "A sturdy stool", R.drawable.ic_category_furniture);

        assertEquals("TEST-01", p.getId());
        assertEquals("Eco Wooden Stool", p.getName());
        assertEquals("Furniture", p.getCategory());
        assertEquals("Like New", p.getCondition());
        assertEquals(800.0, p.getPrice(), 0.001);
        assertFalse(p.isFree());
        assertEquals("৳ 800", p.getFormattedPrice());

        // Change to free
        p.setFree(true);
        assertTrue(p.isFree());
        assertEquals("FREE", p.getFormattedPrice());

        // Toggle favorite
        assertFalse(p.isFavorite());
        p.setFavorite(true);
        assertTrue(p.isFavorite());
    }
}
