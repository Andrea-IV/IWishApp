package com.example.IWish;

public class WishedItem {
    private int id;
    private String name;
    private String description;
    private Float amount;
    private String image;
    private String link;
    private int position;
    private String Category;

    public WishedItem(int id, String name, String description, Float amount, String image, String link, int position, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.image = image;
        this.link = link;
        this.position = position;
        Category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }
}
