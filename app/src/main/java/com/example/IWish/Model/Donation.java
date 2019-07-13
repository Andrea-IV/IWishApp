package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Donation extends Model {

    public Double amount;
    public PrizePool prizePool;
    public Long prizePoolId;
    public User donor;
    public Long donorId;

    public Donation() {
    }

    public Donation(JSONObject json, boolean includeRelations) {
        Log.i("TAGTAG", "json="+json);
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.amount = (Double) (json.get("amount"));

            if ( json.has("prizePool")) {
                if ( json.get("prizePool") instanceof Number ) {
                    this.prizePoolId = Long.parseLong(json.get("prizePool").toString());
                }
                else {
                    JSONObject prizePoolJson = (JSONObject) (json.get("prizePool"));
                    this.prizePool = new PrizePool(prizePoolJson, false);
                }

            }
            if ( json.has("donor")) {
                if ( json.get("donor") instanceof Number ) {
                    this.donorId = Long.parseLong(json.get("donor").toString());
                }
                else {
                    JSONObject donorJson = (JSONObject) (json.get("donor"));
                    this.donor = new User(donorJson, false);
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
        this.prizePool = other.prizePool;
        this.prizePoolId = other.prizePoolId;
        this.donor = other.donor;
        this.donorId = other.donorId;
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
