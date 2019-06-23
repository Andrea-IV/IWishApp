package com.example.IWish;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.IWish.Model.Item;
import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.ItemApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DetailsActivity extends AppCompatActivity {

    private Wishlist wishlist;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle b = getIntent().getExtras();
        if(b != null){
            TextView title = findViewById(R.id.title);
            title.setText(b.getString("TITLE"));
            try {
                wishlist = new Wishlist(new JSONObject(b.getString("WISHLIST")));
                user = new User(new JSONObject(b.getString("USER")));
                wishlist.user = user;
                callItem();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            loadItem();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void loadItem(){
        ListView listview = findViewById(R.id.listOfItem);
        DetailsListAdapter adapter = new DetailsListAdapter(this,R.layout.list_of_item, wishlist.items);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Item item = wishlist.items.get(position);
            goToDetails(view, item);
            }
        });
    }

    public void goToDetails(View view, final Item item){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_product, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        ((TextView)popupView.findViewById(R.id.textTitle)).setText(item.name);
        ((TextView)popupView.findViewById(R.id.description)).setText(item.description);
        ((TextView)popupView.findViewById(R.id.price)).setText("Price : " + String.format("%.2f", item.amount));

        if(item.link.isEmpty()){
            popupView.findViewById(R.id.link).setVisibility(View.GONE);
        }else{
            popupView.findViewById(R.id.link).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(item.link));
                    startActivity(viewIntent);
                }
            });
        }

        popupView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(item);
                callItem();
                loadItem();
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.modify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                showModifyItem(v, item.name, item.description, item.amount, item.link);
            }
        });

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });
    }

    public void showModifyItem(View view, String baseName, String baseDescription, double baseAmount, String baselink){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_item, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });

        ((TextView)popupView.findViewById(R.id.textView)).setText(R.string.modify_item_title);
        ((EditText)popupView.findViewById(R.id.newName)).setText(baseName);
        ((EditText)popupView.findViewById(R.id.description)).setText(baseDescription);
        ((EditText)popupView.findViewById(R.id.amount)).setText(String.format("%.2f", baseAmount));
        ((EditText)popupView.findViewById(R.id.link)).setText(baselink);

        Button modifyButton = popupView.findViewById(R.id.createItem);
        modifyButton.setText(R.string.modify_item_button);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                String productName = ((EditText)popupView.findViewById(R.id.newName)).getText().toString();
                String description = ((EditText)popupView.findViewById(R.id.description)).getText().toString();
                String amount = ((EditText)popupView.findViewById(R.id.amount)).getText().toString();
                String link = ((EditText)popupView.findViewById(R.id.link)).getText().toString();
                if(productName.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_name_empty);
                }else if(amount.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_amount_empty);
                }else{
                    if(!link.isEmpty()){
                        if(URLUtil.isValidUrl(link)){
                            //tryCreateProduct(productName, description, amount, link, popupWindow);
                        }else{
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_link_problem);
                        }
                    }else{
                        //tryCreateProduct(productName, description, amount, link, popupWindow);
                    }
                }
            }
        });
    }

    public void deleteItem(Item item){
        ItemApi itemApi = new ItemApi();
        try {
            itemApi.delete(item.id);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void showCreateItem(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_item, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });

        ((TextView)popupView.findViewById(R.id.textView)).setText(R.string.create_item_name);
        Button createButton = popupView.findViewById(R.id.createItem);
        createButton.setText(R.string.create_item_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                String productName = ((EditText)popupView.findViewById(R.id.newName)).getText().toString();
                String description = ((EditText)popupView.findViewById(R.id.description)).getText().toString();
                String amount = ((EditText)popupView.findViewById(R.id.amount)).getText().toString();
                String link = ((EditText)popupView.findViewById(R.id.link)).getText().toString();
                if(productName.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_name_empty);
                }else if(amount.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_amount_empty);
                }else{
                    if(!link.isEmpty()){
                        if(URLUtil.isValidUrl(link)){
                            tryCreateProduct(productName, description, amount, link, popupWindow);
                        }else{
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_link_problem);
                        }
                    }else{
                        tryCreateProduct(productName, description, amount, link, popupWindow);
                    }
                }
            }
        });
    }

    private void tryCreateProduct(String productName, String description, String amount, String link, PopupWindow popupWindow){
        ItemApi itemApi = new ItemApi();
        Item item = new Item();
        item.name = productName;
        item.description = description;
        item.amount = Float.parseFloat(amount);
        item.link = link;
        item.position = 0;
        item.wishlist = this.wishlist.id;
        try {

            wishlist.items.add(itemApi.create(item));
            loadItem();
            popupWindow.dismiss();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callItem(){
        ItemApi itemApi = new ItemApi();
        try {
            wishlist.items = new ArrayList<>();
            List<Item> itemList = itemApi.findAll();
            for(Item item: itemList){
                if(item.wishlist == wishlist.id){
                    wishlist.items.add(item);
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
