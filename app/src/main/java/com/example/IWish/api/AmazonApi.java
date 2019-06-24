package com.example.IWish.api;

import com.example.IWish.http.HttpGetRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.ExecutionException;

public class AmazonApi {

    private HttpGetRequest getRequest = new HttpGetRequest();

    public String getItemTitleFromUrl(String url) throws ExecutionException, InterruptedException {
        String html = getRequest.exec(url).get();
        Document document = Jsoup.parse(html);
        Element titleElement = document.getElementById("title");
        return titleElement.text();
    }
}
