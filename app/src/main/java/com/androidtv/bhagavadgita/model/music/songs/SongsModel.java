package com.androidtv.bhagavadgita.model.music.songs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SongsModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private SongsDataModel data;
    private final static long serialVersionUID = -8338775105350048260L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public SongsDataModel getData() {
        return data;
    }

    public void setData(SongsDataModel data) {
        this.data = data;
    }

}