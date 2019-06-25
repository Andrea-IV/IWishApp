package com.example.IWish.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Donation extends Model {

    public Float amount;
    public PrizePool prizePool;
    public Long prizePoolId;
    public User donor;
    public Long donorId;

    public Donation() {
    }

    public Donation(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.amount = (float) (json.get("amount"));

            this.prizePoolId = Long.parseLong((String)json.get("prizePoolId"));
            this.donorId = Long.parseLong((String)json.get("donorId"));

            JSONObject prizePoolJson = (JSONObject) (json.get("prizePool"));
            this.prizePool = new PrizePool(prizePoolJson, false);

            JSONObject donorJson = (JSONObject) (json.get("donor"));
            this.donor = new User(donorJson, false);
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
