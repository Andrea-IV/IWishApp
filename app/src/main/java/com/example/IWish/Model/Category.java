package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category extends Model {

    public String name;
    public List<User> fans;
    public List<Item> items;

    public Category() {
    }

    public Category(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.name = (String) (json.get("name"));

            if ( includeRelations ) {
                JSONArray fansJson = (JSONArray) (json.get("fans"));
                int nbFans = fansJson.length();
                this.fans = new ArrayList<>(nbFans);
                for ( int i = 0 ; i < nbFans ; i++ ) {
                    JSONObject fanJson = (JSONObject) (fansJson.get(i));
                    User fan = new User(fanJson, false);
                    this.fans.add(fan);
                }

                JSONArray itemsJson = (JSONArray) (json.get("items"));
                int nbItems = itemsJson.length();
                this.items = new ArrayList<>(nbItems);
                for ( int i = 0 ; i < nbItems ; i++ ) {
                    JSONObject itemJson = (JSONObject) (itemsJson.get(i));
                    Item item = new Item(itemJson, false);
                    this.items.add(item);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Category(JSONObject json) {
        this(json, true);
    }

    public Category(Category other) {

        this.name = other.name;
        this.fans = new ArrayList<>(other.fans.size());
        for ( User fan : other.fans )
            this.fans.add(new User(fan));

        this.items = new ArrayList<>(other.items.size());
        for ( Item item : other.items )
            this.items.add(new Item(item));
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
            .put("name", "Category")
            .put("plural", "Categories")
            .put("path", "Category")
            .put("idName", "id");
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", fans=" + fans +
                ", items=" + items +
                ", id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
