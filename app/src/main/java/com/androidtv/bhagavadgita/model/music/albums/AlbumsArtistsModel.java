package com.androidtv.bhagavadgita.model.music.albums;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AlbumsArtistsModel implements Serializable {

    @SerializedName("primary")
    @Expose
    private List<AlbumsPrimaryModel> albumsPrimaryModel;
    @SerializedName("featured")
    @Expose
    private List<Object> featured;
    @SerializedName("all")
    @Expose
    private List<AlbumsAllModel> albumsAllModel;
    private final static long serialVersionUID = -5947171204985931304L;

    public List<AlbumsPrimaryModel> getPrimary() {
        return albumsPrimaryModel;
    }

    public void setPrimary(List<AlbumsPrimaryModel> albumsPrimaryModel) {
        this.albumsPrimaryModel = albumsPrimaryModel;
    }

    public List<Object> getFeatured() {
        return featured;
    }

    public void setFeatured(List<Object> featured) {
        this.featured = featured;
    }

    public List<AlbumsAllModel> getAll() {
        return albumsAllModel;
    }

    public void setAll(List<AlbumsAllModel> albumsAllModel) {
        this.albumsAllModel = albumsAllModel;
    }

}