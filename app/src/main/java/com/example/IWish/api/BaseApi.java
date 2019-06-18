package com.example.IWish.api;

import com.example.IWish.Model.Model;
import com.example.IWish.http.HttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class BaseApi<T extends Model> {

    protected String actionUrl;
    protected HttpClient http;

    public BaseApi() {
        this.http = new HttpClient();
    }

    protected abstract T createFromJson(JSONObject jsonObject);

    public List<T> findAll() throws ExecutionException, InterruptedException, JSONException {
        String result = this.http.get(this.actionUrl).get();
        JSONArray jsonArray = new JSONArray(result);
        int nbFound = jsonArray.length();

        List<T> all = new ArrayList<>(nbFound);
        for ( int i = 0 ; i < nbFound ; i++ ) {
            JSONObject jsonObject = (JSONObject)jsonArray.get(i);
            T inst = createFromJson(jsonObject);
            all.add(inst);
        }

        return all;
    }

    public T findById(long id) throws ExecutionException, InterruptedException, JSONException {
        String result = this.http.get(this.actionUrl + id).get();
        JSONObject jsonObject = new JSONObject(result);
        T inst = createFromJson(jsonObject);
        return inst;
    }

    public T create(T instance) throws UnsupportedEncodingException, ExecutionException, InterruptedException, JSONException {
        String result = this.http.post(this.actionUrl, instance.toMap()).get();
        JSONObject jsonObject = new JSONObject(result);
        T responseInst = createFromJson(jsonObject);
        return responseInst;
    }
}
