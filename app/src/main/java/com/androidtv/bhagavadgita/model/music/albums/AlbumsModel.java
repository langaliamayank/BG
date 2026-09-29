package com.androidtv.bhagavadgita.model.music.albums;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AlbumsModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private AlbumsDataModel albumsDataModel;
    private final static long serialVersionUID = 2872449325725317390L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public AlbumsDataModel getData() {
        return albumsDataModel;
    }

    public void setData(AlbumsDataModel albumsDataModel) {
        this.albumsDataModel = albumsDataModel;
    }

}