package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Donation extends Model {

    public Double amount;
    public PrizePool concernedPrizePool;
    public Long prizePool;
    public User concernedDonor;
    public Long donor;

    public Donation() {
    }

    public Donation(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.amount = Double.parseDouble(json.get("amount").toString());

            if ( json.has("prizePool")) {
                if ( json.get("prizePool") instanceof Number ) {
                    this.prizePool = Long.parseLong(json.get("prizePool").toString());
                }
                else {
                    JSONObject prizePoolJson = (JSONObject) (json.get("prizePool"));
                    this.concernedPrizePool = new PrizePool(prizePoolJson, false);
                }

            }
            if ( json.has("donor")) {
                if ( json.get("donor") instanceof Number ) {
                    this.donor = Long.parseLong(json.get("donor").toString());
                }
                else {
                    JSONObject donorJson = (JSONObject) (json.get("donor"));
                    this.concernedDonor = new User(donorJson, false);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Donation(JSONObject json) {
        this(json, true);
    }

    public Donation(Donation other) {
        this.amount = other.amount;
        this.concernedPrizePool = other.concernedPrizePool;
        this.prizePool = other.prizePool;
        this.concernedDonor = other.concernedDonor;
        this.donor = other.donor;
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
            .put("name", "Donation")
            .put("plural", "Donations")
            .put("path", "Donation")
            .put("idName", "id");
    }
}
