package com.example.IWish;

public class RowWishList {
    private int id;
    private int imageId;
    private String title;
    private boolean isPublic;

    public RowWishList(int id, int imageId, String title, boolean isPublic) {
        this.id = id;
        this.imageId = imageId;
        this.title = title;
        this.isPublic = isPublic;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public boolean isPublic() {
        return isPublic;
    }
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    @Override
    public String toString() {
        return title;
    }
}
