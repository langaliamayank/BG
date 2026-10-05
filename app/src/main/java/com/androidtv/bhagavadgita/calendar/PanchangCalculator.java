package com.androidtv.bhagavadgita.calendar;

import com.androidtv.bhagavadgita.comman.SharePreferenceManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public class PanchangCalculator {

    public static class PanchangResult {
        public int samvatYear;
        public String masaName;
        public String masaNameHindi;
        public String masaNameGujarati;
        public boolean isAdhikMasa;
        public String paksha;
        public String pakshaHindi;
        public String pakshaGujarati;
        public String tithiName;
        public String tithiNameHindi;
        public String tithiNameGujarati;
        public int tithiNumber;
        private String tithiNumeralScript;
        private String nakshatra;
        private LocalDateTime nakshatraTime;

        public String getNumberScript() {
            return tithiNumeralScript;
        }

        public String getDescription() {
            switch (getLanguage()) {
                case "HINDI":
                    return (isAdhikMasa ? "अधिक " : "") + masaNameHindi + " " + pakshaHindi + " पक्ष, " + tithiNameHindi;
                case "GUJARATI":
                    return (isAdhikMasa ? "અધિક " : "") + masaNameGujarati + " " + pakshaGujarati + " પક્ષ, " + tithiNameGujarati;
                default:
                    return (isAdhikMasa ? "Adhik " : "") + masaName + " " + paksha + " Paksh, " + tithiName;
            }
        }

        /** Matches the exact order you asked for: "Choth, 2083, Shravan, Krushna Paksh" */
        public String getCompactDescription() {
            switch (getLanguage()) {
                case "HINDI":
                    return tithiNameHindi + ", " + samvatYear + ", " + masaNameHindi + ", " + pakshaHindi + " पक्ष";
                case "GUJARATI":
                    return tithiNameGujarati + ", " + samvatYear + ", " + masaNameGujarati + ", " + pakshaGujarati + " પક્ષ";
                default:
                    return tithiName + ", " + samvatYear + ", " + masaName + ", " + paksha + " Paksh";
            }

//            return masaName + ", " + paksha + " Paksh " + tithiName + ", " + samvatYear;
        }

        public String getTithi() {
            switch (getLanguage()) {
                case "HINDI":
                    return String.format("%s, %s पक्ष, %s", masaNameHindi, pakshaHindi, tithiNameHindi);
                case "GUJARATI":
                    return String.format("%s, %s, %s", masaNameGujarati, pakshaGujarati, tithiNameGujarati);
                case "ENGLISH":
                default:
                    return String.format("%s, %s Paksh, %s", masaName, paksha, tithiName);
            }
        }

        public String getVS() {
            switch (getLanguage()) {
                case "HINDI":
                    return "विक्रम संवत, " + getVikramSamvat();
                case "GUJARATI":
                    return "વિક્રમ સંવત, " + getVikramSamvat();
                default:
                    return "Vikram Samvat, " + getVikramSamvat();
            }
        }

        public boolean isShuklaPaksh() {
            switch (getLanguage()) {
                case "HINDI":
                    return "शुक्ल".equalsIgnoreCase(pakshaHindi);
                case "GUJARATI":
                    return "સુદ".equalsIgnoreCase(pakshaGujarati);
                default:
                    return  "Shukla".equalsIgnoreCase(paksha);
            }
        }

        public Integer getVSYear() {
            return samvatYear;
        }

        public String getVikramSamvat() {
            switch (getLanguage()) {
                case "HINDI":
                    return formatNumber(samvatYear, new java.util.Locale("hi"));
                case "GUJARATI":
                    return formatNumber(samvatYear, new java.util.Locale("gu"));
                default:
                    return  formatNumber(samvatYear, new java.util.Locale("en"));
            }
        }

        private String formatNumber(int number, Locale locale) {
            Locale numLocale;
            switch (locale.getLanguage()) {
                case "hi":
                    numLocale = Locale.forLanguageTag("hi-IN-u-nu-deva"); // ०१२३...
                    break;
                case "gu":
                    numLocale = Locale.forLanguageTag("gu-IN-u-nu-gujr"); // ૦૧૨૩...
                    break;
                default:
                    return String.valueOf(number); // 2026
            }
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(numLocale);
            nf.setGroupingUsed(false);
            return nf.format(number);
        }
    }

    public static PanchangResult getPanchang(LocalDate date, double latDeg, double lonDeg, double utcOffsetHr) {
        LocalDateTime sunriseUtc = SunriseCalculator.getSunriseUtc(date, latDeg, lonDeg, utcOffsetHr);

        TithiCalculator.Result activeTithi = TithiCalculator.getTithi(sunriseUtc);
        MasaCalculator.MasaResult masa = MasaCalculator.getMasa(sunriseUtc);
        int samvatYear = VikramSamvatCalculator.getYear(sunriseUtc, date);
        NakshatraCalculator.Result nak = NakshatraCalculator.get(sunriseUtc, utcOffsetHr);

        PanchangResult result = new PanchangResult();
        result.samvatYear = samvatYear;
        result.masaName = masa.masaName;
        result.masaNameHindi = masa.masaNameHindi;
        result.masaNameGujarati = masa.masaNameGujarati;
        result.isAdhikMasa = masa.isAdhik;
        result.paksha = activeTithi.paksha;
        result.pakshaHindi = activeTithi.pakshaHindi;
        result.pakshaGujarati = activeTithi.pakshaGujarati;
        result.tithiName = activeTithi.tithiName;
        result.tithiNameHindi = activeTithi.tithiNameHindi;
        result.tithiNameGujarati = activeTithi.tithiNameGujarati;
        result.tithiNumber = activeTithi.tithiNumber;
        result.tithiNumeralScript = activeTithi.tithiNumeralScript;
        result.nakshatra = nak.name;
        result.nakshatraTime = nak.endLocal;
        return result;
    }

    public static String getDescription(LocalDate date) {
        return getPanchang(date, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getDescription();
    }

    public static NakshatraCalculator.Result getNakshatra(LocalDate localDate){
       return NakshatraCalculator.getForDate(localDate);
    }

    public static String getLanguage() {
        String lang = SharePreferenceManager.getString("LANGUAGE");
        if (lang == null) {
            return "ENGLISH";
        }
        return lang.trim().toUpperCase();
    }
}