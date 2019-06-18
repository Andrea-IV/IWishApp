package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.Donation;
import org.json.JSONObject;

public class DonationApi extends BaseApi<Donation> {

    public DonationApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/donations/";
    }

    @Override
    protected Donation createFromJson(JSONObject jsonObject) {
        return new Donation(jsonObject);
    }
}
