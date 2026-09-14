package com.androidtv.bhagavadgita.model;

import java.util.ArrayList;
import java.util.List;

public class VersesCache {
    private static VersesCache instance;
    private List<VersesModel> verses = new ArrayList<>();   // full verses-of-the-day list
    private List<VersesModel> activeList = new ArrayList<>(); // whichever list DetailActivity should page through

    public static VersesCache getInstance() {
        if (instance == null) instance = new VersesCache();
        return instance;
    }

    public void setVerses(List<VersesModel> list) { this.verses = list; }
    public ArrayList<VersesModel> getVerses() { return new ArrayList<>(verses); }

    public void setActiveList(List<VersesModel> list) { this.activeList = list; }
    public ArrayList<VersesModel> getActiveList() { return new ArrayList<>(activeList); }

    public VersesModel getVerseById(int id) {
        for (VersesModel v : activeList) if (v.getId() == id) return v;
        for (VersesModel v : verses) if (v.getId() == id) return v; // fallback
        return null;
    }
}