package com.example.IWish.api;

import android.util.Log;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class UserApi extends BaseApi<User> {

    public UserApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/users/";
    }

    @Override
    protected User createFromJson(JSONObject jsonObject) {
        return new User(jsonObject);
    }

    public UserResponse createUser(String email, String password, String firstName, String lastName) throws UnsupportedEncodingException, ExecutionException, InterruptedException, JSONException {
        Map<String, String> getData = new HashMap<>();
        getData.put("email", email);
        getData.put("password", password);
        getData.put("firstName", firstName);
        getData.put("lastName", lastName);

        String result = this.http.get(actionUrl + "create?" + "email=" + email + "&password=" + password +"&firstName=" + firstName +"&lastName=" + lastName).get();

        JSONObject jsonObject = new JSONObject(result);
        return new UserResponse(jsonObject);
    }
}
