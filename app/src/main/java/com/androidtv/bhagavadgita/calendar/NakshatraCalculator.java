package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class NakshatraCalculator {

    private static final double SEGMENT = 360.0 / 27.0; // 13.3333 degrees

    private static final String[] NAMES_EN = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu",
            "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta",
            "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
            "Uttara Bhadrapada", "Revati"
    };

    private static final String[] NAMES_HI = {
            "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशिरा", "आर्द्रा", "पुनर्वसु",
            "पुष्य", "आश्लेषा", "मघा", "पूर्वा फाल्गुनी", "उत्तरा फाल्गुनी", "हस्त",
            "चित्रा", "स्वाति", "विशाखा", "अनुराधा", "ज्येष्ठा", "मूल", "पूर्वाषाढ़ा",
            "उत्तराषाढ़ा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वा भाद्रपद",
            "उत्तरा भाद्रपद", "रेवती"
    };

    private static final String[] NAMES_GU = {
            "અશ્વિની", "ભરણી", "કૃતિકા", "રોહિણી", "મૃગશીર્ષ", "આર્દ્રા", "પુનર્વસુ",
            "પુષ્ય", "આશ્લેષા", "મઘા", "પૂર્વા ફાલ્ગુની", "ઉત્તરા ફાલ્ગુની", "હસ્ત",
            "ચિત્રા", "સ્વાતિ", "વિશાખા", "અનુરાધા", "જ્યેષ્ઠા", "મૂળ", "પૂર્વાષાઢા",
            "ઉત્તરાષાઢા", "શ્રવણ", "ધનિષ્ઠા", "શતભિષા", "પૂર્વા ભાદ્રપદ",
            "ઉત્તરા ભાદ્રપદ", "રેવતી"
    };

    public static class Result {
        public int index;               // 0-26
        public String name;             // English
        public String nameHindi;
        public String nameGujarati;
        public LocalDateTime endLocal;  // when this nakshatra ends (local time)

        public String name(Language lang) {
            switch (lang) {
                case HINDI: return nameHindi;
                case GUJARATI: return nameGujarati;
                default: return name;
            }
        }
    }

    private static double siderealMoon(LocalDateTime utc) {
        double T = AstroMath.julianCentury(AstroMath.toJulianDay(utc));
        return AstroMath.normalize(AstroMath.moonLongitude(T) - MasaCalculator.ayanamsaDeg(T));
    }

    public static Result get(LocalDateTime sunriseUtc, double utcOffsetHr) {
        double sid = siderealMoon(sunriseUtc);
        int idx = (int) (sid / SEGMENT);
        double startBoundary = idx * SEGMENT;

        // Moon covers 13.33 deg in about 1 day, so the end is within 36 hours.
        // Binary search on the minutes from sunrise.
        long lo = 0, hi = 36 * 60;
        while (hi - lo > 1) {
            long mid = (lo + hi) / 2;
            double rel = AstroMath.normalize(siderealMoon(sunriseUtc.plusMinutes(mid)) - startBoundary);
            if (rel >= SEGMENT && rel < 180) hi = mid; else lo = mid;
        }
        LocalDateTime endUtc = sunriseUtc.plusMinutes(hi);

        Result r = new Result();
        r.index = idx;
        r.name = NAMES_EN[idx];
        r.nameHindi = NAMES_HI[idx];
        r.nameGujarati = NAMES_GU[idx];
        r.endLocal = endUtc.plusMinutes((long) (utcOffsetHr * 60));
        return r;
    }

    // Add inside the class
    public static Result getForDate(LocalDate date, double latDeg, double lonDeg, double utcOffsetHr) {
        LocalDateTime sunriseUtc = SunriseCalculator.getSunriseUtc(date, latDeg, lonDeg, utcOffsetHr);
        return get(sunriseUtc, utcOffsetHr);
    }

    // Shortcut with your fixed Nathdwara location
    public static Result getForDate(LocalDate date) {
        return getForDate(date, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET);
    }
}