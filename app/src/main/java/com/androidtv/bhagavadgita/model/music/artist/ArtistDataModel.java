package com.androidtv.bhagavadgita.model.music.artist;

import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ArtistDataModel implements Serializable {

    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("start")
    @Expose
    private Integer start;
    @SerializedName("songs")
    @Expose
    private List<SongsResultModel> songs;
    private final static long serialVersionUID = -8609660463809979344L;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public List<SongsResultModel> getSongs() {
        return songs;
    }

    public void setSongs(List<SongsResultModel> songs) {
        this.songs = songs;
    }
}
