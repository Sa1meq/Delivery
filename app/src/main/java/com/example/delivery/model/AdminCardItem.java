package com.example.delivery.model;

public class AdminCardItem {
    private final int imageResId;
    private final String title;

    public AdminCardItem(int imageResId, String title) {
        this.imageResId = imageResId;
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }
}
