package com.androidtv.bhagavadgita.model.music.playlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PlaylistModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private PlaylistDataModel data;
    private final static long serialVersionUID = 1269368261836986470L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public PlaylistDataModel getData() {
        return data;
    }

    public void setData(PlaylistDataModel data) {
        this.data = data;
    }
}
