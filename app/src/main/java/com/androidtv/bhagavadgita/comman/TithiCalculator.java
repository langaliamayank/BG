package com.androidtv.bhagavadgita.comman;

import java.util.HashMap;
import java.util.Map;

/**
 * IMPORTANT: Panchang paksha/month boundaries shift every year because they
 * follow the lunar calendar, not the Gregorian calendar. This class must be
 * populated with data PER YEAR (e.g. from a Panchang API or yearly ephemeris
 * export). A single "MONTH_DAY" table is only ever correct for one year.
 *
 * Key format: "YEAR_MONTH_DAY" (Month is 0-indexed: January = 0)
 */
public class TithiCalculator {

    private static final Map<String, String> panchangMap = new HashMap<>();

    static {
        // Example for year 2026 only — replace/extend with real data per year.
        putRange(2026, 0, 1, 15, "Paush Krushna Paksh");
        putRange(2026, 0, 16, 31, "Paush Shukla Paksh");

        putRange(2026, 1, 1, 13, "Magh Krushna Paksh");
        putRange(2026, 1, 14, 28, "Magh Shukla Paksh");
        // Leap year Feb 29 must be added explicitly when applicable:
        // panchangMap.put("2026_1_29", "Magh Shukla Paksh");

        putRange(2026, 2, 1, 14, "Phalguna Krushna Paksh");
        putRange(2026, 2, 15, 31, "Chaitra Shukla Paksh");

        putRange(2026, 3, 1, 12, "Chaitra Krushna Paksh");
        putRange(2026, 3, 13, 30, "Vaishakha Shukla Paksh");

        putRange(2026, 4, 1, 12, "Vaishakha Krushna Paksh");
        putRange(2026, 4, 13, 31, "Jyeshtha Shukla Paksh");

        putRange(2026, 5, 1, 11, "Jyeshtha Krushna Paksh");
        putRange(2026, 5, 12, 30, "Ashadha Shukla Paksh");

        putRange(2026, 6, 1, 10, "Ashadha Krushna Paksh");
        putRange(2026, 6, 11, 31, "Shravana Shukla Paksh");

        putRange(2026, 7, 1, 11, "Shravana Krushna Paksh");
        putRange(2026, 7, 12, 28, "Shravana Shukla Paksh");
        putRange(2026, 7, 29, 31, "Bhadarao Krushna Paksh");

        putRange(2026, 8, 1, 10, "Bhadarao Krushna Paksh");
        putRange(2026, 8, 11, 30, "Bhadarao Shukla Paksh");

        putRange(2026, 9, 1, 10, "Bhadarao Shukla Paksh");
        putRange(2026, 9, 11, 25, "Ashwin Krushna Paksh");
        putRange(2026, 9, 26, 31, "Ashwin Shukla Paksh");

        putRange(2026, 10, 1, 9, "Ashwin Shukla Paksh");
        putRange(2026, 10, 10, 24, "Kartik Krushna Paksh");
        putRange(2026, 10, 25, 30, "Kartik Shukla Paksh");

        putRange(2026, 11, 1, 8, "Kartik Shukla Paksh");
        putRange(2026, 11, 9, 23, "Margashirsha Krushna Paksh");
        putRange(2026, 11, 24, 31, "Margashirsha Shukla Paksh");
    }

    private static void putRange(int year, int month, int startDay, int endDay, String value) {
        for (int day = startDay; day <= endDay; day++) {
            panchangMap.put(year + "_" + month + "_" + day, value);
        }
    }

    public static String getDescription(int year, int month, int day) {
        String key = year + "_" + month + "_" + day;
        return panchangMap.getOrDefault(key, "Panchang Details");
    }
}