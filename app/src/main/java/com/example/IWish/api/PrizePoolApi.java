package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.PrizePool;

import org.json.JSONObject;

public class PrizePoolApi extends BaseApi<PrizePool> {

    public PrizePoolApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/prizepools/";
    }

    @Override
    protected PrizePool createFromJson(JSONObject jsonObject) {
        return new PrizePool(jsonObject);
    }
}
