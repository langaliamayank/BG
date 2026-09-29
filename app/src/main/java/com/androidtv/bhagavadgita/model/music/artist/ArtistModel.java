package com.androidtv.bhagavadgita.model.music.artist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ArtistModel implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private ArtistDataModel data;
    private final static long serialVersionUID = -1468970970410728424L;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ArtistDataModel getData() {
        return data;
    }

    public void setData(ArtistDataModel data) {
        this.data = data;
    }

}
