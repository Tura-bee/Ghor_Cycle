package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * EcoInsightActivity demonstrates Android networking, JSON deserialization,
 * XML parsing, and asynchronous UI updates for GhorCycle.
 *
 * Implements:
 * 1. Live asynchronous HTTP GET request using HttpURLConnection.
 * 2. Background thread execution with main-thread Handler UI delivery.
 * 3. JSON parsing into EcoAirQuality domain models.
 * 4. Clear distinction between live REST API data and offline mock fixtures.
 * 5. Stream-based XML parsing via XmlPullParser into EcoGuideline objects.
 * 6. Resilient error state management (timeouts, offline, malformed responses).
 */
public class EcoInsightActivity extends AppCompatActivity {

    private static final String TAG = "GhorCycle_EcoInsight";

    private ImageButton btnBack;
    private TextView tvSourceBadge;
    private TextView tvHttpDetails;
    private ProgressBar pbLoading;
    private TextView tvErrorBanner;
    private LinearLayout layoutMetricsContainer;
    private TextView tvPm25;
    private TextView tvPm10;
    private TextView tvAqi;
    private TextView tvEcoAdvice;
    private TextView btnToggleRawJson;
    private TextView tvRawJsonPayload;

    private MaterialButton btnFetchLive;
    private MaterialButton btnLoadFixture;
    private MaterialButton btnTestMalformed;

    private LinearLayout containerXmlGuidelines;
    private MaterialButton btnParseXml;

    private boolean isRawJsonExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eco_insight);
        Log.d(TAG, "EcoInsightActivity: onCreate() - Initializing networking hub");

        bindViews();
        setupListeners();

        // Perform initial live request
        fetchLiveAirQualityData();

        // Perform initial XML parsing demonstration
        parseAndDisplayXmlGuidelines();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.btnEcoBack);
        tvSourceBadge = findViewById(R.id.tvEcoSourceBadge);
        tvHttpDetails = findViewById(R.id.tvEcoHttpDetails);
        pbLoading = findViewById(R.id.pbEcoLoading);
        tvErrorBanner = findViewById(R.id.tvEcoErrorBanner);
        layoutMetricsContainer = findViewById(R.id.layoutEcoMetricsContainer);
        tvPm25 = findViewById(R.id.tvPm25Value);
        tvPm10 = findViewById(R.id.tvPm10Value);
        tvAqi = findViewById(R.id.tvAqiValue);
        tvEcoAdvice = findViewById(R.id.tvEcoAdvice);
        btnToggleRawJson = findViewById(R.id.btnToggleRawJson);
        tvRawJsonPayload = findViewById(R.id.tvRawJsonPayload);

        btnFetchLive = findViewById(R.id.btnFetchLiveApi);
        btnLoadFixture = findViewById(R.id.btnLoadOfflineFixture);
        btnTestMalformed = findViewById(R.id.btnTestMalformedJson);

        containerXmlGuidelines = findViewById(R.id.containerXmlGuidelines);
        btnParseXml = findViewById(R.id.btnParseXmlDocument);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFetchLive.setOnClickListener(v -> fetchLiveAirQualityData());

        btnLoadFixture.setOnClickListener(v -> loadLocalFixtureData());

        btnTestMalformed.setOnClickListener(v -> testMalformedJsonHandling());

        btnToggleRawJson.setOnClickListener(v -> {
            isRawJsonExpanded = !isRawJsonExpanded;
            tvRawJsonPayload.setVisibility(isRawJsonExpanded ? View.VISIBLE : View.GONE);
            btnToggleRawJson.setText(isRawJsonExpanded ? "▾ Hide Raw HTTP JSON Payload" : "▸ Show Raw HTTP JSON Payload");
        });

        btnParseXml.setOnClickListener(v -> parseAndDisplayXmlGuidelines());
    }

    private void fetchLiveAirQualityData() {
        setLoadingState(true);
        tvErrorBanner.setVisibility(View.GONE);

        HttpNetworkService.fetchLiveAirQuality(new HttpNetworkService.NetworkCallback() {
            @Override
            public void onSuccess(EcoAirQuality data, String rawResponse, long latencyMs) {
                setLoadingState(false);
                displayAirQuality(data, rawResponse, latencyMs);
                Toast.makeText(EcoInsightActivity.this, "Live HTTP 200 OK (" + latencyMs + " ms)", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int httpStatusCode, String errorMessage) {
                setLoadingState(false);
                displayError("Live Request Failed (Status " + httpStatusCode + "): " + errorMessage);
            }
        });
    }

    private void loadLocalFixtureData() {
        setLoadingState(true);
        tvErrorBanner.setVisibility(View.GONE);

        HttpNetworkService.loadOfflineFixture(new HttpNetworkService.NetworkCallback() {
            @Override
            public void onSuccess(EcoAirQuality data, String rawResponse, long latencyMs) {
                setLoadingState(false);
                displayAirQuality(data, rawResponse, latencyMs);
                Toast.makeText(EcoInsightActivity.this, "Loaded Local Offline Test Fixture", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int httpStatusCode, String errorMessage) {
                setLoadingState(false);
                displayError(errorMessage);
            }
        });
    }

    private void testMalformedJsonHandling() {
        setLoadingState(true);
        tvErrorBanner.setVisibility(View.GONE);

        HttpNetworkService.testMalformedJson(new HttpNetworkService.NetworkCallback() {
            @Override
            public void onSuccess(EcoAirQuality data, String rawResponse, long latencyMs) {
                setLoadingState(false);
            }

            @Override
            public void onError(int httpStatusCode, String errorMessage) {
                setLoadingState(false);
                displayError(errorMessage);
                Toast.makeText(EcoInsightActivity.this, "Handled Malformed JSON gracefully without crash!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAirQuality(EcoAirQuality data, String rawResponse, long latencyMs) {
        if (data.isFromLiveApi()) {
            tvSourceBadge.setText("LIVE REST API (Open-Meteo)");
            tvSourceBadge.setBackgroundResource(R.drawable.bg_role_tag);
            tvSourceBadge.setTextColor(ContextCompat.getColor(this, R.color.eco_green_dark));
        } else {
            tvSourceBadge.setText("LOCAL OFFLINE FIXTURE");
            tvSourceBadge.setBackgroundResource(R.drawable.bg_badge_condition);
            tvSourceBadge.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }

        String details = "HTTP " + data.getHttpStatusCode() + " OK • Latency: " + latencyMs
                + " ms • Lat: " + data.getLatitude() + "°N, Lon: " + data.getLongitude() + "°E";
        tvHttpDetails.setText(details);

        tvPm25.setText(String.format(java.util.Locale.US, "%.1f µg/m³", data.getPm25()));
        tvPm10.setText(String.format(java.util.Locale.US, "%.1f µg/m³", data.getPm10()));
        tvAqi.setText(data.getEuropeanAqi() + " (" + data.getAqiCategory() + ")");
        tvEcoAdvice.setText(data.getEcoRecommendation());

        tvRawJsonPayload.setText(rawResponse);
        layoutMetricsContainer.setVisibility(View.VISIBLE);
    }

    private void displayError(String message) {
        tvErrorBanner.setText(message);
        tvErrorBanner.setVisibility(View.VISIBLE);
        tvHttpDetails.setText("Request Failed • See error log below");
    }

    private void setLoadingState(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnFetchLive.setEnabled(!isLoading);
        btnLoadFixture.setEnabled(!isLoading);
        btnTestMalformed.setEnabled(!isLoading);
    }

    private void parseAndDisplayXmlGuidelines() {
        containerXmlGuidelines.removeAllViews();
        try {
            List<EcoGuideline> guidelines = XmlEcoParserHelper.parseGuidelinesFromString(XmlEcoParserHelper.SAMPLE_ECO_GUIDELINES_XML);

            for (EcoGuideline g : guidelines) {
                TextView tvItem = new TextView(this);
                tvItem.setTextSize(13);
                tvItem.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                tvItem.setLineSpacing(2, 1);
                tvItem.setPadding(0, 4, 0, 8);

                String formatted = "• [" + g.getCategory() + "] " + g.getTitle()
                        + "\n  Impact: " + g.getImpactCo2()
                        + "\n  Action: " + g.getInstructions();
                tvItem.setText(formatted);

                containerXmlGuidelines.addView(tvItem);
            }
            Toast.makeText(this, "XML Parsed: " + guidelines.size() + " guidelines loaded", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "XML parse error", e);
            TextView tvError = new TextView(this);
            tvError.setText("Failed to parse XML: " + e.getMessage());
            tvError.setTextColor(ContextCompat.getColor(this, R.color.status_danger));
            containerXmlGuidelines.addView(tvError);
        }
    }
}
