package com.androidtv.bhagavadgita.model;

import java.io.Serializable;

public class ActionModel implements Serializable {

    private long Id;
    private boolean Selected = false;
    private String title;
    private int Icon;

    public ActionModel(long index, String title, boolean selected, int icon) {
        this.Id = index;
        this.title = title;
        this.Selected = selected;
        this.Icon = icon;
    }

    public ActionModel() {
    }

    public long getId() {
        return Id;
    }

    public void setId(long mId) {
        this.Id = mId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return Selected;
    }

    public void setSelected(boolean mSelected) {
        this.Selected = mSelected;
    }

    public int getIcon() {
        return Icon;
    }

    public void setIcon(int icon) {
        Icon = icon;
    }
}
