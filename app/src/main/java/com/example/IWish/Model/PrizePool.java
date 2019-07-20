package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrizePool extends Model {

    public Long endDate;
    public Boolean closed;
    public Wishlist concernedWishlist;
    public Long wishlist;
    public User concernedManager;
    public Long manager;
    public List<Donation> donations;

    public PrizePool() {
    }

    public PrizePool(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.endDate = (long) (json.get("endDate"));
            this.closed = (boolean) (json.get("closed"));

            if(json.get("wishlist") instanceof Integer){
                this.wishlist = Long.valueOf((Integer)json.get("wishlist"));
            }else{
                JSONObject wishlistJson = (JSONObject) (json.get("wishlist"));
                this.concernedWishlist = new Wishlist(wishlistJson, false);
            }

            if(json.get("manager") instanceof Integer){
                this.manager = Long.valueOf((Integer)json.get("manager"));
            }else{
                JSONObject managerJson = (JSONObject) json.get("manager");
                this.concernedManager = new User(managerJson, false);
            }

            if(json.has("wishlistId")){
                this.wishlist = Long.decode((String)json.get("wishlistId"));
            }else if(!(json.get("wishlist") instanceof Integer)){
                this.wishlist = this.concernedWishlist.id;
            }
            if(json.has("managerId")){
                this.manager = Long.decode((String)json.get("managerId"));
            }else if(!(json.get("manager") instanceof Integer)){
                this.manager = this.concernedManager.id;
            }

            if ( includeRelations ) {
                JSONArray donationsJson = (JSONArray)(json.get("donations"));
                int nbDonations = donationsJson.length();
                this.donations = new ArrayList<>(nbDonations);
                for ( int i =0 ; i < nbDonations ; i++ ) {
                    JSONObject donationJson = (JSONObject)(donationsJson.get(i));
                    Donation donation = new Donation(donationJson, false);
                    this.donations.add(donation);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public PrizePool(JSONObject json) {
        this(json, true);
    }

    public PrizePool(PrizePool other) {
        this.endDate = other.endDate;
        this.closed = other.closed;
        this.wishlist = other.wishlist;
        this.concernedWishlist = other.concernedWishlist;
        this.manager = other.manager;
        this.manager = other.manager;

        this.donations = new ArrayList<>(other.donations.size());
        for ( Donation donation : other.donations )
            this.donations.add(new Donation(donation));
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
            .put("name", "PrizePool")
            .put("plural", "PrizePools")
            .put("path", "PrizePool")
            .put("idName", "id");
    }
}
