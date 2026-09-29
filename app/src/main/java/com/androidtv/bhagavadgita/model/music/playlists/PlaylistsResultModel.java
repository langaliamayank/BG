package com.androidtv.bhagavadgita.model.music.playlists;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PlaylistsResultModel implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("image")
    @Expose
    private List<PlaylistsImageModel> image;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("songCount")
    @Expose
    private Integer songCount;
    @SerializedName("language")
    @Expose
    private String language;
    @SerializedName("explicitContent")
    @Expose
    private Boolean explicitContent;
    private final static long serialVersionUID = -3057697008765374857L;

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

    public List<PlaylistsImageModel> getImage() {
        return image;
    }

    public void setImage(List<PlaylistsImageModel> image) {
        this.image = image;
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
}
