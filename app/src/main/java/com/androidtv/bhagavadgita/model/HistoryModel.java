package com.androidtv.bhagavadgita.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HistoryModel implements Serializable {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("titleHi")
    @Expose
    private String titleHi;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("descriptionHi")
    @Expose
    private String descriptionHi;
    @SerializedName("image")
    @Expose
    private String image;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleHi() {
        return titleHi;
    }

    public void setTitleHi(String titleHi) {
        this.titleHi = titleHi;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionHi() {
        return descriptionHi;
    }

    public void setDescriptionHi(String descriptionHi) {
        this.descriptionHi = descriptionHi;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
