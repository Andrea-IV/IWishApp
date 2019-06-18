package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.Item;
import org.json.JSONObject;

public class ItemApi extends BaseApi<Item> {
    public ItemApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/items/";
    }

    @Override
    protected Item createFromJson(JSONObject jsonObject) {
        return new Item(jsonObject);
    }
}
