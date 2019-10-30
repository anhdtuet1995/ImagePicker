package com.example.demogallery;

import android.graphics.Bitmap;

public class Item {

    private int id;
    private String path;
    private boolean isVideo;
    private long duration;
    private boolean isSelected;

    public Item(int id, String path, boolean isVideo) {
        this.id = id;
        this.path = path;
        this.isVideo = isVideo;
        this.duration = -1;
        this.isSelected = false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public int getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
