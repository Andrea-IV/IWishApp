package com.example.IWish.http;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class HttpPostRequest extends HttpRequest {
    public static final String REQUEST_METHOD = "POST";
    public static final String UTF_8 = "UTF-8";

    @Override
    public String getRequestMethod() {
        return REQUEST_METHOD;
    }

    @Override
    protected void initConnection(String uri) throws Exception {
        super.initConnection(uri);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setChunkedStreamingMode(0);
    }

    private void write(String params) throws IOException {
        OutputStream out = new BufferedOutputStream(connection.getOutputStream());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, UTF_8));
        writer.write(params);
        writer.flush();
        writer.close();
        out.close();
    }

    @Override
    protected String doInBackground(String... strings) {
        String result;
        String url = strings[0];
        String params = strings[1];

        try {
            initConnection(url);

            write(params);

            result = read();

        } catch(Exception e) {
            e.printStackTrace();
            result = e.getMessage();
        }

        return result;
    }

    private String buildParams(Map<String, String> paramsMap) throws UnsupportedEncodingException {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        int nb = paramsMap.size();
        for ( Map.Entry<String, String> entry : paramsMap.entrySet() ) {
            stringBuilder.append(URLEncoder.encode(entry.getKey(), UTF_8));
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(entry.getValue(), UTF_8));
            if ( i++ < nb - 1 )
                stringBuilder.append("&");
        }
        return stringBuilder.toString();
    }

    public AsyncTask<String, Void, String> exec(String url, Map<String, String> paramsMap) throws UnsupportedEncodingException {
        String paramsStr = buildParams(paramsMap);
        return execute(url, paramsStr);
    }
}
