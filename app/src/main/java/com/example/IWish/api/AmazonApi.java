package com.example.IWish.api;

import com.example.IWish.Model.Item;
import com.example.IWish.http.HttpGetRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.ExecutionException;

public class AmazonApi {

    private HttpGetRequest getRequest = new HttpGetRequest();

    public Item getItemFromUrl(String url) throws ExecutionException, InterruptedException {
        String html = getRequest.exec(url).get();
        Document document = Jsoup.parse(html);

        Element titleElement = document.getElementById("title");
        if(titleElement == null){
            return null;
        }

        Element priceElement = document.getElementById("priceblock_ourprice");
        if(priceElement == null){
            return null;
        }

        String imageUrl = "";
        Element image = document.getElementById("landingImage");
        if(image != null){
            imageUrl = image.absUrl("src");
        }

        Item item = new Item();
        item.name = titleElement.text();
        item.amount = Double.parseDouble((getPriceFromString(priceElement.text())).replace(',','.'));
        item.image = imageUrl;
        return item;
    }

    private String getPriceFromString(String string){
        int start = 0;
        while (start < string.length() && !Character.isDigit(string.charAt(start))) start++;

        int end = string.length() - 1;
        while (end > start && Character.isDigit(string.charAt(end))) end--;
        return string.substring(start, end);
    }
}
