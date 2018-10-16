package com.simple.sd.sharkfeed.data.modal;

public class Item {
    private String title;
    private String description;
    private String imageLowURL;
    private String imageHighURL;
    private String originalURL;
    private long itemId;

    public Item() {
        title = "Not Available";
        description = "Not Available";
        imageLowURL = null;
        imageHighURL = null;
        originalURL = null;
        itemId = 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageLowURL() {
        return imageLowURL;
    }

    public void setImageLowURL(String imageLowURL) {
        this.imageLowURL = imageLowURL;
    }

    public String getImageHighURL() {
        return imageHighURL;
    }

    public void setImageHighURL(String imageHighURL) {
        this.imageHighURL = imageHighURL;
    }

    public String getOriginalURL() {
        return originalURL;
    }

    public void setOriginalURL(String originalURL) {
        this.originalURL = originalURL;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}
