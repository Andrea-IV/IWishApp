package com.example.IWish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.WishlistApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DashboardActivity extends AppCompatActivity {

    Context context;
    List<RowWishList> rowWishLists;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        context = this;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            try {
                this.user = new User(new JSONObject(bundle.getString("USER")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        loadWishList();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void goToDetails(View view){
        LinearLayout lay = (LinearLayout)view;
        String id = ((TextView)lay.getChildAt(2)).getText().toString();
        String title = ((TextView)lay.getChildAt(1)).getText().toString();

        Intent intent = new Intent(view.getContext(), DetailsActivity.class);
        Bundle b = new Bundle();
        b.putInt("ID", Integer.parseInt(id));
        b.putString("TITLE", title);
        intent.putExtras(b);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void loadWishList(){
        ListView listview = findViewById(R.id.listOfWishlist);
        displayWishList();
        DashboardListAdapter adapter = new DashboardListAdapter(this,R.layout.list_of_wishlist, rowWishLists);
        listview.setAdapter(adapter);
    }

    public void showCreateWishlist(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.activity_create_wishlist, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DashboardActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });

        Button createButton = (Button)popupView.findViewById(R.id.createWishlist);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                String wishlistName = ((EditText)popupView.findViewById(R.id.newName)).getText().toString();
                if(wishlistName.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.name_empty);
                }else{
                    tryCreateWishlist(wishlistName, popupWindow);
                }
            }
        });
    }

    public void tryCreateWishlist(String wishlistName, PopupWindow popupWindow){
        WishlistApi wishlistApi = new WishlistApi();
        Wishlist wishlist = new Wishlist();
        wishlist.name = wishlistName;
        wishlist.owner = user.id;
        try {
          user.wishlists.add(wishlistApi.create(wishlist));
          loadWishList();
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
        /*try {
            WishlistResponse res = userApi.createWishlist(wishlistName);
            Intent intent = new Intent(this, DetailsActivity.class);
            if(res.user != null){
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                ((TextView)findViewById(R.id.errorText)).setText(R.string.default_error_creation);
                View loadingPanel = findViewById(R.id.loadingPanel);
                loadingPanel.setVisibility(View.INVISIBLE);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public void displayWishList(){
        rowWishLists = new ArrayList<>();

        for(Wishlist wishlist: user.wishlists){
            rowWishLists.add(new RowWishList(wishlist));
        }
    }

}
