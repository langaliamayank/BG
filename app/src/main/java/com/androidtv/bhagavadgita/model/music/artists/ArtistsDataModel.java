package com.androidtv.bhagavadgita.model.music.artists;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ArtistsDataModel implements Serializable {

    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("start")
    @Expose
    private Integer start;
    @SerializedName("results")
    @Expose
    private List<ArtistsResultModel> results;
    private final static long serialVersionUID = 6898459405255798998L;

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

    public List<ArtistsResultModel> getResults() {
        return results;
    }

    public void setResults(List<ArtistsResultModel> results) {
        this.results = results;
    }
}
