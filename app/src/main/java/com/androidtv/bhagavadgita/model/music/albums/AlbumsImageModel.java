package com.androidtv.bhagavadgita.model.music.albums;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AlbumsImageModel implements Serializable {

    @SerializedName("quality")
    @Expose
    private String quality;
    @SerializedName("url")
    @Expose
    private String url;
    private final static long serialVersionUID = -4457201364658081248L;

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}