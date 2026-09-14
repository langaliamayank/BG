package com.androidtv.bhagavadgita.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

        public String getNumberScript() {
            return tithiNumeralScript;
        }

        public String getDescription() {
            return getDescription(Language.ENGLISH);
        }

        public String getDescription(Language lang) {
            switch (lang) {
                case HINDI:
                    return (isAdhikMasa ? "अधिक " : "") + masaNameHindi + " " + pakshaHindi + " पक्ष, " + tithiNameHindi;
                case GUJARATI:
                    return (isAdhikMasa ? "અધિક " : "") + masaNameGujarati + " " + tithiNameGujarati + ", " + pakshaGujarati;
                default:
                    return (isAdhikMasa ? "Adhik " : "") + masaName + " " + paksha + " Paksh, " + tithiName;
            }
        }

        /** Matches the exact order you asked for: "Choth, 2083, Shravan, Krushna Paksh" */
        public String getCompactDescription() {
            return tithiName + ", " + samvatYear + ", " + masaName + ", " + paksha + " Paksh";
//            return masaName + ", " + paksha + " Paksh " + tithiName + ", " + samvatYear;
        }

        public String getTithi() {
            return tithiName + ", " + masaName + ", " + paksha + " Paksh";
        }

        public String getVS(Language lang) {
            /*Vikram Samvat*/
            switch (lang) {
                case HINDI:
                    return "विक्रम संवत, " + samvatYear;
                case GUJARATI:
                    return "વિક્રમ સંવત, " + samvatYear;
                default:
                    return "Vikram Samvat, " + samvatYear;
            }
        }

        public boolean isShuklaPaksh() {
            return "Shukla".equalsIgnoreCase(paksha);
        }

        public boolean isKrushnaPaksh() {
            return "Krushna".equalsIgnoreCase(paksha);
        }

        public Integer getVSYear() {
            return samvatYear;
        }
    }

    public static PanchangResult getPanchang(LocalDate date, double latDeg, double lonDeg, double utcOffsetHr) {
        LocalDateTime sunriseUtc = SunriseCalculator.getSunriseUtc(date, latDeg, lonDeg, utcOffsetHr);

        TithiCalculator.Result activeTithi = TithiCalculator.getTithi(sunriseUtc);
        MasaCalculator.MasaResult masa = MasaCalculator.getMasa(sunriseUtc);
        int samvatYear = VikramSamvatCalculator.getYear(sunriseUtc, date);

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
        return result;
    }

    public static String getDescription(LocalDate date) {
        return getDescription(date, Language.ENGLISH);
    }

    public static String getDescription(LocalDate date, Language lang) {
        return getPanchang(date, CalendarUtils.LAT, CalendarUtils.LON, CalendarUtils.UTC_OFFSET).getDescription(lang);
    }
}