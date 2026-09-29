package com.androidtv.bhagavadgita.model.music.album;

import com.androidtv.bhagavadgita.model.music.albums.AlbumsArtistsModel;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsImageModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class AlbumDataModel implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("year")
    @Expose
    private Integer year;
    @SerializedName("playCount")
    @Expose
    private Object playCount;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("explicitContent")
    @Expose
    private Boolean explicitContent;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("songCount")
    @Expose
    private Integer songCount;
    @SerializedName("artists")
    @Expose
    private AlbumsArtistsModel artists;
    @SerializedName("image")
    @Expose
    private List<AlbumsImageModel> image;
    @SerializedName("songs")
    @Expose
    private List<SongsResultModel> songs;
    private final static long serialVersionUID = 7154307970460620295L;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSongCount() {
        return songCount;
    }

    public void setSongCount(Integer songCount) {
        this.songCount = songCount;
    }

    public AlbumsArtistsModel getArtists() {
        return artists;
    }

    public void setArtists(AlbumsArtistsModel artists) {
        this.artists = artists;
    }

    public List<AlbumsImageModel> getImage() {
        return image;
    }

    public void setImage(List<AlbumsImageModel> image) {
        this.image = image;
    }

    public List<SongsResultModel> getSongs() {
        return songs;
    }

    public void setSongs(List<SongsResultModel> songs) {
        this.songs = songs;
    }

}
