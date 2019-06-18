package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.Wishlist;
import org.json.JSONObject;

public class WishlistApi extends BaseApi<Wishlist> {

    public WishlistApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/wishlists/";
    }

    @Override
    protected Wishlist createFromJson(JSONObject jsonObject) {
        return new Wishlist(jsonObject);
    }
}
