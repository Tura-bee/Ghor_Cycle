package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclingDirectoryActivity displays verified community recycling centers in Dhaka.
 *
 * Implements:
 * 1. RecyclerView listing with transparent sample community hub tags.
 * 2. Real-time material category and location query filtering.
 * 3. External map navigation via geo: URI intents (only when valid coordinates exist).
 * 4. Contact dialing via tel: Intent.
 * 5. Dynamic empty search states.
 */
public class RecyclingDirectoryActivity extends AppCompatActivity {

    private List<RecyclingCenter> masterCenterList;
    private List<RecyclingCenter> displayedCenterList;

    private String selectedMaterial = RecyclingDataProvider.MATERIAL_ALL;
    private String currentQuery = "";

    private RecyclingCenterAdapter adapter;

    private EditText etSearch;
    private ImageButton btnClearSearch;
    private LinearLayout layoutMaterialChips;
    private TextView tvCount;
    private TextView btnReset;
    private RecyclerView rvCenters;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycling_directory);

        bindViews();
        setupMaterialChips();
        setupSearchAndReset();
        setupRecyclerView();

        loadDirectoryData();
    }

    private void bindViews() {
        ImageButton btnBack = findViewById(R.id.btnDirectoryBack);
        btnBack.setOnClickListener(v -> finish());

        etSearch = findViewById(R.id.etDirectorySearch);
        btnClearSearch = findViewById(R.id.btnDirectoryClearSearch);
        layoutMaterialChips = findViewById(R.id.layoutMaterialChipsContainer);
        tvCount = findViewById(R.id.tvDirectoryCount);
        btnReset = findViewById(R.id.btnDirectoryResetFilters);
        rvCenters = findViewById(R.id.rvRecyclingCenters);
        layoutEmpty = findViewById(R.id.layoutDirectoryEmpty);
    }

    private void setupMaterialChips() {
        layoutMaterialChips.removeAllViews();
        List<String> materials = RecyclingDataProvider.getAvailableMaterials();

        for (String mat : materials) {
            TextView chip = new TextView(this);
            chip.setText(mat);
            chip.setTextSize(12);
            chip.setPadding(28, 14, 28, 14);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(lp);

            updateChipVisualState(chip, mat.equalsIgnoreCase(selectedMaterial));

            chip.setOnClickListener(v -> {
                selectedMaterial = mat;
                setupMaterialChips(); // Refresh visual state
                applyFilters();
            });

            layoutMaterialChips.addView(chip);
        }
    }

    private void updateChipVisualState(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_category_chip_selected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            chip.setBackgroundResource(R.drawable.bg_category_chip_unselected);
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void setupSearchAndReset() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                currentQuery = s != null ? s.toString() : "";
                btnClearSearch.setVisibility(currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentQuery = "";
            applyFilters();
        });

        btnReset.setOnClickListener(v -> {
            etSearch.setText("");
            currentQuery = "";
            selectedMaterial = RecyclingDataProvider.MATERIAL_ALL;
            setupMaterialChips();
            applyFilters();
        });
    }

    private void setupRecyclerView() {
        rvCenters.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclingCenterAdapter(displayedCenterList, new RecyclingCenterAdapter.OnCenterActionListener() {
            @Override
            public void onGetDirections(RecyclingCenter center) {
                launchMapDirections(center);
            }

            @Override
            public void onContactCenter(RecyclingCenter center) {
                launchPhoneDialer(center);
            }
        });
        rvCenters.setAdapter(adapter);
    }

    private void loadDirectoryData() {
        masterCenterList = RecyclingDataProvider.getSampleCenters();
        applyFilters();
    }

    private void applyFilters() {
        displayedCenterList = RecyclingDataProvider.filterCenters(masterCenterList, selectedMaterial, currentQuery);
        adapter.updateCenters(displayedCenterList);

        boolean hasActiveFilter = !selectedMaterial.equalsIgnoreCase(RecyclingDataProvider.MATERIAL_ALL)
                || !currentQuery.trim().isEmpty();

        btnReset.setVisibility(hasActiveFilter ? View.VISIBLE : View.GONE);

        if (displayedCenterList.isEmpty()) {
            rvCenters.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            tvCount.setText("0 centers found");
        } else {
            rvCenters.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            String label = "Showing " + displayedCenterList.size() + (displayedCenterList.size() == 1 ? " center" : " centers");
            if (!selectedMaterial.equalsIgnoreCase(RecyclingDataProvider.MATERIAL_ALL)) {
                label += " for " + selectedMaterial;
            }
            tvCount.setText(label);
        }
    }

    private void launchMapDirections(RecyclingCenter center) {
        if (!center.hasValidCoordinates()) {
            Toast.makeText(this, "Coordinates not available for this center.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Standard geo: navigation intent
        String geoUri = "geo:" + center.getLatitude() + "," + center.getLongitude()
                + "?q=" + Uri.encode(center.getName() + ", " + center.getAddressOrArea());

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            // Browser map fallback if no dedicated map app is installed
            String webMapUrl = "https://www.google.com/maps/search/?api=1&query="
                    + center.getLatitude() + "," + center.getLongitude();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webMapUrl)));
        }
    }

    private void launchPhoneDialer(RecyclingCenter center) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + center.getContactPhone().replaceAll("[^0-9+]", "")));
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Phone: " + center.getContactPhone(), Toast.LENGTH_LONG).show();
        }
    }
}
