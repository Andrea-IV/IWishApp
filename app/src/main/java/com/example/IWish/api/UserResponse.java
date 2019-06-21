package com.example.IWish.api;

import com.example.IWish.Model.User;

import org.json.JSONException;
import org.json.JSONObject;

public class UserResponse {
    public User user;

    public UserResponse(JSONObject json) {
        try {
            Object userResponse = json.get("id");
            this.user = new User(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "UserResponse{user=" + user +'}';
    }
}
