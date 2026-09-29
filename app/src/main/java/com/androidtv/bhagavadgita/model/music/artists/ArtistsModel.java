package com.androidtv.bhagavadgita.model.music.artists;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArtistsModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private ArtistsDataModel data;
    private final static long serialVersionUID = 2696063968676377765L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ArtistsDataModel getData() {
        return data;
    }

    public void setData(ArtistsDataModel data) {
        this.data = data;
    }

}