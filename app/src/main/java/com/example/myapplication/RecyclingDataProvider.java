package com.example.myapplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Data provider for the GhorCycle community recycling directory.
 *
 * Implements:
 * 1. Clearly identified, transparent sample community center records for Dhaka urban zones.
 * 2. Multi-parameter filtering by accepted material and neighborhood location.
 * 3. Real geographical coordinates for external navigation intents.
 */
public class RecyclingDataProvider {

    public static final String MATERIAL_ALL = "All Materials";
    public static final String MATERIAL_PLASTIC = "Plastic";
    public static final String MATERIAL_PAPER = "Paper";
    public static final String MATERIAL_METAL = "Metal";
    public static final String MATERIAL_ELECTRONICS = "Electronics";
    public static final String MATERIAL_CLOTHES = "Clothes";
    public static final String MATERIAL_GLASS = "Glass";

    public static List<String> getAvailableMaterials() {
        return Arrays.asList(
                MATERIAL_ALL,
                MATERIAL_PLASTIC,
                MATERIAL_PAPER,
                MATERIAL_METAL,
                MATERIAL_ELECTRONICS,
                MATERIAL_CLOTHES,
                MATERIAL_GLASS
        );
    }

    /**
     * Returns curated community recycling hubs in Dhaka.
     * All entries are explicitly tagged with isSampleData = true to prevent fabricating real entities.
     */
    public static List<RecyclingCenter> getSampleCenters() {
        List<RecyclingCenter> list = new ArrayList<>();

        list.add(new RecyclingCenter(
                "RC-DHK-01",
                "Tejgaon Industrial Material Recovery Facility",
                "Tejgaon Industrial Area, Dhaka",
                Arrays.asList(MATERIAL_METAL, MATERIAL_PLASTIC, MATERIAL_PAPER),
                "+880 1711-000001",
                "tejgaon.mrf@sample.ghorcycle.org",
                "Sat - Thu: 8:30 AM - 5:30 PM",
                23.7644, 90.3982,
                true,
                "Accepts sorted scrap metals, heavy industrial cardboard, and HDPE/PET bulk plastic."
        ));

        list.add(new RecyclingCenter(
                "RC-DHK-02",
                "Mirpur E-Waste & Battery Safe Drop-off",
                "Mirpur Section 10 (Near Roundabout), Dhaka",
                Arrays.asList(MATERIAL_ELECTRONICS, MATERIAL_METAL),
                "+880 1711-000002",
                "mirpur.ewaste@sample.ghorcycle.org",
                "Sun - Fri: 10:00 AM - 7:00 PM",
                23.8069, 90.3687,
                true,
                "Certified hazardous electronic component separation, circuit boards, and lead-acid battery recovery."
        ));

        list.add(new RecyclingCenter(
                "RC-DHK-03",
                "Dhanmondi Community Paper & Plastic Upcycling Hub",
                "Road 27 (Old), Dhanmondi, Dhaka",
                Arrays.asList(MATERIAL_PAPER, MATERIAL_PLASTIC, MATERIAL_CLOTHES),
                "+880 1711-000003",
                "dhanmondi.eco@sample.ghorcycle.org",
                "Everyday: 9:00 AM - 8:00 PM",
                23.7533, 90.3769,
                true,
                "Neighborhood collection point for carton packaging, PET bottles, clean textiles, and newspapers."
        ));

        list.add(new RecyclingCenter(
                "RC-DHK-04",
                "Old Dhaka Brass & Copper Reclamation Yard",
                "Kotwali / Islampur, Old Dhaka",
                Arrays.asList(MATERIAL_METAL, MATERIAL_GLASS),
                "+880 1711-000004",
                "olddhaka.metal@sample.ghorcycle.org",
                "Sat - Thu: 9:00 AM - 6:00 PM",
                23.7099, 90.4071,
                true,
                "Specialized in scrap copper wire, brass fittings, iron rods, and culinary glass containers."
        ));

        list.add(new RecyclingCenter(
                "RC-DHK-05",
                "Uttara Green Circular Economy Center",
                "Sector 3, Jashimuddin Avenue, Uttara, Dhaka",
                Arrays.asList(MATERIAL_CLOTHES, MATERIAL_PAPER, MATERIAL_PLASTIC),
                "+880 1711-000005",
                "uttara.green@sample.ghorcycle.org",
                "Mon - Sat: 9:30 AM - 6:30 PM",
                23.8682, 90.3985,
                true,
                "Textile sorting for community clothing charity redistribution and paper shredding."
        ));

        return list;
    }

    public static List<RecyclingCenter> filterCenters(String materialFilter, String query) {
        return filterCenters(getSampleCenters(), materialFilter, query);
    }

    /**
     * Filters centers by accepted material and search text query (name or address).
     */
    public static List<RecyclingCenter> filterCenters(List<RecyclingCenter> allCenters,
                                                      String materialFilter,
                                                      String query) {
        List<RecyclingCenter> filtered = new ArrayList<>();
        if (allCenters == null) return filtered;

        String normalizedQuery = query != null ? query.trim().toLowerCase(Locale.getDefault()) : "";
        boolean isAllMaterials = materialFilter == null
                || materialFilter.equalsIgnoreCase("All")
                || materialFilter.equalsIgnoreCase(MATERIAL_ALL)
                || materialFilter.trim().isEmpty();

        for (RecyclingCenter center : allCenters) {
            if (center == null) continue;

            // 1. Material filter
            boolean matchesMaterial = isAllMaterials;
            if (!isAllMaterials && center.getAcceptedMaterials() != null) {
                for (String m : center.getAcceptedMaterials()) {
                    if (m.equalsIgnoreCase(materialFilter)) {
                        matchesMaterial = true;
                        break;
                    }
                }
            }

            if (!matchesMaterial) continue;

            // 2. Query filter (matches name, address, or notes)
            if (!normalizedQuery.isEmpty()) {
                String name = center.getName().toLowerCase(Locale.getDefault());
                String address = center.getAddressOrArea().toLowerCase(Locale.getDefault());
                String notes = center.getNotes().toLowerCase(Locale.getDefault());

                if (!name.contains(normalizedQuery) && !address.contains(normalizedQuery) && !notes.contains(normalizedQuery)) {
                    continue;
                }
            }

            filtered.add(center);
        }

        return filtered;
    }
}
