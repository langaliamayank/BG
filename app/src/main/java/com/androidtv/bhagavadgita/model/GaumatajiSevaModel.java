package com.androidtv.bhagavadgita.model;

import java.io.Serializable;

public class GaumatajiSevaModel implements Serializable {
    int id;
    String title;
    int price;

    public GaumatajiSevaModel(int id, String title, int price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
