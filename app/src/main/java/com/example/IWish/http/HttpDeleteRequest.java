package com.example.IWish.http;

import android.os.AsyncTask;

public class HttpDeleteRequest extends HttpRequest {
    public static final String REQUEST_METHOD = "DELETE";

    @Override
    public String getRequestMethod() {
        return REQUEST_METHOD;
    }

    @Override
    protected String doInBackground(String... strings) {
        String result;
        String url = strings[0];

        try {
            initConnection(url);
            connection.connect();

            result = read();

        } catch(Exception e) {
            e.printStackTrace();
            result = "error";
        }

        return result;
    }

    public AsyncTask<String, Void, String> exec(String url) {
        return execute(url);
    }
}