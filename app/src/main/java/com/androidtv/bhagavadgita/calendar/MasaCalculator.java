package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDateTime;

public class MasaCalculator {

    private static final String[] MASA_NAMES_EN = {
            "Vaishakh", "Jeth", "Ashadh", "Shravan", "Bhadarvo", "Aaso",
            "Kartak", "Magshar", "Posh", "Maha", "Fagan", "Chaitra"
    }; // index 0 = Mesha(Aries) .. 11 = Meena(Pisces)

    private static final String[] MASA_NAMES_HI = {
            "वैशाख", "जेठ", "आषाढ़", "श्रावण", "भादो", "आसो",
            "कार्तिक", "मगसर", "पौष", "माघ", "फागुन", "चैत्र"
    };

    private static final String[] MASA_NAMES_GU = {
            "વૈશાખ", "જેઠ", "અષાઢ", "શ્રાવણ", "ભાદરવો", "આસો",
            "કારતક", "માગશર", "પોષ", "મહા", "ફાગણ", "ચૈત્ર"
    };

    public static class MasaResult {
        public int masaIndex;          // 0-11, useful if you need the raw rashi index elsewhere
        public String masaName;        // English (kept as-is for backward compatibility)
        public String masaNameHindi;
        public String masaNameGujarati;
        public boolean isAdhik;

        public String name(Language lang) {
            switch (lang) {
                case HINDI: return masaNameHindi;
                case GUJARATI: return masaNameGujarati;
                default: return masaName;
            }
        }
    }

    private static double ayanamsaDeg(double T) {
        // Standard Lahiri Ayanamsa calculation based on Julian Century T
        double seconds = 50.23884 * (2000.0 + T * 100.0 - 2000.0) + 0.000111 * Math.pow(2000.0 + T * 100.0 - 2000.0, 2);
        // Base Lahiri Ayanamsa for J2000.0 is approximately 23° 51' 11" (23.853°)
        return 23.853 + (seconds / 3600.0);
    }

    private static double siderealSunLongitude(LocalDateTime utc) {
        double T = AstroMath.julianCentury(AstroMath.toJulianDay(utc));
        return AstroMath.normalize(AstroMath.sunLongitude(T) - ayanamsaDeg(T));
    }

    /**
     * Finds the Amavasya (new moon instant) nearest to the given UTC time.
     */
    private static LocalDateTime findNearestAmavasya(LocalDateTime start) {
        double diff = AstroMath.moonSunDiff(start);
        double delta = (diff <= 180) ? -diff : (360 - diff);
        LocalDateTime guess = start.plusMinutes((long) (delta / 12.19 * 24 * 60));

        for (int i = 0; i < 5; i++) {
            double d = AstroMath.moonSunDiff(guess);
            double signedD = (d <= 180) ? d : d - 360;
            guess = guess.plusMinutes((long) (-signedD / 12.19 * 24 * 60));
        }
        return guess;
    }

    /**
     * Masa (lunar month) name for the given UTC instant, with Adhik Maas detection.
     */
    public static MasaResult getMasa(LocalDateTime utcDateTime) {
        LocalDateTime thisAmavasya = findNearestAmavasya(utcDateTime);
        int rashiIdx = (int) (siderealSunLongitude(thisAmavasya) / 30.0);

        LocalDateTime prevAmavasya = findNearestAmavasya(thisAmavasya.minusDays(20));
        int prevRashiIdx = (int) (siderealSunLongitude(prevAmavasya) / 30.0);

        MasaResult r = new MasaResult();
        r.masaIndex = rashiIdx;
        r.masaName = MASA_NAMES_EN[rashiIdx];
        r.masaNameHindi = MASA_NAMES_HI[rashiIdx];
        r.masaNameGujarati = MASA_NAMES_GU[rashiIdx];
        r.isAdhik = (rashiIdx == prevRashiIdx);
        return r;
    }
}