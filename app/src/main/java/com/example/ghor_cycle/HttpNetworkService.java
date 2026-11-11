package com.example.ghor_cycle;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service managing direct HTTP/REST API communications, JSON transformation,
 * and error boundaries for GhorCycle.
 *
 * Key Concepts:
 * 1. Firebase SDK vs. Direct HTTP/REST:
 *    - Firebase SDK uses persistent, bi-directional gRPC/WebSocket multiplexed streams with
 *      built-in offline SQLite sync, automatic retry, and proprietary protocol serialization.
 *    - Direct HTTP/REST is a stateless request-response architecture using standard HTTP methods
 *      (GET, POST) over raw TCP/TLS connections. It requires explicit connection lifecycle management,
 *      timeout configuration, status code inspection, manual JSON/XML parsing, and threading control.
 *
 * 2. Asynchronous Execution:
 *    All network requests execute strictly off the UI thread via ExecutorService, posting results
 *    back to the main Looper to prevent Application Not Responding (ANR) lockups.
 *
 * 3. Live Endpoint vs. Offline Fixture:
 *    - Live API: Real, public Open-Meteo Air Quality REST API for Dhaka (Lat 23.8103, Lon 90.4125).
 *    - Local Fixture: Clearly labeled offline JSON fixture for deterministic offline and unit testing.
 *      The service explicitly tags responses with isFromLiveApi to never misrepresent mock data as live.
 */
public class HttpNetworkService {

    private static final String TAG = "GhorCycle_Http";

    // Real, keyless, public REST API endpoint for environmental monitoring (Dhaka, Bangladesh)
    public static final String LIVE_AIR_QUALITY_URL =
            "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=23.8103&longitude=90.4125&current=pm10,pm2_5,european_aqi";

    // Clearly identified local offline JSON fixture for fallback/testing
    public static final String LOCAL_MOCK_AIR_QUALITY_JSON =
            "{\n" +
            "  \"latitude\": 23.8103,\n" +
            "  \"longitude\": 90.4125,\n" +
            "  \"generationtime_ms\": 0.12,\n" +
            "  \"utc_offset_seconds\": 21600,\n" +
            "  \"timezone\": \"Asia/Dhaka\",\n" +
            "  \"current\": {\n" +
            "    \"time\": \"2026-11-11T09:00\",\n" +
            "    \"interval\": 3600,\n" +
            "    \"pm10\": 48.5,\n" +
            "    \"pm2_5\": 24.2,\n" +
            "    \"european_aqi\": 38\n" +
            "  }\n" +
            "}";

    public static final String MALFORMED_JSON_FIXTURE =
            "{ \"latitude\": 23.81, \"current\": { \"pm2_5\": INVALID_SYNTAX ";

    public interface NetworkCallback {
        void onSuccess(EcoAirQuality data, String rawResponse, long latencyMs);
        void onError(int httpStatusCode, String errorMessage);
    }

    private static final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Performs an asynchronous HTTP GET request to the live Open-Meteo REST API.
     */
    public static void fetchLiveAirQuality(final NetworkCallback callback) {
        networkExecutor.execute(() -> {
            long startTime = System.currentTimeMillis();
            HttpURLConnection connection = null;

            try {
                Log.d(TAG, "Initiating HTTP GET to live URL: " + LIVE_AIR_QUALITY_URL);
                URL url = new URL(LIVE_AIR_QUALITY_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000); // 8 second connection timeout
                connection.setReadTimeout(8000);    // 8 second socket read timeout
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "GhorCycle-Android/1.0");

                int statusCode = connection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + statusCode);

                InputStream stream = (statusCode >= 200 && statusCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                String responseBody = readStreamToString(stream);
                long latency = System.currentTimeMillis() - startTime;

                if (statusCode >= 200 && statusCode < 300) {
                    EcoAirQuality quality = parseAirQualityJson(responseBody, true, statusCode);
                    mainHandler.post(() -> callback.onSuccess(quality, responseBody, latency));
                } else {
                    String errorMsg = "HTTP Error " + statusCode + ": " + responseBody;
                    mainHandler.post(() -> callback.onError(statusCode, errorMsg));
                }

            } catch (SocketTimeoutException e) {
                Log.e(TAG, "HTTP request timed out", e);
                mainHandler.post(() -> callback.onError(408, "Request timed out (8s limit reached). Check your network speed."));
            } catch (UnknownHostException e) {
                Log.e(TAG, "Host unreachable / offline", e);
                mainHandler.post(() -> callback.onError(0, "No internet access: Could not resolve air-quality-api.open-meteo.com."));
            } catch (JSONException e) {
                Log.e(TAG, "JSON parse failure on live payload", e);
                mainHandler.post(() -> callback.onError(-1, "Malformed JSON received: " + e.getMessage()));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected networking exception", e);
                mainHandler.post(() -> callback.onError(-1, "Network failure: " + e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    /**
     * Executes JSON parsing on a clearly identified local offline fixture.
     * Guaranteed to run reliably without live network dependency.
     */
    public static void loadOfflineFixture(final NetworkCallback callback) {
        networkExecutor.execute(() -> {
            try {
                Log.d(TAG, "Loading local offline mock fixture (Offline mode test)");
                EcoAirQuality quality = parseAirQualityJson(LOCAL_MOCK_AIR_QUALITY_JSON, false, 200);
                mainHandler.post(() -> callback.onSuccess(quality, LOCAL_MOCK_AIR_QUALITY_JSON, 5));
            } catch (JSONException e) {
                mainHandler.post(() -> callback.onError(-1, "Fixture JSON error: " + e.getMessage()));
            }
        });
    }

    /**
     * Demonstrates resilient error handling when parsing malformed JSON payloads.
     */
    public static void testMalformedJson(final NetworkCallback callback) {
        networkExecutor.execute(() -> {
            try {
                parseAirQualityJson(MALFORMED_JSON_FIXTURE, false, 200);
                mainHandler.post(() -> callback.onError(-1, "Expected JSON parse error, but parsed unexpectedly."));
            } catch (JSONException e) {
                mainHandler.post(() -> callback.onError(-1, "Handled Malformed JSON: " + e.getMessage()));
            }
        });
    }

    /**
     * Transforms raw JSON string into an EcoAirQuality Java domain model object.
     */
    public static EcoAirQuality parseAirQualityJson(String jsonString, boolean isLive, int statusCode) throws JSONException {
        JSONObject root = new JSONObject(jsonString);

        double lat = root.optDouble("latitude", 23.8103);
        double lon = root.optDouble("longitude", 90.4125);

        JSONObject current = root.getJSONObject("current");
        String time = current.optString("time", "2026-11-11");
        double pm25 = current.optDouble("pm2_5", 0.0);
        double pm10 = current.optDouble("pm10", 0.0);
        int aqi = current.optInt("european_aqi", 0);

        return new EcoAirQuality(lat, lon, time, pm25, pm10, aqi, isLive, statusCode);
    }

    private static String readStreamToString(InputStream inputStream) {
        if (inputStream == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
