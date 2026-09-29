package com.androidtv.bhagavadgita.model.music.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SongsResultModel implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("year")
    @Expose
    private String year;
    @SerializedName("releaseDate")
    @Expose
    private Object releaseDate;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("explicitContent")
    @Expose
    private Boolean explicitContent;
    @SerializedName("playCount")
    @Expose
    private Integer playCount;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("hasLyrics")
    @Expose
    private Boolean hasLyrics;
    @SerializedName("lyricsId")
    @Expose
    private Object lyricsId;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("copyright")
    @Expose
    private String copyright;
    @SerializedName("album")
    @Expose
    private SongsAlbumModel album;
    @SerializedName("artists")
    @Expose
    private SongsArtistsModel artists;
    @SerializedName("image")
    @Expose
    private List<SongsImageModel> image;
    @SerializedName("downloadUrl")
    @Expose
    private List<SongsDownloadModel> downloadUrl;
    private boolean playing = false;

    private final static long serialVersionUID = 8286012560238778267L;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Object getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Object releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Boolean getExplicitContent() {
        return explicitContent;
    }

    public void setExplicitContent(Boolean explicitContent) {
        this.explicitContent = explicitContent;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getHasLyrics() {
        return hasLyrics;
    }

    public void setHasLyrics(Boolean hasLyrics) {
        this.hasLyrics = hasLyrics;
    }

    public Object getLyricsId() {
        return lyricsId;
    }

    public void setLyricsId(Object lyricsId) {
        this.lyricsId = lyricsId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public SongsAlbumModel getAlbum() {
        return album;
    }

    public void setAlbum(SongsAlbumModel album) {
        this.album = album;
    }

    public SongsArtistsModel getArtists() {
        return artists;
    }

    public void setArtists(SongsArtistsModel artists) {
        this.artists = artists;
    }

    public List<SongsImageModel> getImage() {
        return image;
    }

    public void setImage(List<SongsImageModel> image) {
        this.image = image;
    }

    public List<SongsDownloadModel> getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(List<SongsDownloadModel> downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongsResultModel that = (SongsResultModel) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
