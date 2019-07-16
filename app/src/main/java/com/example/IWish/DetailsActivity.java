package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.example.IWish.Model.Donation;
import com.example.IWish.Model.Item;
import com.example.IWish.Model.PrizePool;
import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.AmazonApi;
import com.example.IWish.api.DonationApi;
import com.example.IWish.api.ItemApi;
import com.example.IWish.api.PrizePoolApi;
import com.example.IWish.api.UserApi;
import com.facebook.CallbackManager;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailsActivity extends AppCompatActivity {

    public static final int PAYPAL_REQUEST_CODE = 7171;

    private static PayPalConfiguration paypalConfig = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiConfig.PAYPAL_CLIENT_ID);

    private Wishlist wishlist;
    private User user;
    private Boolean owned;
    private int positionSelected = -1;
    final static String DATE_FORMAT = "dd/MM/yyyy";
    private String amount;
    CallbackManager callbackManager;
    final String regExp = "[0-9]+([,.][0-9]{1,2})?";

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

    public void goToDetails(View view, final Item item){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.display_product, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
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
                            userListView.getChildAt(positionSelected).setBackgroundColor(getResources().getColor(R.color.blueBackground));
                        }else{
                            userListView.getChildAt(positionSelected).setBackgroundColor(Color.TRANSPARENT);
                            positionSelected = position;
                            userListView.getChildAt(positionSelected).setBackgroundColor(getResources().getColor(R.color.blueBackground));
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

            ((TextView)popupView.findViewById(R.id.endDateValue)).setText(mDay + "/" + mMonth + "/" + mYear);

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

    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
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
                                tryCreateProduct(item.name, "", item.amount.toString(), link, popupWindow);
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
                    itemApi.updateAttributes(item.id, item);
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
