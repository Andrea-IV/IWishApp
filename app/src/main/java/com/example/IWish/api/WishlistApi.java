package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.Wishlist;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class WishlistApi extends BaseApi<Wishlist> {

    public WishlistApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/wishlists/";
    }

    @Override
    protected Wishlist createFromJson(JSONObject jsonObject) {
        return new Wishlist(jsonObject);
    }

    public Wishlist addParticipant(long id, long fk) throws InterruptedException, ExecutionException, JSONException {
        return super.addRelation(id, "participants", fk);
    }

    public Wishlist addItem(long id, long fk) throws InterruptedException, ExecutionException, JSONException {
        return super.addRelation(id, "items", fk);
    }
}
