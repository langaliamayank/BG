package com.androidtv.bhagavadgita.comman;

import androidx.leanback.widget.HeaderItem;

import java.util.List;

public class RowHeaderItem extends HeaderItem {
    private List<Object> mList;

    public RowHeaderItem(long id, String name, List<Object> list) {
        super(id, name);
        this.mList = list;
    }

    public List<Object> getList() { return mList; }
}
