package com.example.IWish.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Item extends Model {

    public String name;
    public String description;
    public double amount;
    public String image;
    public String link;
    public int position;
    public long wishlist;
    public Wishlist fromWishlist;

    public Item() {
    }

    public Item(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.name = (String) (json.get("name"));
            this.description = (String) (json.get("description"));
            this.amount = Double.parseDouble(json.get("amount").toString());
            this.image = (String) (json.get("image"));
            this.link = (String) (json.get("link"));
            this.position = (int) (json.get("position"));

            if(json.has("wishlist")){
                try {
                    this.wishlist = Integer.parseInt(json.get("wishlist").toString());
                } catch(NumberFormatException e) {
                    this.fromWishlist = new Wishlist((JSONObject) json.get("wishlist"), true);
                    this.wishlist = fromWishlist.id;
                }
            }
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
        this.fromWishlist = other.fromWishlist;
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
