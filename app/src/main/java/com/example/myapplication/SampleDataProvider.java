package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Clearly identified Java sample-data source providing marketplace mock items
 * across all 8 core recycling & reuse categories in GhorCycle.
 */
public class SampleDataProvider {

    public static final String CATEGORY_ALL = "All";
    public static final String CATEGORY_FURNITURE = "Furniture";
    public static final String CATEGORY_CLOTHES = "Clothes";
    public static final String CATEGORY_BOOKS = "Books";
    public static final String CATEGORY_KITCHEN = "Kitchen Items";
    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String CATEGORY_PLASTIC = "Plastic";
    public static final String CATEGORY_PAPER = "Paper";
    public static final String CATEGORY_METAL = "Metal";

    /**
     * Returns the full list of available categories for the horizontal filter.
     */
    public static List<Category> getCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(CATEGORY_ALL, R.drawable.ic_category_all, true));
        categories.add(new Category(CATEGORY_FURNITURE, R.drawable.ic_category_furniture, false));
        categories.add(new Category(CATEGORY_CLOTHES, R.drawable.ic_category_clothes, false));
        categories.add(new Category(CATEGORY_BOOKS, R.drawable.ic_category_books, false));
        categories.add(new Category(CATEGORY_KITCHEN, R.drawable.ic_category_kitchen, false));
        categories.add(new Category(CATEGORY_ELECTRONICS, R.drawable.ic_category_electronics, false));
        categories.add(new Category(CATEGORY_PLASTIC, R.drawable.ic_category_plastic, false));
        categories.add(new Category(CATEGORY_PAPER, R.drawable.ic_category_paper, false));
        categories.add(new Category(CATEGORY_METAL, R.drawable.ic_category_metal, false));
        return categories;
    }

    /**
     * Returns a realistic, diverse set of sample products for all 8 categories,
     * including priced and FREE items.
     */
    public static List<Product> getSampleProducts() {
        List<Product> products = new ArrayList<>();

        // 1. Furniture
        products.add(new Product(
                "GC-FURN-01",
                "Solid Malaysian Oak Study Desk & Chair",
                CATEGORY_FURNITURE,
                "Like New",
                5200.0,
                "Tasnim Rahman",
                "Uttara Sector 7, Dhaka",
                "High quality Malaysian oak desk with 3 lockable storage drawers and an ergonomic adjustable chair. Gently used for 6 months.",
                R.drawable.ic_category_furniture
        ));

        products.add(new Product(
                "GC-FURN-02",
                "Teak Wood Coffee Table with Glass Top",
                CATEGORY_FURNITURE,
                "Gently Used",
                2800.0,
                "Farhan Ahmed",
                "Dhanmondi 27, Dhaka",
                "Sturdy CTG teak coffee table with scratch-free 8mm tempered glass top. Ideal for living room center space.",
                R.drawable.ic_category_furniture
        ));

        products.add(new Product(
                "GC-FURN-03",
                "Foldable Compact Balcony Chairs (Set of 2)",
                CATEGORY_FURNITURE,
                "Good Condition",
                0.0, // FREE
                "Nusrat Jahan",
                "Gulshan 1, Dhaka",
                "Two weather-resistant foldable wooden balcony chairs looking for a new home. Giving away for free to clear balcony space.",
                R.drawable.ic_category_furniture
        ));

        // 2. Clothes
        products.add(new Product(
                "GC-CLOTH-01",
                "Winter Heavy Down Jacket (Men's Large)",
                CATEGORY_CLOTHES,
                "Like New",
                1800.0,
                "Saiful Islam",
                "Mirpur 10, Dhaka",
                "Windproof, water-resistant heavy winter down coat. Only worn during one overseas trip. Dry cleaned and ready to wear.",
                R.drawable.ic_category_clothes
        ));

        products.add(new Product(
                "GC-CLOTH-02",
                "Traditional Handloom Cotton Sarees (Set of 3)",
                CATEGORY_CLOTHES,
                "Gently Used",
                0.0, // FREE
                "Rokeya Begum",
                "Mohammadpur, Dhaka",
                "Three pure cotton handloom sarees in vibrant traditional weave. Clean and washed, giving away for free to anyone in need.",
                R.drawable.ic_category_clothes
        ));

        products.add(new Product(
                "GC-CLOTH-03",
                "Vintage Levi's Denim Jacket (Unisex Medium)",
                CATEGORY_CLOTHES,
                "Gently Used",
                1450.0,
                "Abrar Khan",
                "Banani, Dhaka",
                "Authentic classic medium-wash denim jacket with copper hardware. Heavyweight durable fabric with great patina.",
                R.drawable.ic_category_clothes
        ));

        // 3. Books
        products.add(new Product(
                "GC-BOOK-01",
                "HSC Science Textbook Bundle (Physics, Chemistry & Math)",
                CATEGORY_BOOKS,
                "Good Condition",
                0.0, // FREE
                "Shahriar Kabir",
                "Farmgate, Dhaka",
                "Complete set of NCTB approved Higher Secondary science guides and question banks. Free for any upcoming student.",
                R.drawable.ic_category_books
        ));

        products.add(new Product(
                "GC-BOOK-02",
                "Humayun Ahmed Novel Classics (10 Volume Set)",
                CATEGORY_BOOKS,
                "Like New",
                1200.0,
                "Tahmid Hassan",
                "Shantinagar, Dhaka",
                "Hardcover collector editions of famous Himu and Misir Ali novels. Clean spine and pages with no pen markings.",
                R.drawable.ic_category_books
        ));

        products.add(new Product(
                "GC-BOOK-03",
                "English Fiction Paperback Collection (7 Bestsellers)",
                CATEGORY_BOOKS,
                "Gently Used",
                650.0,
                "Sabrina Haque",
                "Baridhara, Dhaka",
                "Popular international bestsellers including mystery, thriller, and sci-fi titles. Perfect for weekend reading.",
                R.drawable.ic_category_books
        ));

        // 4. Kitchen Items
        products.add(new Product(
                "GC-KITCH-01",
                "Philips Rapid Air Fryer HD9200 (4.1L)",
                CATEGORY_KITCHEN,
                "Like New",
                6800.0,
                "Kamal Uddin",
                "Mirpur 2, Dhaka",
                "Authentic 1400W low-fat air fryer with Rapid Air technology. Includes manual, recipe booklet, and frying basket.",
                R.drawable.ic_category_kitchen
        ));

        products.add(new Product(
                "GC-KITCH-02",
                "Prestige Hard Anodized 5L Pressure Cooker",
                CATEGORY_KITCHEN,
                "Good Condition",
                1250.0,
                "Nasreen Akhter",
                "Lalmatia, Dhaka",
                "Durable induction-compatible heavy base pressure cooker with safety valve replaced recently. Ready for daily cooking.",
                R.drawable.ic_category_kitchen
        ));

        products.add(new Product(
                "GC-KITCH-03",
                "Airtight Borosilicate Glass Spice Jars (Set of 8)",
                CATEGORY_KITCHEN,
                "Brand New",
                0.0, // FREE
                "Zubair Ahmed",
                "Bashundhara R/A, Dhaka",
                "Unused airtight spice containers with bamboo lids. Extra kitchen gift we do not need, free for pickup.",
                R.drawable.ic_category_kitchen
        ));

        // 5. Electronics
        products.add(new Product(
                "GC-ELEC-01",
                "Logitech MK295 Silent Wireless Keyboard & Mouse Combo",
                CATEGORY_ELECTRONICS,
                "Like New",
                1950.0,
                "Mahmudul Hasan",
                "Kawran Bazar, Dhaka",
                "SilentTouch noise reduction technology with 2.4GHz USB nano receiver and full battery health. Working flawlessly.",
                R.drawable.ic_category_electronics
        ));

        products.add(new Product(
                "GC-ELEC-02",
                "Orient High Speed Vintage Table Fan (16 Inch)",
                CATEGORY_ELECTRONICS,
                "Good Condition",
                950.0,
                "Anisur Rahman",
                "Jatrabari, Dhaka",
                "100% copper motor durable oscillating table fan with 3-speed control. Serviced and running smoothly.",
                R.drawable.ic_category_electronics
        ));

        products.add(new Product(
                "GC-ELEC-03",
                "Assorted Micro-USB & Type-C Charging Cables (Pack of 5)",
                CATEGORY_ELECTRONICS,
                "Good Condition",
                0.0, // FREE
                "Fahim Muntasir",
                "Badda, Dhaka",
                "Working fast-charging braided cables tested and functional. Free for students or anyone needing spares.",
                R.drawable.ic_category_electronics
        ));

        // 6. Plastic
        products.add(new Product(
                "GC-PLAS-01",
                "Heavy-Duty Heavy Plastic Storage Tubs (45L, Set of 3)",
                CATEGORY_PLASTIC,
                "Gently Used",
                900.0,
                "Tanvir Chowdhury",
                "Khilgaon, Dhaka",
                "Food-grade BPA-free transparent storage containers with snap-lock handles. Perfect for clothing or tool organization.",
                R.drawable.ic_category_plastic
        ));

        products.add(new Product(
                "GC-PLAS-02",
                "Recyclable Polypropylene Planter Pots (Set of 10)",
                CATEGORY_PLASTIC,
                "Like New",
                0.0, // FREE
                "Maliha Parveen",
                "Uttara Sector 13, Dhaka",
                "8-inch indoor/outdoor flower pots with drainage trays. Great condition, free for gardening enthusiasts.",
                R.drawable.ic_category_plastic
        ));

        // 7. Paper
        products.add(new Product(
                "GC-PAPR-01",
                "Heavy Corrugated Moving Boxes (15 Large Boxes)",
                CATEGORY_PAPER,
                "Gently Used",
                450.0,
                "Imran Hossain",
                "Mohakhali DOHS, Dhaka",
                "Double-wall heavy duty brown cardboard cartons used once for house shifting. Clean, folded flat for easy transport.",
                R.drawable.ic_category_paper
        ));

        products.add(new Product(
                "GC-PAPR-02",
                "Discarded Office Shredded Paper & Craft Rolls for Recycling",
                CATEGORY_PAPER,
                "Recyclable",
                0.0, // FREE
                "Green Office Initiative",
                "Motijheel, Dhaka",
                "Approx 25kg of clean white paper scraps and craft pulp ready for paper recycling or eco-packaging material.",
                R.drawable.ic_category_paper
        ));

        // 8. Metal
        products.add(new Product(
                "GC-METL-01",
                "Foldable Aluminium 5-Step Safety Household Ladder",
                CATEGORY_METAL,
                "Like New",
                2400.0,
                "Badrul Huda",
                "Dhanmondi 8A, Dhaka",
                "Lightweight anti-slip wide step aluminum ladder with rubber grip feet. Rated for up to 150kg load capacity.",
                R.drawable.ic_category_metal
        ));

        products.add(new Product(
                "GC-METL-02",
                "Wrought Iron 3-Tier Indoor Plant & Book Stand",
                CATEGORY_METAL,
                "Good Condition",
                1100.0,
                "Sumiya Akter",
                "Shyamoli, Dhaka",
                "Rust-treated matte black wrought iron corner stand. Very strong and decorative for balcony or living room.",
                R.drawable.ic_category_metal
        ));

        products.add(new Product(
                "GC-METL-03",
                "Scrap Copper & Brass Decorative Utensils for Upcycling",
                CATEGORY_METAL,
                "Recyclable",
                0.0, // FREE
                "Salam Enterprise",
                "Old Dhaka, Kotwali",
                "Assorted brass handles, copper bowls, and vintage metal hardware pieces for artisans or metal recyclers.",
                R.drawable.ic_category_metal
        ));

        return products;
    }

    /**
     * Backward-compatible 3-argument filter method.
     */
    public static List<Product> filter(List<Product> allProducts, String category, String query) {
        return filter(allProducts, category, query, null, null, null, false, null);
    }

    /**
     * Comprehensive multi-filter covering Category, Condition, Transaction Type, Location, and Favorites.
     */
    public static List<Product> filter(List<Product> allProducts,
                                       String category,
                                       String query,
                                       String condition,
                                       String transactionType,
                                       String location,
                                       boolean onlyFavorites,
                                       java.util.Set<String> favoriteIds) {
        List<Product> filtered = new ArrayList<>();
        if (allProducts == null) return filtered;

        String normalizedQuery = query != null ? query.trim().toLowerCase(Locale.getDefault()) : "";
        boolean isAllCategory = category == null
                || category.equalsIgnoreCase(CATEGORY_ALL)
                || category.equalsIgnoreCase("All Categories")
                || category.trim().isEmpty();
        boolean isAllCondition = condition == null || condition.equalsIgnoreCase("All") || condition.trim().isEmpty();
        boolean isAllType = transactionType == null || transactionType.equalsIgnoreCase("All") || transactionType.trim().isEmpty();
        String normalizedLoc = location != null ? location.trim().toLowerCase(Locale.getDefault()) : "";
        boolean isAllLoc = normalizedLoc.isEmpty() || normalizedLoc.equalsIgnoreCase("all");

        for (Product product : allProducts) {
            if (product == null) continue;

            // 1. Favorites filter
            if (onlyFavorites) {
                if (favoriteIds == null || !favoriteIds.contains(product.getProductId())) {
                    continue;
                }
            }

            // 2. Category filter
            if (!isAllCategory) {
                if (product.getCategory() == null || !product.getCategory().equalsIgnoreCase(category)) {
                    continue;
                }
            }

            // 3. Condition filter
            if (!isAllCondition) {
                if (product.getCondition() == null || !product.getCondition().toLowerCase(Locale.getDefault()).contains(condition.toLowerCase(Locale.getDefault()))) {
                    continue;
                }
            }

            // 4. Transaction Type filter
            if (!isAllType) {
                if (product.getTransactionType() == null || !product.getTransactionType().equalsIgnoreCase(transactionType)) {
                    continue;
                }
            }

            // 5. Location filter
            if (!isAllLoc) {
                if (product.getLocation() == null || !product.getLocation().toLowerCase(Locale.getDefault()).contains(normalizedLoc)) {
                    continue;
                }
            }

            // 6. Text query search (matching title, description, or category)
            if (!normalizedQuery.isEmpty()) {
                String title = product.getTitle() != null ? product.getTitle().toLowerCase(Locale.getDefault()) : "";
                String desc = product.getDescription() != null ? product.getDescription().toLowerCase(Locale.getDefault()) : "";
                String cat = product.getCategory() != null ? product.getCategory().toLowerCase(Locale.getDefault()) : "";

                if (!title.contains(normalizedQuery) && !desc.contains(normalizedQuery) && !cat.contains(normalizedQuery)) {
                    continue;
                }
            }

            filtered.add(product);
        }
        return filtered;
    }
}
