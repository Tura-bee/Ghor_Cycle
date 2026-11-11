package com.example.ghor_cycle;

import org.json.JSONException;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests verifying Stage 7 Networking, JSON deserialization, and XML parsing:
 * 1. JSON parsing and transformation to EcoAirQuality domain model.
 * 2. Malformed JSON boundary testing and exception containment.
 * 3. XML parsing via XmlPullParser into EcoGuideline list.
 * 4. Empty and edge-case XML document handling.
 * 5. Strict distinction between live REST API data and offline mock fixtures.
 * 6. AQI categorization and contextual eco recommendations.
 */
public class Stage7NetworkingUnitTest {

    @Test
    public void testJsonParsingOfOfflineFixture() throws JSONException {
        EcoAirQuality quality = HttpNetworkService.parseAirQualityJson(
                HttpNetworkService.LOCAL_MOCK_AIR_QUALITY_JSON,
                false,
                200
        );

        assertNotNull(quality);
        assertEquals(23.8103, quality.getLatitude(), 0.001);
        assertEquals(90.4125, quality.getLongitude(), 0.001);
        assertEquals(24.2, quality.getPm25(), 0.01);
        assertEquals(48.5, quality.getPm10(), 0.01);
        assertEquals(38, quality.getEuropeanAqi());
        assertEquals("Fair", quality.getAqiCategory());
        assertEquals(200, quality.getHttpStatusCode());
        assertFalse("Offline mock fixture must not claim to be a live response", quality.isFromLiveApi());
        assertNotNull(quality.getEcoRecommendation());
    }

    @Test(expected = JSONException.class)
    public void testMalformedJsonThrowsExceptionSafely() throws JSONException {
        // Must throw JSONException rather than hanging or passing invalid state
        HttpNetworkService.parseAirQualityJson(HttpNetworkService.MALFORMED_JSON_FIXTURE, false, 200);
    }

    @Test
    public void testXmlEcoParserSampleDocument() throws XmlPullParserException, IOException {
        List<EcoGuideline> guidelines = XmlEcoParserHelper.parseGuidelinesFromString(
                XmlEcoParserHelper.SAMPLE_ECO_GUIDELINES_XML
        );

        assertNotNull(guidelines);
        assertEquals(3, guidelines.size());

        // Guideline 1: Paper
        EcoGuideline g1 = guidelines.get(0);
        assertEquals("ECO-01", g1.getId());
        assertEquals("Cardboard & Paper Box Upcycling", g1.getTitle());
        assertEquals("Paper", g1.getCategory());
        assertEquals("1.8 kg CO2 saved / kg", g1.getImpactCo2());
        assertTrue(g1.getInstructions().contains("Flatten corrugated"));

        // Guideline 2: Electronics
        EcoGuideline g2 = guidelines.get(1);
        assertEquals("ECO-02", g2.getId());
        assertEquals("Safe Electronic Waste Salvage", g2.getTitle());
        assertEquals("Electronics", g2.getCategory());

        // Guideline 3: Metal
        EcoGuideline g3 = guidelines.get(2);
        assertEquals("ECO-03", g3.getId());
        assertEquals("Ferrous & Non-Ferrous Metal Scrap", g3.getTitle());
        assertEquals("Metal", g3.getCategory());
    }

    @Test
    public void testXmlParserEmptyDocumentReturnsEmptyList() throws XmlPullParserException, IOException {
        List<EcoGuideline> nullResult = XmlEcoParserHelper.parseGuidelinesFromString(null);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        List<EcoGuideline> emptyResult = XmlEcoParserHelper.parseGuidelinesFromString("");
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    public void testLiveVsFixtureFlagIntegrity() {
        EcoAirQuality liveQuality = new EcoAirQuality(
                23.8103, 90.4125, "2026-11-11T09:00", 25.0, 50.0, 45, true, 200
        );
        assertTrue("Live API responses must report isFromLiveApi == true", liveQuality.isFromLiveApi());

        EcoAirQuality fixtureQuality = new EcoAirQuality(
                23.8103, 90.4125, "2026-11-11T09:00", 25.0, 50.0, 45, false, 200
        );
        assertFalse("Fixture responses must report isFromLiveApi == false", fixtureQuality.isFromLiveApi());
    }

    @Test
    public void testAqiCategoryThresholds() {
        EcoAirQuality good = new EcoAirQuality(0, 0, "", 5, 10, 15, false, 200);
        assertEquals("Good (Clean Air)", good.getAqiCategory());

        EcoAirQuality fair = new EcoAirQuality(0, 0, "", 15, 25, 35, false, 200);
        assertEquals("Fair", fair.getAqiCategory());

        EcoAirQuality moderate = new EcoAirQuality(0, 0, "", 30, 60, 55, false, 200);
        assertEquals("Moderate", moderate.getAqiCategory());

        EcoAirQuality poor = new EcoAirQuality(0, 0, "", 55, 90, 75, false, 200);
        assertEquals("Poor", poor.getAqiCategory());

        EcoAirQuality veryPoor = new EcoAirQuality(0, 0, "", 80, 140, 95, false, 200);
        assertEquals("Very Poor", veryPoor.getAqiCategory());

        EcoAirQuality hazardous = new EcoAirQuality(0, 0, "", 120, 200, 120, false, 200);
        assertEquals("Hazardous", hazardous.getAqiCategory());
    }
}
