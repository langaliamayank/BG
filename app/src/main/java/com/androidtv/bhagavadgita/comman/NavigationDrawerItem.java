package com.androidtv.bhagavadgita.comman;

import com.androidtv.bhagavadgita.R;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerItem {

    private String title;
    private int imageId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public static List<NavigationDrawerItem> getData() {
        List<NavigationDrawerItem> dataList = new ArrayList<>();

        int[] imageIds = getImages();
        String[] titles = getTitles();

        for (int i = 0; i < titles.length; i++) {
            NavigationDrawerItem navItem = new NavigationDrawerItem();
            navItem.setTitle(titles[i]);
            navItem.setImageId(imageIds[i]);
            dataList.add(navItem);
        }
        return dataList;
    }

    private static int[] getImages() {

        return new int[]{
                R.drawable.ic_action_cottage, R.drawable.ic_action_calendar, R.drawable.ic_action_darshan_book,
                R.drawable.ic_action_kirtan, R.drawable.ic_action_bell, R.drawable.ic_action_settings};
    }

    private static String[] getTitles() {

        return new String[]{
                "Home", "Daily Darshan", "Bhagavad Gita", "Kirtan", "Notification", "Settings"};
    }
}