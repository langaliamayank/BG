package com.androidtv.bhagavadgita.model.music.playlists;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlaylistsModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private PlaylistsDataModel data;
    private final static long serialVersionUID = 5428573956856040542L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public PlaylistsDataModel getData() {
        return data;
    }

    public void setData(PlaylistsDataModel data) {
        this.data = data;
    }

}