package com.example.IWish.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Item extends Model {

    public String name;
    public String description;
    public float amount;
    public String image;
    public String link;
    public int position;
    public Wishlist wishlist;
    public long wishlistId;

    public Item() {
    }

    public Item(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.name = (String) (json.get("name"));
            this.description = (String) (json.get("description"));
            this.amount = (float) (json.get("amount"));
            this.image = (String) (json.get("image"));
            this.link = (String) (json.get("link"));
            this.position = (int) (json.get("position"));

            this.wishlistId = (int) (json.get("wishlistId"));

            JSONObject wishlistJson = (JSONObject) (json.get("wishlist"));
            this.wishlist = new Wishlist(wishlistJson, false);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Item(JSONObject json) {
        this(json, true);
    }

    public Item(Item other) {
        this.name = other.name;
        this.description = other.description;
        this.amount = other.amount;
        this.image = other.image;
        this.link = other.link;
        this.position = other.position;
        this.wishlist = other.wishlist;
        this.wishlistId = other.wishlistId;
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
            .put("name", "Item")
            .put("plural", "Items")
            .put("path", "Item")
            .put("idName", "id");
    }
}
