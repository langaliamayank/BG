package com.androidtv.bhagavadgita.model.music.album;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AlbumModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private AlbumDataModel data;
    private final static long serialVersionUID = -6433021701154023332L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public AlbumDataModel getData() {
        return data;
    }

    public void setData(AlbumDataModel data) {
        this.data = data;
    }

}