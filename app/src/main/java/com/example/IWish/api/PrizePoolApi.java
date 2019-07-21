package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.PrizePool;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PrizePoolApi extends BaseApi<PrizePool> {

    public PrizePoolApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/prizepools/";
    }

    @Override
    protected PrizePool createFromJson(JSONObject jsonObject) {
        return new PrizePool(jsonObject);
    }

    public JSONObject collectDonations(long id) throws UnsupportedEncodingException, JSONException, ExecutionException, InterruptedException {
        Map<String, String> data = new HashMap<>();
        data.put("wishlistId", String.valueOf(id));

        String result = this.http.post(ApiConfig.RECEIVE_DONATIONS_URL, data).get();

        return new JSONObject(result);
    }
}
