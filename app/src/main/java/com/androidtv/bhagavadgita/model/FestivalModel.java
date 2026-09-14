package com.androidtv.bhagavadgita.model;

import java.io.Serializable;

public class FestivalModel implements Serializable {
    String title;
    String tithi;
    String description;
    String date;

    public FestivalModel(String title, String tithi, String description, String date){
        this.title = title;
        this.tithi = tithi;
        this.description = description;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTithi() {
        return tithi;
    }

    public void setTithi(String tithi) {
        this.tithi = tithi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}