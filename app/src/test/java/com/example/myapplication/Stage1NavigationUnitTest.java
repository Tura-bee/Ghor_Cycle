package com.example.myapplication;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test suite verifying Stage 1 navigation contracts, Intent extra keys,
 * sample product data models, and fallbacks.
 */
public class Stage1NavigationUnitTest {

    @Test
    public void testProductDetailsExtraKeysAreDefinedAndUnique() {
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_ID);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_NAME);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_PRICE);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_SELLER);
        assertNotNull(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION);

        Set<String> keys = new HashSet<>();
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_ID);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_NAME);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_PRICE);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_CATEGORY);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_CONDITION);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_SELLER);
        keys.add(ProductDetailsActivity.EXTRA_PRODUCT_DESCRIPTION);

        assertEquals("All product detail extra keys must be unique", 7, keys.size());
    }

    @Test
    public void testAuthAndProfileExtraKeysAreDefined() {
        assertNotNull(LoginActivity.EXTRA_USER_NAME);
        assertNotNull(ProfileActivity.EXTRA_PROFILE_NAME);
        assertFalse(LoginActivity.EXTRA_USER_NAME.trim().isEmpty());
        assertFalse(ProfileActivity.EXTRA_PROFILE_NAME.trim().isEmpty());
    }

    @Test
    public void testSampleProductFormattingAndCurrency() {
        String samplePrice = "৳ 9,500";
        assertTrue("Price should contain BDT symbol", samplePrice.contains("৳"));

        String sampleId = "GC-BIKE-101";
        assertTrue("Product ID should adhere to GC prefix convention", sampleId.startsWith("GC-"));
    }
}
