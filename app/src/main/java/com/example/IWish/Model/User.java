package com.example.IWish.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class User extends Model {

    public String email;
    public String firstName;
    public String lastName;
    public List<Category> categories;
    public List<PrizePool> managedPrizePools;
    public List<Donation> donations;
    public List<Wishlist> wishlists;
    public List<Wishlist> concernedWishlists;

    public User() {
    }

    public User(User other) {
        this.email = other.email;
        this.firstName = other.firstName;
        this.lastName = other.lastName;

        this.categories = new ArrayList<>(other.categories.size());
        for ( Category category : other.categories )
            this.categories.add(new Category(category));

        this.managedPrizePools = new ArrayList<>(other.managedPrizePools.size());
        for ( PrizePool prizePool : other.managedPrizePools )
            this.managedPrizePools.add(new PrizePool(prizePool));

        this.donations = new ArrayList<>(other.donations.size());
        for ( Donation donation : other.donations )
            this.donations.add(new Donation(donation));

        this.wishlists = new ArrayList<>(other.wishlists.size());
        for ( Wishlist wishlist : other.wishlists )
            this.wishlists.add(new Wishlist(wishlist));

        this.concernedWishlists = new ArrayList<>(other.concernedWishlists.size());
        for ( Wishlist wishlist : other.concernedWishlists )
            this.concernedWishlists.add(new Wishlist(wishlist));
    }

    public User(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.email = (String) (json.get("email"));
            this.firstName = (String) (json.get("firstName"));
            this.lastName = (String) (json.get("lastName"));

            if ( includeRelations ) {
                JSONArray categoriesJson = (JSONArray) (json.get("categories"));
                int nbCategories = categoriesJson.length();
                this.categories = new ArrayList<>(nbCategories);
                for (int i = 0; i < nbCategories; i++) {
                    JSONObject categoryJson = (JSONObject) (categoriesJson.get(i));
                    Category category = new Category(categoryJson, false);
                    this.categories.add(category);
                }

                /*JSONArray managedPrizePoolsJson = (JSONArray) (json.get("managedPrizePools"));
                int nbManagedPrizePools = managedPrizePoolsJson.length();
                this.managedPrizePools = new ArrayList<>(nbManagedPrizePools);
                for (int i = 0; i < nbManagedPrizePools; i++) {
                    JSONObject managedPrizePoolJson = (JSONObject) (managedPrizePoolsJson.get(i));
                    PrizePool prizePool = new PrizePool(managedPrizePoolJson, false);
                    this.managedPrizePools.add(prizePool);
                }*/

                JSONArray wishlistsJson = (JSONArray) (json.get("wishlists"));
                int nbWishlists = wishlistsJson.length();
                this.wishlists = new ArrayList<>(nbWishlists);
                for (int i = 0; i < nbWishlists; i++) {
                    JSONObject wishlistJson = (JSONObject) (wishlistsJson.get(i));
                    Wishlist wishlist = new Wishlist(wishlistJson, false);
                    this.wishlists.add(wishlist);
                }

                JSONArray concernedWishlistsJson = (JSONArray) (json.get("concernedWishlists"));
                int nbConcernedWishlists = concernedWishlistsJson.length();
                this.concernedWishlists = new ArrayList<>(nbConcernedWishlists);
                for (int i = 0; i < nbConcernedWishlists; i++) {
                    JSONObject concernedWishlistJson = (JSONObject) (concernedWishlistsJson.get(i));
                    Wishlist concernedWishlist = new Wishlist(concernedWishlistJson, false);
                    this.concernedWishlists.add(concernedWishlist);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User(JSONObject json) {
        this(json, true);
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
            .put("name", "User")
            .put("plural", "Users")
            .put("path", "User")
            .put("idName", "id")
            .put("relations", new JSONObject()
                .put("managedPrizePools", new JSONObject()
                        .put("name", "managedPrizePools")
                        .put("type", "List<PrizePool>")
                        .put("model", "PrizePool")
                )
                .put("concernedWishlists", new JSONObject()
                        .put("name", "concernedWishlists")
                        .put("type", "List<Wishlist>")
                        .put("model", "Wishlist")
                )
            );
    }

    public String adaptToJson(List<?> objects){
        if ( objects == null ){
            return "null";
        }

        String result = "[";
        for (Object object : objects) {
            result += object.toString();
            result += ",";
        }
        if (result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }
        result += "]";

        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "\"email\":\"" + email + '\"' +
                ", \"firstName\":\"" + firstName + '\"' +
                ", \"lastName\":\"" + lastName + '\"' +
                ", \"categories\":" + adaptToJson(categories) +
                ", \"managedPrizePools\":" + adaptToJson(managedPrizePools) +
                ", \"donations\":" + adaptToJson(donations) +
                ", \"wishlists\":" + adaptToJson(wishlists) +
                ", \"concernedWishlists\":" + adaptToJson(concernedWishlists) +
                ", \"id\":" + id +
                ", \"createdAt\":" + createdAt +
                ", \"updatedAt\":" + updatedAt +
                '}';
    }
}
