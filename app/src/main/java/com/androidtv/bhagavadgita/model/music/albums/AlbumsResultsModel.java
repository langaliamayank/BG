package com.androidtv.bhagavadgita.model.music.albums;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AlbumsResultsModel implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("playCount")
    @Expose
    private Object playCount;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("explicitContent")
    @Expose
    private Boolean explicitContent;
    @SerializedName("artists")
    @Expose
    private AlbumsArtistsModel albumsArtistsModel;
    @SerializedName("image")
    @Expose
    private List<AlbumsImageModel> albumsImageModel;
    private final static long serialVersionUID = 6148340485413444721L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Object playCount) {
        this.playCount = playCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getExplicitContent() {
        return explicitContent;
    }

    public void setExplicitContent(Boolean explicitContent) {
        this.explicitContent = explicitContent;
    }

    public AlbumsArtistsModel getArtists() {
        return albumsArtistsModel;
    }

    public void setArtists(AlbumsArtistsModel albumsArtistsModel) {
        this.albumsArtistsModel = albumsArtistsModel;
    }

    public List<AlbumsImageModel> getImage() {
        return albumsImageModel;
    }

    public void setImage(List<AlbumsImageModel> albumsImageModel) {
        this.albumsImageModel = albumsImageModel;
    }

}