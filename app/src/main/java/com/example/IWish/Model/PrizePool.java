package com.example.IWish.Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrizePool extends Model {

    public long endDate;
    public boolean closed;
    public Wishlist wishlist;
    public long wishlistId;
    public User manager;
    public long managerId;
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

            this.wishlistId = (int) (json.get("wishlistId"));
            this.managerId = (int) (json.get("managerId"));

            JSONObject wishlistJson = (JSONObject) (json.get("wishlist"));
            this.wishlist = new Wishlist(wishlistJson, false);

            JSONObject managerJson = (JSONObject) (json.get("manager"));
            this.manager = new User(managerJson, false);

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
        this.wishlistId = other.wishlistId;
        this.manager = other.manager;
        this.managerId = other.managerId;

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
