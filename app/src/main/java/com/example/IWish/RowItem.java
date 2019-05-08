package com.example.IWish;

public class RowItem {
    private int imageId;
    private String title;
    private boolean isPublic;

    public RowItem(int imageId, String title, boolean isPublic) {
        this.imageId = imageId;
        this.title = title;
        this.isPublic = isPublic;
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
