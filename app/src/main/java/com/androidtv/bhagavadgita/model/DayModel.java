package com.androidtv.bhagavadgita.model;

import com.androidtv.bhagavadgita.comman.SharePreferenceManager;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DayModel {

    private LocalDate date;
    private String primaryDate;     // e.g. "29"
    private String secondaryDate;   // e.g. Gujarati digit or weekday short label
    private String description;     // Panchang label, e.g. "Bhadarvo Krushna Paksh Ekam"
    private boolean isCurrentMonth; // true if this date belongs to the month being displayed
    private boolean isToday;        // true if this date == today

    private String festivalTitle = "";
    public String getFestivalTitle() { return festivalTitle; }
    public void setFestivalTitle(String festivalTitle) { this.festivalTitle = festivalTitle; }


    public DayModel(LocalDate date, String primaryDate, String secondaryDate,
                    String description, boolean isCurrentMonth, boolean isToday) {
        this.date = date;
        this.primaryDate = primaryDate;
        this.secondaryDate = secondaryDate;
        this.description = description;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
    }

    public LocalDate getDate() { return date; }
    public String getPrimaryDate() { return primaryDate; }
    public String getSecondaryDate() { return secondaryDate; }
    public String getDescription() { return description; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean isToday() { return isToday; }

    /**
     * Checks if this day falls on a Sunday.
     */
    public boolean isSunday() {
        return date != null && date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public boolean isPunam() {
        switch (getLanguage()) {
            case "HINDI":
                return description != null && description.contains("पूनम");
            case "GUJARATI":
                return description != null && description.contains("પૂનમ");
            default:
                return description != null && description.contains("Punam");
        }
    }

    public boolean isAmavas() {
        switch (getLanguage()) {
            case "HINDI":
                return description != null && description.contains("अमावस");
            case "GUJARATI":
                return description != null && description.contains("અમાસ");
            default:
                return description != null && description.contains("Amavas");
        }
    }

    @Override
    public String toString() {
        return "DayModel{" +
                "date=" + date +
                ", primaryDate='" + primaryDate + '\'' +
                ", secondaryDate='" + secondaryDate + '\'' +
                ", description='" + description + '\'' +
                ", isCurrentMonth=" + isCurrentMonth +
                ", isToday=" + isToday +
                '}';
    }

    public boolean isFirstDayOfMonth() {
        return date != null && date.getDayOfMonth() == 1;
    }

    public LocalDate getFirstDateOfMonth() {
        return date != null ? date.withDayOfMonth(1) : null;
    }

    public String getEnglishDate() {
        if (primaryDate != null && !primaryDate.isEmpty()) {
            return primaryDate;
        }
        return date != null ? String.valueOf(date.getDayOfMonth()) : "";
    }

    public String getHindiDate() {
        return convertDigits(getEnglishDate(), '\u0966'); // '\u0966' is Devanagari digit 0 (०)
    }

    public String getGujaratiDate() {
        return convertDigits(getEnglishDate(), '\u0AE6'); // '\u0AE6' is Gujarati digit 0 (૦)
    }

    private String convertDigits(String input, char zeroChar) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(input.length());
        for (char ch : input.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                sb.append((char) (zeroChar + (ch - '0')));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String getLanguage() {
        String lang = SharePreferenceManager.getString("LANGUAGE");
        if (lang == null) {
            return "ENGLISH";
        }
        return lang.trim().toUpperCase();
    }
}