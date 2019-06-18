package com.example.IWish.api;

import com.example.IWish.ApiConfig;
import com.example.IWish.Model.Category;
import org.json.JSONObject;

public class CategoryApi extends BaseApi<Category> {

    public CategoryApi() {
        this.actionUrl = ApiConfig.BASE_URL + "/categories/";
    }

    @Override
    protected Category createFromJson(JSONObject jsonObject) {
        return new Category(jsonObject);
    }
}
