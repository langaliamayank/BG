package com.androidtv.bhagavadgita.model;

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
        return description != null && description.contains("Punam");
    }

    public boolean isAmavas() {
        return description != null && description.contains("Amavas");
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
}