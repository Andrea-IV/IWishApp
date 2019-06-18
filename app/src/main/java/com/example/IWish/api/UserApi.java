package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.User;
import org.json.JSONObject;

public class UserApi extends BaseApi<User> {

    public UserApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/users/";
    }

    @Override
    protected User createFromJson(JSONObject jsonObject) {
        return new User(jsonObject);
    }
}
