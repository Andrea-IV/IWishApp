package com.example.IWish;

import com.example.IWish.Model.Wishlist;

public class RowWishList {
    private long id;
    private int imageId;
    private String title;
    private boolean isPublic;
    private Wishlist wishlist;


    public RowWishList(Wishlist wishlist) {
        this.id = wishlist.id;
        this.imageId = 1;
        this.title = wishlist.name;
        this.isPublic = wishlist.isPublic;
    }

    public Wishlist getWishlist(){ return wishlist; }
    public long getId() {
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
