package com.example.IWish;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.IWish.Model.Category;
import com.example.IWish.Model.Donation;
import com.example.IWish.Model.Item;
import com.example.IWish.Model.PrizePool;
import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.AmazonApi;
import com.example.IWish.api.CategoryApi;
import com.example.IWish.api.DonationApi;
import com.example.IWish.api.ItemApi;
import com.example.IWish.api.PrizePoolApi;
import com.example.IWish.api.UserApi;
import com.example.IWish.http.HttpClient;
import com.facebook.CallbackManager;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.IWish.ApiConfig.IMAGES_URL;

public class DetailsActivity extends AppCompatActivity {

    static final int PAYPAL_REQUEST_CODE = 7171;
    static final int IMAGE_REQUEST_CODE = 3;
    static final int STORAGE_PERMISSION_CODE = 123;
    final static String DATE_FORMAT = "dd/MM/yyyy";
    final String regExp = "[0-9]+([,.][0-9]{1,2})?";

    static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiConfig.PAYPAL_CLIENT_ID);

    Wishlist wishlist;
    User user;
    Boolean owned;
    int positionSelected = -1;
    String amount;
    CallbackManager callbackManager;
    Uri filePath;
    ImageView imageView;
    Bitmap bitmap;
    ProgressDialog mDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ( requestCode == PAYPAL_REQUEST_CODE ) {
            if ( resultCode == RESULT_OK ) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if ( confirmation != null ) {
                    try {
                        final String paymentDetails = confirmation.toJSONObject().toString(4);

                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(paymentDetails);
                                    JSONObject response = jsonObject.getJSONObject("response");

                                    if(response.getString("state").equals("approved")){
                                        DonationApi donationApi = new DonationApi();
                                        Donation donation = new Donation();
                                        donation.amount = Double.parseDouble(amount);
                                        donation.donor = user.id;
                                        donation.prizePool = wishlist.prizePool.id;
                                        donationApi.create(donation);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        startActivity(new Intent(this, PaymentDetails.class)
                                .putExtra("PaymentDetails", paymentDetails)
                                .putExtra("PaymentAmount", amount));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if ( resultCode == Activity.RESULT_CANCELED ){
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        }
        else if ( resultCode == PaymentActivity.RESULT_EXTRAS_INVALID ) {
            Toast.makeText(this, "Invalid", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Start Paypal service
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
        startService(intent);

        callbackManager = CallbackManager.Factory.create();

        Bundle b = getIntent().getExtras();
        if(b != null){
            TextView title = findViewById(R.id.title);
            title.setText(b.getString("TITLE"));
            owned = Boolean.valueOf(b.getString("OWNED"));

            if(!owned){
                findViewById(R.id.imageView2).setVisibility(View.INVISIBLE);
            }

            try {
                wishlist = new Wishlist(new JSONObject(b.getString("WISHLIST")));
                user = new User(new JSONObject(b.getString("USER")));
                wishlist.user = user;

                PrizePoolApi prizePoolApi = new PrizePoolApi();
                try {
                    List<PrizePool> prizePools = prizePoolApi.findAll();
                    for(PrizePool prizePool: prizePools){
                        if(prizePool.wishlist == wishlist.id){
                            wishlist.prizePool = prizePool;
                            break;
                        }
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
        final ListView listview = findViewById(R.id.listOfItem);
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

    public void showComparator(final View view){
        view.findViewById(R.id.link).setVisibility(View.GONE);
        view.findViewById(R.id.imageView).setVisibility(View.GONE);
        view.findViewById(R.id.textTitle).setVisibility(View.GONE);
        view.findViewById(R.id.description).setVisibility(View.GONE);
        view.findViewById(R.id.price).setVisibility(View.GONE);
        view.findViewById(R.id.delete).setVisibility(View.GONE);
        view.findViewById(R.id.modify).setVisibility(View.GONE);

        view.findViewById(R.id.textView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.amazonLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.commerceLayout).setVisibility(View.VISIBLE);

        view.findViewById(R.id.comparatorImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideComparator(view);
            }
        });
    }

    public void hideComparator(final View view){
        view.findViewById(R.id.link).setVisibility(View.VISIBLE);
        view.findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        view.findViewById(R.id.textTitle).setVisibility(View.VISIBLE);
        view.findViewById(R.id.description).setVisibility(View.VISIBLE);
        view.findViewById(R.id.price).setVisibility(View.VISIBLE);
        view.findViewById(R.id.delete).setVisibility(View.VISIBLE);
        view.findViewById(R.id.modify).setVisibility(View.VISIBLE);

        view.findViewById(R.id.textView).setVisibility(View.GONE);
        view.findViewById(R.id.amazonLayout).setVisibility(View.GONE);
        view.findViewById(R.id.commerceLayout).setVisibility(View.GONE);

        view.findViewById(R.id.comparatorImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showComparator(view);
            }
        });
    }

    public void goToDetails(View view, final Item item){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.display_product, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        ((TextView)popupView.findViewById(R.id.textTitle)).setText(item.name);
        ((TextView)popupView.findViewById(R.id.description)).setText(item.description);
        ((TextView)popupView.findViewById(R.id.price)).setText("Price : " + String.format("%.2f", item.amount) + " €");

        new DownloadImageTask((ImageView) popupView.findViewById(R.id.imageView))
                .execute(IMAGES_URL + "item_" + item.id + ".jpg");

        if(((ImageView) popupView.findViewById(R.id.imageView)).getDrawable() == null){
            ((ImageView) popupView.findViewById(R.id.imageView)).setImageDrawable(getResources().getDrawable(R.drawable.default_image));
        }

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
        if(this.owned){
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
                    showModifyItem(v, item.id, item.name, item.description, item.amount, item.link);
                }
            });
        }else{
            popupView.findViewById(R.id.modify).setVisibility(View.INVISIBLE);
            popupView.findViewById(R.id.delete).setVisibility(View.INVISIBLE);
        }

        popupView.findViewById(R.id.comparatorImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpClient httpClient = new HttpClient();

                try {
                    String result = httpClient.get(ApiConfig.COMPARATOR_URL + "?string=" + ((TextView) popupView.findViewById(R.id.textTitle)).getText().toString()).get();
                    final JSONObject jsonObject;
                    jsonObject = new JSONObject(result);

                    ((TextView)popupView.findViewById(R.id.amazonTitle)).setText("Price : " + jsonObject.get("priceAmazon"));
                    popupView.findViewById(R.id.amazonLayout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent viewIntent = null;
                            try {
                                viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(jsonObject.get("linkAmazon").toString()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivity(viewIntent);
                        }
                    });

                    ((TextView)popupView.findViewById(R.id.commerceTitle)).setText("Price : " + jsonObject.get("priceCommerce"));
                    popupView.findViewById(R.id.commerceLayout).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent viewIntent = null;
                            try {
                                viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(jsonObject.get("linkCommerce").toString()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            startActivity(viewIntent);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showComparator(popupView);
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

    public void showModifyItem(View view, final long id, String baseName, String baseDescription, double baseAmount, String baselink){
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

        popupView.findViewById(R.id.recomendationImage).setVisibility(View.GONE);
        ((TextView)popupView.findViewById(R.id.textView)).setText(R.string.modify_item_title);
        ((EditText)popupView.findViewById(R.id.newName)).setText(baseName);
        ((EditText)popupView.findViewById(R.id.description)).setText(baseDescription);
        ((EditText)popupView.findViewById(R.id.amount)).setText(String.format("%.2f", baseAmount));
        ((EditText)popupView.findViewById(R.id.link)).setText(baselink);

        ((ImageView) popupView.findViewById(R.id.uploadedImage)).setImageDrawable(getResources().getDrawable(R.drawable.default_image));


        new DownloadImageTask((ImageView) popupView.findViewById(R.id.uploadedImage))
                .execute(IMAGES_URL + "item_" + id + ".jpg");

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
                            tryModifyProduct(id, productName, description, amount, link, popupWindow);
                        }else{
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.product_link_problem);
                        }
                    }else{
                        tryModifyProduct(id, productName, description, amount, link, popupWindow);
                    }
                }
            }
        });

        Button modifyAmazonButton = popupView.findViewById(R.id.createAmazonItem);
        modifyAmazonButton.setText(R.string.modify_amazon_item_button);
        modifyAmazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            String link = ((EditText) popupView.findViewById(R.id.link)).getText().toString();
            if (!link.isEmpty()) {
                if (URLUtil.isValidUrl(link)) {
                    AmazonApi amazonApi = new AmazonApi();
                    try {
                        Item item = amazonApi.getItemFromUrl(link);
                        if(item != null){
                            tryModifyProduct(id, item.name, "", item.amount.toString(), link, popupWindow);
                        }else{
                            ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.amazon_link_not_working);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.product_link_problem);
                }
            } else {
                ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.link_empty);
            }
            }
        });
        ImageView uploadImage = popupView.findViewById(R.id.uploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(popupView);
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

    public void showPrizePoolUser(View view){
        if(wishlist.prizePool == null && user.id == wishlist.owner){
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.show_prizepool, null);
            popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            ((TextView)popupView.findViewById(R.id.title)).setText(R.string.create_title);

            final Context context = this;
            final long id = this.wishlist.id;

            UserApi userApi = new UserApi();
            try {
                List<User> userList = new LinkedList<User>();
                for(User user: userApi.findAll()){
                    for(Wishlist list: user.concernedWishlists){
                        if(list.id == id){
                            userList.add(user);
                        }
                    }
                }
                final ListView userListView = popupView.findViewById(R.id.listOfUsers);
                UserListAdapter adapter = new UserListAdapter(context, R.layout.show_users, userList);
                userListView.setAdapter(adapter);
                if(positionSelected != -1){
                    positionSelected = -1;
                }

                userListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                        if(positionSelected == -1){
                            positionSelected = position;
                            userListView.getChildAt(positionSelected).setBackgroundColor(getResources().getColor(R.color.selected));
                        }else{
                            userListView.getChildAt(positionSelected).setBackgroundColor(Color.TRANSPARENT);
                            positionSelected = position;
                            userListView.getChildAt(positionSelected).setBackgroundColor(getResources().getColor(R.color.selected));
                        }
                    }
                });
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Button createButton = popupView.findViewById(R.id.createPrizePool);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                    String endDate = ((EditText)popupView.findViewById(R.id.endDate)).getText().toString();
                    if(isDateValid(endDate)){
                        if(positionSelected == -1){
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.no_user_selected);
                        }else if(endDate.isEmpty()){
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.no_date_selected);
                        }else{
                            try {
                                Date date = new SimpleDateFormat(DATE_FORMAT, Locale.FRANCE).parse(endDate);

                                if(date.after(new Date())){
                                    final PrizePool prizePool = new PrizePool();
                                    prizePool.endDate = date.getTime();
                                    prizePool.closed = false;
                                    prizePool.wishlist = wishlist.id;
                                    prizePool.manager = Long.parseLong(((TextView)((ListView)popupView.findViewById(R.id.listOfUsers)).getChildAt(positionSelected).findViewById(R.id.userId)).getText().toString());

                                    new Thread(new Runnable() {
                                        public void run() {
                                            PrizePoolApi prizePoolApi = new PrizePoolApi();
                                            try {
                                                wishlist.prizePool = prizePoolApi.create(prizePool);
                                            } catch (ExecutionException e) {
                                                ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.problem_creating_prizepool);
                                                e.printStackTrace();
                                            } catch (InterruptedException e) {
                                                ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.problem_creating_prizepool);
                                                e.printStackTrace();
                                            } catch (JSONException e) {
                                                ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.problem_creating_prizepool);
                                                e.printStackTrace();
                                            } catch (UnsupportedEncodingException e) {
                                                ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.problem_creating_prizepool);
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                    popupWindow.dismiss();
                                }else{
                                    ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.date_passed);
                                }
                            } catch (ParseException e) {
                                ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.wrong_format);
                            }
                        }
                    }else{
                        ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.wrong_format);
                    }
                }
            });

            // dismiss the popup window when touched
            popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
                public void onSwipeBottom() {
                    popupWindow.dismiss();
                }
                public void onSwipeTop() {
                }
            });
        }else if(wishlist.prizePool == null){
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.no_prizepool, null);
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
        }else if(!(new Date(wishlist.prizePool.endDate)).after(new Date()) && (wishlist.prizePool.manager != user.id || wishlist.prizePool.closed)){
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.no_prizepool, null);
            popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

            ((TextView)popupView.findViewById(R.id.title)).setText(R.string.end_of_prizepool);
            // dismiss the popup window when touched
            popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
                public void onSwipeBottom() {
                    popupWindow.dismiss();
                }
                public void onSwipeTop() {
                }
            });
        }else if(!(new Date(wishlist.prizePool.endDate)).after(new Date()) && wishlist.prizePool.manager == user.id){
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.no_prizepool, null);
            popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

            ((TextView)popupView.findViewById(R.id.title)).setText(R.string.end_of_prizepool);
            popupView.findViewById(R.id.endLayout).setVisibility(View.VISIBLE);

            popupView.findViewById(R.id.getDonation).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrizePoolApi prizePoolApi = new PrizePoolApi();
                    try {
                        long prizePoolId = wishlist.prizePoolId != null ? wishlist.prizePoolId : wishlist.prizePool.id;
                        Log.i("TAGTAG","prizePoolId="+prizePoolId);
                        JSONObject res = prizePoolApi.collectDonations(prizePoolId);
                        if(res.get("status").toString().equals("200")){
                            Toast.makeText(DetailsActivity.this, "Donation have been transferred", Toast.LENGTH_LONG).show();
                            popupWindow.dismiss();
                        }else{
                            Toast.makeText(DetailsActivity.this, "Donation couldn't be transferred", Toast.LENGTH_LONG).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });

            // dismiss the popup window when touched
            popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
                public void onSwipeBottom() {
                    popupWindow.dismiss();
                }
                public void onSwipeTop() {
                }
            });
        }else{
            view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View popupView = inflater.inflate(R.layout.donation_prizepool, null);
            popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

            // create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

            ((TextView)popupView.findViewById(R.id.managerValue)).setText(wishlist.prizePool.concernedManager.firstName + " " + wishlist.prizePool.concernedManager.lastName);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(wishlist.prizePool.endDate);

            int mYear = calendar.get(Calendar.YEAR);
            int mMonth = calendar.get(Calendar.MONTH);
            int mDay = calendar.get(Calendar.DAY_OF_MONTH);

            ((TextView)popupView.findViewById(R.id.endDateValue)).setText(mDay + "/" + (mMonth + 1) + "/" + mYear);

            ImageView showButton = popupView.findViewById(R.id.showIcon);
            showButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDonationList(popupView);
                }
            });

            Button createButton = popupView.findViewById(R.id.createDonation);
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                    String donation = ((EditText)popupView.findViewById(R.id.donation_amount)).getText().toString();
                    if(!donation.equals("")){
                        final Pattern pattern = Pattern.compile(regExp);

                        Matcher matcher = pattern.matcher(donation);
                        if(matcher.matches()){
                            amount = donation;
                            PayPalPayment payPalPayment = new PayPalPayment(
                                    new BigDecimal(amount),
                                    "EUR",
                                    "Faire un don",
                                    PayPalPayment.PAYMENT_INTENT_SALE);
                            Intent intent = new Intent(DetailsActivity.this, PaymentActivity.class);
                            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfig);
                            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                        }else{
                            ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.price_wrong_format);
                        }
                    }else{
                        ((TextView)popupView.findViewById(R.id.errorText)).setText(R.string.empty_donation);
                    }
                }
            });



            // dismiss the popup window when touched
            popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
                public void onSwipeBottom() {
                    popupWindow.dismiss();
                }
                public void onSwipeTop() {
                }
            });
        }
    }

    public static boolean isDateValid(String date){
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public void uploadImage(View view){
        requestStoragePermission();
        imageView = view.findViewById(R.id.uploadedImage);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void uploadMultipart(long id) {
        //String caption = etCaption.getText().toString().trim();

        //getting the actual path of the image
        String  path = getPath(filePath);


        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, ApiConfig.SAVE_FILE_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", "item_" + id + ".jpg") //Adding text parameter to the request
                    //.setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {
        Log.i("TAGTAG","uri"+uri);
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    public void showSharedUser(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.show_users, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        UserApi userApi = new UserApi();
        try {
            List<User> userList = new LinkedList<User>();
            for(User user: userApi.findAll()){
                for(Wishlist list: user.wishlists){
                    if(list.id == this.wishlist.id){
                        userList.add(user);
                    }
                }
                for(Wishlist list: user.concernedWishlists){
                    if(list.id == this.wishlist.id){
                        userList.add(user);
                    }
                }
            }
            ListView userListView = popupView.findViewById(R.id.listOfUsers);
            UserListAdapter adapter = new UserListAdapter(this, R.layout.show_users, userList);
            userListView.setAdapter(adapter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });
    }

    public void showDonationList(final View view){
        view.findViewById(R.id.firstRow).setVisibility(View.GONE);
        view.findViewById(R.id.secondRow).setVisibility(View.GONE);
        view.findViewById(R.id.thirdRow).setVisibility(View.GONE);
        view.findViewById(R.id.errorText).setVisibility(View.GONE);
        view.findViewById(R.id.createDonation).setVisibility(View.GONE);
        view.findViewById(R.id.listLayout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.showIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateDonation(view);
            }
        });

        DonationApi donationApi = new DonationApi();
        try {
            List<Donation> donationList = donationApi.findAll();
            List<Donation> finalList = new ArrayList<>();
            Double total = 0.0;

            for(Donation donation: donationList){
                if(donation.concernedPrizePool.id == wishlist.prizePool.id){
                    finalList.add(donation);
                    total += donation.amount;
                }
            }

            String string = getResources().getString(R.string.donation_list);
            String string2 = String.valueOf(total);

            ((TextView)view.findViewById(R.id.title)).setText(string + " " + string2 + "€");

            ListView listview = view.findViewById(R.id.listOfDonations);
            DonationListAdapter adapter = new DonationListAdapter(this,R.layout.list_of_donation,finalList);
            listview.setAdapter(adapter);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showCreateDonation(final View view){
        ((TextView)view.findViewById(R.id.title)).setText(R.string.donation_creation);
        view.findViewById(R.id.firstRow).setVisibility(View.VISIBLE);
        view.findViewById(R.id.secondRow).setVisibility(View.VISIBLE);
        view.findViewById(R.id.thirdRow).setVisibility(View.VISIBLE);
        view.findViewById(R.id.errorText).setVisibility(View.VISIBLE);
        view.findViewById(R.id.createDonation).setVisibility(View.VISIBLE);
        view.findViewById(R.id.listLayout).setVisibility(View.GONE);

        view.findViewById(R.id.showIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDonationList(view);
            }
        });
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
        popupView.findViewById(R.id.uploadedImage).setVisibility(View.GONE);

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

        Button createAmazonButton = popupView.findViewById(R.id.createAmazonItem);
        createAmazonButton.setText(R.string.create_amazon_button);
        createAmazonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
                String link = ((EditText) popupView.findViewById(R.id.link)).getText().toString();
                if (!link.isEmpty()) {
                    if (URLUtil.isValidUrl(link)) {
                        AmazonApi amazonApi = new AmazonApi();
                        try {
                            Item item = amazonApi.getItemFromUrl(link);
                            if(item != null){
                                tryCreateProduct(item.name, ((EditText)popupView.findViewById(R.id.description)).getText().toString(), item.amount.toString(), link, popupWindow);
                            }else{
                                ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.amazon_link_not_working);
                            }
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.product_link_problem);
                    }
                } else {
                    ((TextView) popupView.findViewById(R.id.errorText)).setText(R.string.link_empty);
                }
            }
        });

        popupView.findViewById(R.id.recomendationImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                showRecomendation(v);
            }
        });

        ImageView uploadImage = popupView.findViewById(R.id.uploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(popupView);
            }
        });
    }

    private void showRecomendation(View view){
        view.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.button_anim));
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.show_recommendation, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        HttpClient httpClient = new HttpClient();

        try {
            String param = "";
            for(Category category: user.categories){
                param += category.name + ";";
            }
            if(!param.equals("")){
                param = param.substring(0, param.length()-1);

                String result = httpClient.get(ApiConfig.RECOMMENDATION_URL + "?categories=" + param).get();
                final JSONObject jsonObject;
                jsonObject = new JSONObject(result);

                ArrayList<String> listTitle = new ArrayList<String>();
                ArrayList<String> listLink = new ArrayList<String>();
                JSONArray jArray = (JSONArray)((JSONObject)jsonObject.get("result")).get("result");
                if (jArray != null) {
                    for (int i=0;i<jArray.length();i++){
                        listTitle.add(((JSONObject)jArray.get(i)).get("title").toString());
                        listLink.add(((JSONObject)jArray.get(i)).get("link").toString());
                    }
                }

                final ListView listview = popupView.findViewById(R.id.listOfRecommendation);
                RecommendationListAdapter adapter = new RecommendationListAdapter(this,R.layout.list_of_item, listTitle, listLink);
                listview.setAdapter(adapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });
    }

    private void tryCreateProduct(String productName, String description, String amount, String link, PopupWindow popupWindow){
        ItemApi itemApi = new ItemApi();
        Item item = new Item();
        item.name = productName;
        item.description = description;
        item.amount = Double.parseDouble(amount);
        item.link = link;
        item.position = 0;
        item.wishlist = this.wishlist.id;
        try {
            item = itemApi.create(item);
            wishlist.items.add(item);
            if(filePath != null){
                uploadMultipart(item.id);
                filePath = null;
            }
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

    private void tryModifyProduct(long id, String productName, String description, String amount, String link, PopupWindow popupWindow){
        ItemApi itemApi = new ItemApi();
        for(Item item: wishlist.items){
            if(item.id == id){
                item.name = productName;
                item.description = description;
                item.amount = Double.parseDouble(amount);
                item.link = link;
                item.position = 0;
                item.wishlist = this.wishlist.id;
                try {
                    if(filePath != null){
                        uploadMultipart(item.id);
                        filePath = null;
                    }
                    itemApi.updateAttributes(item.id, item);
                    loadItem();
                    popupWindow.dismiss();
                    Thread.sleep(5000);
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
