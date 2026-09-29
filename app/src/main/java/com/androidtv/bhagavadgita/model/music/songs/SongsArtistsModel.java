package com.androidtv.bhagavadgita.model.music.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SongsArtistsModel implements Serializable {

    @SerializedName("primary")
    @Expose
    private List<SongsPrimaryModel> primary;
    @SerializedName("featured")
    @Expose
    private List<Object> featured;
    @SerializedName("all")
    @Expose
    private List<SongsAllModel> all;
    private final static long serialVersionUID = -5947171204985931304L;

    public List<SongsPrimaryModel> getPrimary() {
        return primary;
    }

    public void setPrimary(List<SongsPrimaryModel> primary) {
        this.primary = primary;
    }

    public List<Object> getFeatured() {
        return featured;
    }

    public void setFeatured(List<Object> featured) {
        this.featured = featured;
    }

    public List<SongsAllModel> getAll() {
        return all;
    }

    public void setAll(List<SongsAllModel> all) {
        this.all = all;
    }

}
