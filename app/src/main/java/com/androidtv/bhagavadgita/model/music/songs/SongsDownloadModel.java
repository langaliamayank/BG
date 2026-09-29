package com.androidtv.bhagavadgita.model.music.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SongsDownloadModel implements Serializable {

    @SerializedName("quality")
    @Expose
    private String quality;
    @SerializedName("url")
    @Expose
    private String url;
    private final static long serialVersionUID = -3723436997490173218L;

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
