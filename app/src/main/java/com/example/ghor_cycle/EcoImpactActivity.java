package com.example.ghor_cycle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * EcoImpactActivity displays the verified environmental impact dashboard for GhorCycle.
 *
 * Implements:
 * 1. Strict verification: Computes metrics only from verified transaction records
 *    (ACCEPTED requests and COMPLETED product handovers).
 * 2. Deduplication: Unique transaction IDs ensure records are never double-counted.
 * 3. Defensible indicators: Waste diverted (kg), CO2e avoided (kg), and water conserved (L).
 * 4. Transparent methodology card documenting all assumptions.
 */
public class EcoImpactActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_EcoImpact";

    private ImageButton btnBack;
    private TextView tvHeroTotalReused;
    private TextView tvStatDonations;
    private TextView tvStatExchanges;
    private TextView tvStatReuses;
    private TextView tvMetricWaste;
    private TextView tvMetricCo2;
    private TextView tvMetricWater;
    private TextView tvLedgerSummary;
    private TextView tvMethodologyNote;
    private LinearLayout containerVerifiedTransactions;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_impact);

        currentUser = FirebaseAuthService.getCurrentUser();

        initViews();
        setupListeners();
        loadImpactData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnEcoImpactBack);
        tvHeroTotalReused = findViewById(R.id.tvHeroTotalReused);
        tvStatDonations = findViewById(R.id.tvStatDonations);
        tvStatExchanges = findViewById(R.id.tvStatExchanges);
        tvStatReuses = findViewById(R.id.tvStatReuses);
        tvMetricWaste = findViewById(R.id.tvMetricWaste);
        tvMetricCo2 = findViewById(R.id.tvMetricCo2);
        tvMetricWater = findViewById(R.id.tvMetricWater);
        tvLedgerSummary = findViewById(R.id.tvLedgerSummary);
        tvMethodologyNote = findViewById(R.id.tvMethodologyNote);
        containerVerifiedTransactions = findViewById(R.id.containerVerifiedTransactions);

        if (tvMethodologyNote != null) {
            tvMethodologyNote.setText(EcoImpactCalculator.METHODOLOGY_ASSUMPTION_NOTE);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadImpactData() {
        String currentUid = FirebaseAuthService.getCurrentUserId();
        final String uid = (currentUid != null) ? currentUid : "guest-user";

        // Step 1: Fetch user requests
        InteractionService.fetchUserRequests(uid, new InteractionService.RequestListCallback() {
            @Override
            public void onLoaded(List<ItemRequest> requests) {
                // Step 2: Fetch products owned by user
                FirestoreProductService.listenToProducts(new FirestoreProductService.ProductListCallback() {
                    @Override
                    public void onLoaded(List<Product> allProducts) {
                        List<Product> userProducts = new ArrayList<>();
                        if (allProducts != null) {
                            for (Product p : allProducts) {
                                if (p != null && uid.equals(p.getOwnerId())) {
                                    userProducts.add(p);
                                }
                            }
                        }

                        // Compute verified, deduplicated impact
                        EcoImpactSummary summary = EcoImpactCalculator.calculateUserImpact(uid, requests, userProducts);
                        displaySummary(summary);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Error fetching products for eco-impact: " + errorMessage);
                        // Fallback to requests only
                        EcoImpactSummary summary = EcoImpactCalculator.calculateUserImpact(uid, requests, new ArrayList<>());
                        displaySummary(summary);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error fetching requests for eco-impact: " + errorMessage);
                EcoImpactSummary summary = EcoImpactCalculator.calculateUserImpact(uid, new ArrayList<>(), new ArrayList<>());
                displaySummary(summary);
            }
        });
    }

    private void displaySummary(EcoImpactSummary summary) {
        if (summary == null) return;

        int total = summary.getTotalItemsReused();
        tvHeroTotalReused.setText(total == 1 ? "1 Item" : total + " Items");
        tvStatDonations.setText(String.valueOf(summary.getSuccessfulDonationsCount()));
        tvStatExchanges.setText(String.valueOf(summary.getCompletedExchangesCount()));
        tvStatReuses.setText(String.valueOf(summary.getCompletedSalesReuseCount()));

        tvMetricWaste.setText(String.format(Locale.US, "%.1f kg", summary.getEstimatedWasteDivertedKg()));
        tvMetricCo2.setText(String.format(Locale.US, "%.1f kg", summary.getEstimatedCo2AvoidedKg()));
        tvMetricWater.setText(String.format(Locale.US, "%,d L", (long) summary.getEstimatedWaterSavedLiters()));

        int txCount = summary.getVerifiedTransactionsCount();
        tvLedgerSummary.setText(txCount + " unique verified transaction" + (txCount == 1 ? "" : "s") +
                " (Deduplicated; Pending & rejected strictly excluded)");

        containerVerifiedTransactions.removeAllViews();
        if (summary.getVerifiedTransactionIds().isEmpty()) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No completed or accepted reuse transactions recorded yet. Complete an exchange or donation to generate verified eco-impact records.");
            tvEmpty.setTextSize(12);
            tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvEmpty.setPadding(0, 16, 0, 16);
            containerVerifiedTransactions.addView(tvEmpty);
        } else {
            for (String txId : summary.getVerifiedTransactionIds()) {
                TextView tvRow = new TextView(this);
                tvRow.setText("✔ Record Ref: " + txId + " — Status: VERIFIED HANDOVER");
                tvRow.setTextSize(12);
                tvRow.setTextColor(ContextCompat.getColor(this, R.color.brand_primary_dark));
                tvRow.setPadding(0, 6, 0, 6);
                containerVerifiedTransactions.addView(tvRow);
            }
        }
    }
}
