package com.example.IWish.api;

import android.util.Log;

import com.example.IWish.Model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginResponse {
    public int status;
    public String message;
    public User user;

    public LoginResponse(JSONObject json) {
        try {
            this.status = (int) (json.get("status"));
            this.message = (String) (json.get("message"));
            Object userResponse = json.get("user");
            if ( userResponse instanceof Boolean && (boolean)userResponse == false ) {
                this.user = null;
            }
            else {
                UserApi userApi = new UserApi();
                try{
                    JSONArray userFound = userApi.findsUser(String.valueOf((int)((JSONObject) userResponse).get("id")), "", "", "");
                    this.user = new User(userFound.getJSONObject(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", user=" + user +
                '}';
    }

    public class UserLoginResponse {
        private String email;
        private long id;
        private String token;

        private UserLoginResponse(JSONObject json) {
            try {
                this.email = (String) (json.get("email"));
                this.id = (int) (json.get("id"));
                this.token = (String) (json.get("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        @Override
        public String toString() {
            return "UserLoginResponse{" +
                    "email='" + email + '\'' +
                    ", id=" + id +
                    ", token='" + token + '\'' +
                    '}';
        }
    }
}
