package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDateTime;

public class TithiCalculator {

    private static final String[] TITHI_NAMES_EN = {
            "Ekam", "Beej", "Tij", "Choth", "Pancham", "Chhath", "Satam",
            "Atham", "Navam", "Dasham", "Gyaras", "Baras", "Teras", "Chaudash"
    };

    private static final String[] TITHI_NAMES_HI = {
            "एकम", "बीज/दूज", "तीज", "चौथ", "पंचम", "छठ", "सातम",
            "आठम", "नोम", "दशम", "ग्यारस", "बारस", "तेरस", "चौदस"
    };

    private static final String[] TITHI_NAMES_GU = {
            "એકમ", "બીજ", "ત્રીજ", "ચોથ", "પાંચમ", "છઠ", "સાતમ",
            "આઠમ", "નોમ", "દશમ", "અગિયારસ", "બારસ", "તેરસ", "ચૌદસ"
    };

    public static final String[] HINDI_NUMERALS = {
            "१", "२", "३", "४", "५", "६", "७", "८", "९", "१०", "११", "१२", "१३", "१४", "१५"
    };

    // Amavas is conventionally the 30th tithi of the lunar month, not the 15th —
    // Purnima (15) and Amavas (30) are both "inPaksha==15" but display differently.
    private static final String AMAVAS_NUMERAL = "३०";
    private static final String PURNIMA_NUMERAL = "१५";

    private static final String PAKSHA_SHUKLA_EN = "Shukla";
    private static final String PAKSHA_KRUSHNA_EN = "Krushna";
    private static final String PAKSHA_SHUKLA_HI = "शुक्ल";
    private static final String PAKSHA_KRUSHNA_HI = "कृष्ण";
    private static final String PAKSHA_SHUKLA_GU = "સુદ";
    private static final String PAKSHA_KRUSHNA_GU = "વદ";

    public static class Result {
        public int tithiNumber;          // 1-30
        public String paksha;            // English: "Shukla" / "Krushna"
        public String pakshaHindi;       // "शुक्ल" / "कृष्ण"
        public String pakshaGujarati;    // "સુદ" / "વદ"
        public String tithiName;         // English: Ekam..Chaudash, Punam/Amavas
        public String tithiNameHindi;
        public String tithiNameGujarati;
        public String tithiNumeralScript; // traditional numeral for the corner display

        public String label() {
            return label(Language.ENGLISH);
        }

        public String label(Language lang) {
            switch (lang) {
                case HINDI:
                    return pakshaHindi + " पक्ष " + tithiNameHindi;
                case GUJARATI:
                    return tithiNameGujarati + " " + pakshaGujarati;
                default:
                    return paksha + " Paksh " + tithiName;
            }
        }
    }

    public static Result getTithi(LocalDateTime utcDateTime) {
        double diff = AstroMath.moonSunDiff(utcDateTime);
        // Add a small epsilon to prevent precision drift right at boundaries
        int tithiNum = (int) Math.floor(diff / 12.0) + 1;
        tithiNum = Math.max(1, Math.min(30, tithiNum)); // clamp safely between 1 and 30

        Result r = new Result();
        r.tithiNumber = tithiNum;

        int inPaksha = tithiNum > 15 ? tithiNum - 15 : tithiNum;
        boolean isShukla = tithiNum <= 15;

        r.paksha = isShukla ? PAKSHA_SHUKLA_EN : PAKSHA_KRUSHNA_EN;
        r.pakshaHindi = isShukla ? PAKSHA_SHUKLA_HI : PAKSHA_KRUSHNA_HI;
        r.pakshaGujarati = isShukla ? PAKSHA_SHUKLA_GU : PAKSHA_KRUSHNA_GU;

        if (inPaksha == 15) {
            r.tithiName = isShukla ? "Punam" : "Amavas";
            r.tithiNameHindi = isShukla ? "पूनम" : "अमावस";
            r.tithiNameGujarati = isShukla ? "પૂનમ" : "અમાસ";
            r.tithiNumeralScript = isShukla ? PURNIMA_NUMERAL : AMAVAS_NUMERAL; // 15 vs 30
        } else {
            r.tithiName = TITHI_NAMES_EN[inPaksha - 1];
            r.tithiNameHindi = TITHI_NAMES_HI[inPaksha - 1];
            r.tithiNameGujarati = TITHI_NAMES_GU[inPaksha - 1];
            r.tithiNumeralScript = HINDI_NUMERALS[inPaksha - 1];
        }

        return r;
    }
}