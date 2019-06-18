package com.example.IWish.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class HttpRequest extends AsyncTask<String, Void, String> {
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;

    protected URL url;
    protected HttpURLConnection connection;

    public abstract String getRequestMethod();

    protected void initConnection(String urlStr) throws Exception {
        url = new URL(urlStr);
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(getRequestMethod());
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
    }

    private String getReadingError() throws IOException {
        String inputLine;

        InputStream errorStream = connection.getErrorStream();

        if ( errorStream == null ) {
            return null;
        }

        InputStreamReader streamReader = new InputStreamReader(errorStream);
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder stringBuilder = new StringBuilder();
        while ( (inputLine = reader.readLine()) != null ) {
            stringBuilder.append(inputLine);
        }
        reader.close();
        streamReader.close();

        return stringBuilder.toString();
    }

    protected String read() throws IOException {
        String inputLine;

        String error = getReadingError();
        if ( error != null ) {
            return error;
        }

        InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(streamReader);
        StringBuilder stringBuilder = new StringBuilder();
        while ( (inputLine = reader.readLine()) != null ) {
            stringBuilder.append(inputLine);
        }
        reader.close();
        streamReader.close();

        return stringBuilder.toString();
    }
}
