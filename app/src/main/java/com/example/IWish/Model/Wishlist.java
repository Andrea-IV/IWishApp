package com.example.IWish.Model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Wishlist extends Model {

    public String name;
    public Boolean isPublic;
    public List<Item> items;
    public PrizePool prizePool;
    public Long prizePoolId;
    public User user;
    public Long owner;
    public List<User> participants;

    public Wishlist() {
    }

    public Wishlist(JSONObject json, boolean includeRelations) {
        try {
            this.id = (int) (json.get("id"));
            this.createdAt = (long) (json.get("createdAt"));
            this.updatedAt = (long) (json.get("updatedAt"));
            this.name = (String) (json.get("name"));
            this.isPublic = (boolean) (json.get("isPublic"));

            if(json.has("prizePoolId")){
                this.prizePoolId = Long.decode((String)json.get("prizePoolId"));
            }
            if(json.has("owner")){
                try {
                    owner = Long.decode(json.get("owner").toString());
                } catch(NumberFormatException e) {
                    this.user = new User((JSONObject) json.get("owner"), false);
                    this.owner = user.id;
                }
            }

            if(json.has("ownerId")){
                this.owner =  Long.decode((String)json.get("ownerId"));
             }

            /*if(json.has("prizePool")){
                JSONObject prizePoolJson = (JSONObject) (json.get("prizePool"));
                this.prizePool = new PrizePool(prizePoolJson, false);
            }*/

            //if(json.has("owner")){
                //JSONObject ownerJson = (JSONObject) (json.get("owner"));
                //this.owner = new User(ownerJson, false);
            //}
            this.items = new ArrayList<>();
            this.participants = new ArrayList<>();

            if ( includeRelations ) {
                if(json.has("items")){
                    JSONArray itemsJson = (JSONArray) (json.get("items"));
                    int nbItems = itemsJson.length();
                    this.items = new ArrayList<>(nbItems);
                    for (int i = 0; i < nbItems; i++) {
                        JSONObject itemJson = new JSONObject(itemsJson.get(i).toString());
                        Item item = new Item(itemJson, false);
                        this.items.add(item);
                    }
                }

                if(json.has("participants")){
                    JSONArray participantsJson = (JSONArray) (json.get("participants"));
                    int nbParticipants = participantsJson.length();
                    this.participants = new ArrayList<>(nbParticipants);
                    for (int i = 0; i < nbParticipants; i++) {
                        JSONObject participantJson = (JSONObject) (participantsJson.get(i));
                        User participant = new User(participantJson, false);
                        this.participants.add(participant);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Wishlist(JSONObject json) {
        this(json, true);
    }

    public Wishlist(Wishlist other) {
        this.name = other.name;
        this.isPublic = other.isPublic;
        this.prizePool = other.prizePool;
        this.prizePoolId = other.prizePoolId;
        this.owner = other.owner;
        this.user = other.user;

        this.participants = new ArrayList<>(other.participants.size());
        for ( User participant : other.participants )
            this.participants.add(new User(participant));

        this.items = new ArrayList<>(other.items.size());
        for ( Item item : other.items )
            this.items.add(new Item(item));
    }

    @Override
    public JSONObject getModelDefinition() throws JSONException {
        return new JSONObject()
                .put("name", "Wishlist")
                .put("plural", "Wishlists")
                .put("path", "Wishlist")
                .put("idName", "id")
                .put("relations", new JSONObject()
                        .put("participants", new JSONObject()
                                .put("name", "participants")
                                .put("type", "List<User>")
                                .put("model", "User")
                        )
                        .put("items", new JSONObject()
                                .put("name", "items")
                                .put("type", "List<Item>")
                                .put("model", "Item")
                        )
                );
    }

    public String adaptToJson(List<?> objects){
        if ( objects == null ){
            return "null";
        }

        String result = "[";

        for(Object object : objects){
            result += object.toString();
            result += ",";
        }
        if(result.length() > 1){
            result = result.substring(0, result.length() - 1);
        }
        result += "]";

        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"name\":\"" + name + '\"' +
                ", \"isPublic\":" + isPublic +
                ", \"owner\":" + owner +
                ", \"items\": ..." + /*adaptToJson(items) +*/
                ", \"createdAt\":" + createdAt +
                ", \"updatedAt\":" + updatedAt +
                ", \"participants\": " + participants +
                '}';
    }
}
