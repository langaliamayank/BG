package com.androidtv.bhagavadgita.model;

import android.net.Uri;

public class MediaCard {
    private String id;
    private String writer;
    private String url;
    private String title;
    private String subtitle;
    private Uri imageUri;

    public MediaCard(String id, String writer, String url, String title, String subtitle, Uri imageUri) {
        this.id = id;
        this.writer = writer;
        this.url = url;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public String getWriter() {
        return writer;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}