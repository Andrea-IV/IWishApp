package com.example.IWish.api;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginResponse {
    public int status;
    public String message;
    public UserLoginResponse user;

    public LoginResponse(JSONObject json) {
        try {
            this.status = (int) (json.get("status"));
            this.message = (String) (json.get("message"));
            Object userResponse = json.get("user");
            if ( userResponse instanceof Boolean && (boolean)userResponse == false ) {
                this.user = null;
            }
            else {
                this.user = new UserLoginResponse((JSONObject) userResponse);
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

    private class UserLoginResponse {
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
