package com.example.ghor_cycle;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates XML parsing in Android using Java and the pull-parser architecture (XmlPullParser).
 *
 * Implements:
 * 1. Stream-based, memory-efficient XML parsing (avoids loading entire DOM into memory).
 * 2. Tag recognition and attribute extraction.
 * 3. Graceful handling of malformed XML or IO interruptions.
 * 4. Transformation of XML records into EcoGuideline Java domain models.
 */
public class XmlEcoParserHelper {

    private static final String TAG = "GhorCycle_XmlParser";

    public static final String SAMPLE_ECO_GUIDELINES_XML =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<guidelines version=\"1.0\">\n" +
            "    <guideline id=\"ECO-01\">\n" +
            "        <title>Cardboard &amp; Paper Box Upcycling</title>\n" +
            "        <category>Paper</category>\n" +
            "        <impactCo2>1.8 kg CO2 saved / kg</impactCo2>\n" +
            "        <instructions>Flatten corrugated moving boxes. Keep dry from rain. Avoid plastic tape when bundling.</instructions>\n" +
            "    </guideline>\n" +
            "    <guideline id=\"ECO-02\">\n" +
            "        <title>Safe Electronic Waste Salvage</title>\n" +
            "        <category>Electronics</category>\n" +
            "        <impactCo2>4.5 kg CO2 saved / item</impactCo2>\n" +
            "        <instructions>Remove lithium batteries before community exchange. Keep circuit boards dry and unbroken.</instructions>\n" +
            "    </guideline>\n" +
            "    <guideline id=\"ECO-03\">\n" +
            "        <title>Ferrous &amp; Non-Ferrous Metal Scrap</title>\n" +
            "        <category>Metal</category>\n" +
            "        <impactCo2>3.2 kg CO2 saved / kg</impactCo2>\n" +
            "        <instructions>Separate aluminium cans from iron rods. Rinse food containers to prevent pest attraction.</instructions>\n" +
            "    </guideline>\n" +
            "</guidelines>";

    /**
     * Parses an XML document from an InputStream and transforms it into a list of EcoGuideline objects.
     */
    public static List<EcoGuideline> parseGuidelines(InputStream inputStream) throws XmlPullParserException, IOException {
        List<EcoGuideline> guidelines = new ArrayList<>();
        if (inputStream == null) return guidelines;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(false);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, "UTF-8");

        int eventType = parser.getEventType();
        String currentTag = "";

        String currentId = "";
        String currentTitle = "";
        String currentCategory = "";
        String currentImpact = "";
        String currentInstructions = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();

            switch (eventType) {
                case XmlPullParser.START_TAG:
                    currentTag = tagName != null ? tagName : "";
                    if ("guideline".equalsIgnoreCase(currentTag)) {
                        currentId = parser.getAttributeValue(null, "id");
                        if (currentId == null) currentId = "ECO-GEN";
                        currentTitle = "";
                        currentCategory = "";
                        currentImpact = "";
                        currentInstructions = "";
                    }
                    break;

                case XmlPullParser.TEXT:
                    String text = parser.getText();
                    if (text != null) {
                        text = text.trim();
                        if (!text.isEmpty()) {
                            if ("title".equalsIgnoreCase(currentTag)) {
                                currentTitle = text;
                            } else if ("category".equalsIgnoreCase(currentTag)) {
                                currentCategory = text;
                            } else if ("impactCo2".equalsIgnoreCase(currentTag)) {
                                currentImpact = text;
                            } else if ("instructions".equalsIgnoreCase(currentTag)) {
                                currentInstructions = text;
                            }
                        }
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if ("guideline".equalsIgnoreCase(tagName)) {
                        guidelines.add(new EcoGuideline(
                                currentId,
                                currentTitle,
                                currentCategory,
                                currentImpact,
                                currentInstructions
                        ));
                    }
                    currentTag = "";
                    break;
            }
            eventType = parser.next();
        }

        Log.d(TAG, "XmlEcoParserHelper: Successfully parsed " + guidelines.size() + " guideline entries");
        return guidelines;
    }

    /**
     * Convenience method to parse guidelines from an XML String.
     */
    public static List<EcoGuideline> parseGuidelinesFromString(String xmlString) throws XmlPullParserException, IOException {
        if (xmlString == null || xmlString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try (ByteArrayInputStream stream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))) {
            return parseGuidelines(stream);
        }
    }
}
