package com.example.IWish;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.UserApi;
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
    List<User> userList;

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

    @SuppressLint("ClickableViewAccessibility")
    public void loadWishList(){
        final ListView listview = findViewById(R.id.listOfWishlist);
        displayWishList();
        DashboardListAdapter adapter = new DashboardListAdapter(this,R.layout.list_of_wishlist, rowWishLists);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WishlistApi wishlistApi = new WishlistApi();
                Wishlist wishlist = rowWishLists.get(position).getWishlist();

                Intent intent = new Intent(view.getContext(), DetailsActivity.class);
                Bundle b = new Bundle();
                b.putString("TITLE", wishlist.name);
                b.putString("WISHLIST", wishlist.toString());
                b.putString("USER", user.toString());
                b.putString("OWNED", Boolean.toString(rowWishLists.get(position).isOwned()));
                intent.putExtras(b);

                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        listview.setOnTouchListener(new OnSwipeTouchListener(DashboardActivity.this) {
            public void onSwipeLeft(float xStart, float yStart) {
                if(listview.getChildCount() > 0){
                    float childHeight = convertDpToPixel(70);

                    final int position = (int)yStart / (int)childHeight;
                    final View swipedView = listview.getChildAt(position);
                    ImageView image = swipedView.findViewById(R.id.expand);

                    if (swipedView.findViewById(R.id.owned).getVisibility() == View.INVISIBLE && String.valueOf(image.getTag()).equals("expand")) {
                        swipedView.findViewById(R.id.deleteWishlist).setVisibility(View.VISIBLE);

                        Animation animation = new TranslateAnimation(0, -100,0, 0);
                        animation.setFillAfter(true);
                        animation.setDuration(100);

                        swipedView.findViewById(R.id.wishButtonLayout).startAnimation(animation);
                        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
                        fadeIn.setDuration(500);
                        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float alpha = (float) animation.getAnimatedValue();
                                swipedView.findViewById(R.id.deleteWishlist).setAlpha(alpha);
                            }
                        });
                        fadeIn.start();
                        image.setImageResource(R.drawable.reduce);
                        image.setTag("reduce");

                        swipedView.findViewById(R.id.deleteWishlist).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDeleteWishlist(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position);
                            }
                        });
                    } else if(String.valueOf(image.getTag()).equals("expand")){
                        swipedView.findViewById(R.id.deleteWishlist).setVisibility(View.VISIBLE);
                        swipedView.findViewById(R.id.modifyWishlist).setVisibility(View.VISIBLE);
                        swipedView.findViewById(R.id.inviteUser).setVisibility(View.VISIBLE);
                        swipedView.findViewById(R.id.facebookShare).setVisibility(View.VISIBLE);

                        Animation animation = new TranslateAnimation(0, -500,0, 0);
                        animation.setFillAfter(true);
                        animation.setDuration(300);

                        swipedView.findViewById(R.id.wishButtonLayout).startAnimation(animation);
                        ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
                        fadeIn.setDuration(1000);
                        fadeIn.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float alpha = (float) animation.getAnimatedValue();
                                swipedView.findViewById(R.id.deleteWishlist).setAlpha(alpha);
                                swipedView.findViewById(R.id.modifyWishlist).setAlpha(alpha);
                                swipedView.findViewById(R.id.inviteUser).setAlpha(alpha);
                                swipedView.findViewById(R.id.facebookShare).setAlpha(alpha);
                            }
                        });
                        fadeIn.start();
                        image.setImageResource(R.drawable.reduce);
                        image.setTag("reduce");

                        swipedView.findViewById(R.id.inviteUser).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UserApi userApi = new UserApi();
                                try {
                                    userList = userApi.findAll();
                                    showShareList(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position);
                                /*for(int i = 0; i < userList.size(); i++){
                                    if()
                                }*/
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        swipedView.findViewById(R.id.modifyWishlist).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showModifyWishlist(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString());
                            }
                        });

                        swipedView.findViewById(R.id.deleteWishlist).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showDeleteWishlist(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position);
                            }
                        });
                    }
                }
            }
            public void onSwipeRight(float xStart, float yStart) {
                if(listview.getChildCount() > 0){
                    float childHeight = convertDpToPixel(70);

                    int position = (int)yStart / (int)childHeight;
                    final View swipedView = listview.getChildAt(position);
                    ImageView image = swipedView.findViewById(R.id.expand);

                    if(swipedView.findViewById(R.id.owned).getVisibility() == View.INVISIBLE && String.valueOf(image.getTag()).equals("reduce")) {
                        Animation animation = new TranslateAnimation(-100, 0, 0, 0);
                        animation.setFillAfter(true);
                        animation.setDuration(100);

                        swipedView.findViewById(R.id.wishButtonLayout).startAnimation(animation);
                        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
                        fadeOut.setDuration(50);
                        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float alpha = (float) animation.getAnimatedValue();
                                swipedView.findViewById(R.id.deleteWishlist).setAlpha(alpha);
                            }
                        });
                        fadeOut.start();
                        image.setImageResource(R.drawable.expand);
                        image.setTag("expand");

                        swipedView.findViewById(R.id.deleteWishlist).setVisibility(View.GONE);
                    }else if(String.valueOf(image.getTag()).equals("reduce")) {
                        Animation animation = new TranslateAnimation(-500, 0, 0, 0);
                        animation.setFillAfter(true);
                        animation.setDuration(300);

                        swipedView.findViewById(R.id.wishButtonLayout).startAnimation(animation);
                        ValueAnimator fadeOut = ValueAnimator.ofFloat(1f, 0f);
                        fadeOut.setDuration(200);
                        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float alpha = (float) animation.getAnimatedValue();
                                swipedView.findViewById(R.id.deleteWishlist).setAlpha(alpha);
                                swipedView.findViewById(R.id.modifyWishlist).setAlpha(alpha);
                                swipedView.findViewById(R.id.inviteUser).setAlpha(alpha);
                                swipedView.findViewById(R.id.facebookShare).setAlpha(alpha);
                            }
                        });
                        fadeOut.start();
                        image.setImageResource(R.drawable.expand);
                        image.setTag("expand");

                        swipedView.findViewById(R.id.deleteWishlist).setVisibility(View.GONE);
                        swipedView.findViewById(R.id.modifyWishlist).setVisibility(View.GONE);
                        swipedView.findViewById(R.id.inviteUser).setVisibility(View.GONE);
                        swipedView.findViewById(R.id.facebookShare).setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void showShareList(View view, final int idWishlist, String name, final int position){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.share_list_user, null);
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

        ((TextView)popupView.findViewById(R.id.textView)).setText(getString(R.string.share_user_title) + " \"" + name + "\"");

        ListView userListView = popupView.findViewById(R.id.listOfUsers);
        UserListAdapter adapter = new UserListAdapter(this, R.layout.list_of_user, userList);
        userListView.setAdapter(adapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                final User user = userList.get(position);
                userList.remove(position);

                new Thread(new Runnable() {
                    public void run() {
                        UserApi userApi = new UserApi();
                        WishlistApi wishlitApi = new WishlistApi();
                        try {
                            user.email = "test@mailmail.fr";
                            //user.concernedWishlists.add(wishlitApi.findById(idWishlist));
                            Log.i("USER", user.toString());
                            userApi.updateAttributes(user.id, user);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                ListView userListView = popupView.findViewById(R.id.listOfUsers);
                UserListAdapter adapter = new UserListAdapter(context, R.layout.list_of_user, userList);
                userListView.setAdapter(adapter);
            }
        });
    }

    public void showDeleteWishlist(View view, final int id, String name, final int position){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.delete_wishlist, null);
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

        ((TextView)popupView.findViewById(R.id.textView)).setText(getString(R.string.delete_message) + " " + name + " ?");
        Button deleteButton = popupView.findViewById(R.id.deleteWishlist);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                tryDeleteWishlist(id, position);
                popupWindow.dismiss();
            }
        });

        Button cancelButton = popupView.findViewById(R.id.cancelWishlist);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                popupWindow.dismiss();
            }
        });
    }

    public void showModifyWishlist(View view, final int id, String name){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_wishlist, null);
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

        ((TextView)popupView.findViewById(R.id.textView)).setText("Modify " + name);
        ((EditText)popupView.findViewById(R.id.newName)).setText(name);
        Button createButton = popupView.findViewById(R.id.createWishlist);
        createButton.setText(R.string.modify_item_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                String wishlistName = ((EditText)popupView.findViewById(R.id.newName)).getText().toString();
                if(wishlistName.isEmpty()){
                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.name_empty);
                }else{
                    tryModifyWishlist(id, wishlistName, popupWindow);
                }
            }
        });
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public void showCreateWishlist(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.create_wishlist, null);
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

        ((TextView)popupView.findViewById(R.id.textView)).setText(R.string.create_wishlist_name);
        ((EditText)popupView.findViewById(R.id.newName)).setText("");
        Button createButton = popupView.findViewById(R.id.createWishlist);
        createButton.setText(R.string.create_item_button);
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
    }

    public void tryModifyWishlist(int id, String wishlistName, PopupWindow popupWindow){
        WishlistApi wishlistApi = new WishlistApi();
        for(Wishlist wishlist: user.wishlists){
            if(wishlist.id == id){
                wishlist.name = wishlistName;
                try {
                    wishlistApi.updateAttributes(wishlist.id, wishlist);
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
                break;
            }
        }
    }

    public void tryDeleteWishlist(int id, int position){
        WishlistApi wishlistApi = new WishlistApi();
        try {
            wishlistApi.delete(id);
            user.wishlists.remove(position + 1);
            loadWishList();
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

    public void displayWishList(){
        rowWishLists = new ArrayList<>();
        int i = 0;
        for(Wishlist wishlist: user.wishlists){
            rowWishLists.add(new RowWishList(wishlist, true));
            i++;
        }
        for(Wishlist wishlist: user.concernedWishlists){
            rowWishLists.add(new RowWishList(wishlist, false));
            i++;
        }
    }

}
