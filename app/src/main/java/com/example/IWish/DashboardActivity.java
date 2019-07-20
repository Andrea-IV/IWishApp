package com.example.IWish;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.IWish.Model.Category;
import com.example.IWish.Model.User;
import com.example.IWish.Model.Wishlist;
import com.example.IWish.api.CategoryApi;
import com.example.IWish.api.UserApi;
import com.example.IWish.api.WishlistApi;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import net.gotev.uploadservice.MultipartUploadRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.example.IWish.ApiConfig.IMAGES_URL;

public class DashboardActivity extends AppCompatActivity {
    public static final int IMAGE_REQUEST_CODE = 3;
    public static final int STORAGE_PERMISSION_CODE = 123;

    Context context;
    List<RowWishList> rowWishLists;
    User user;
    Boolean facebookLogin = false;
    List<User> userList;
    ImageView imageView;
    Uri filePath;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        context = this;
        callbackManager = CallbackManager.Factory.create();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            try {
                this.user = new User(new JSONObject(bundle.getString("USER")));
                this.facebookLogin = (bundle.getString("FACEBOOK")).equals("YES");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        loadWishList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
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
                                showDeleteWishlist(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position, false);
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

                        swipedView.findViewById(R.id.facebookShare).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ShareLinkContent content = new ShareLinkContent.Builder()
                                        .setContentUrl(Uri.parse("https://developers.facebook.com"))
                                        .build();
                                if(ShareDialog.canShow(ShareLinkContent.class)){
                                    ShareDialog shareDialog = new ShareDialog(DashboardActivity.this);
                                    shareDialog.show(content);
                                }
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            if(swipedView.findViewById(R.id.locked).getVisibility() == View.VISIBLE){
                                                WishlistApi wishlistApi = new WishlistApi();
                                                user.wishlists.get(position).isPublic = true;
                                                wishlistApi.updateAttributes(user.wishlists.get(position).id, user.wishlists.get(position));
                                                swipedView.findViewById(R.id.locked).setVisibility(View.INVISIBLE);
                                            }
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
                            }
                        });

                        swipedView.findViewById(R.id.inviteUser).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UserApi userApi = new UserApi();
                                try {
                                    userList = new LinkedList<User>();
                                    int id = Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString());
                                    boolean add;
                                    for(User user: userApi.findAll()){
                                        add = true;
                                        for(Wishlist list: user.wishlists){
                                            if(list.id == id){
                                                Log.i("USER", id + " " + list.id);
                                                add = false;
                                            }
                                        }
                                        for(Wishlist list: user.concernedWishlists){
                                            if(list.id == id){
                                                Log.i("USER", String.valueOf(id) + " " + String.valueOf(list.id));
                                                add = false;
                                            }
                                        }
                                        if(add){
                                            userList.add(user);
                                        }else{
                                            Log.i("USER", user.toString());
                                        }
                                    }
                                    showShareList(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position);
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
                                showDeleteWishlist(v, Integer.parseInt(((TextView)swipedView.findViewById(R.id.wishlistId)).getText().toString()), ((TextView)swipedView.findViewById(R.id.wishlistText)).getText().toString(), position, true);
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

    //This method will be called when the user will tap on allow or deny
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

    public void uploadImage(View view){
        requestStoragePermission();
        imageView = view.findViewById(R.id.uploadedImage);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), IMAGE_REQUEST_CODE);
    }

    public void showModifyUser(View view){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.user_profile, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);

        ((TextView)popupView.findViewById(R.id.lastName)).setText(user.lastName);
        ((TextView)popupView.findViewById(R.id.firstName)).setText(user.firstName);
        ((TextView)popupView.findViewById(R.id.emailUser)).setText(user.email);
        new DownloadImageTask((ImageView) popupView.findViewById(R.id.uploadedImage))
                .execute(IMAGES_URL + "pp_" + user.id + ".jpg");

        if(((ImageView) popupView.findViewById(R.id.uploadedImage)).getDrawable() == null){
            ((ImageView) popupView.findViewById(R.id.uploadedImage)).setImageDrawable(getResources().getDrawable(R.drawable.example));
        }

        popupView.findViewById(R.id.uploadedImage).setVisibility(View.VISIBLE);


        if(facebookLogin){
            popupView.findViewById(R.id.emailUser).setVisibility(View.GONE);
        }

        popupView.findViewById(R.id.createItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lastname = ((TextView)popupView.findViewById(R.id.lastName)).getText().toString();
                String firstname = ((TextView)popupView.findViewById(R.id.firstName)).getText().toString();
                String email = ((TextView)popupView.findViewById(R.id.emailUser)).getText().toString();
                if(!lastname.equals("") && !firstname.equals("") && !email.equals("")){
                    tryModifyUser(email, firstname, lastname, popupWindow);
                }
            }
        });

        popupView.findViewById(R.id.uploadImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(popupView);
            }
        });

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DashboardActivity.this) {
            public void onSwipeBottom() {
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });
    }

    public void uploadMultipart() {
        //String caption = etCaption.getText().toString().trim();

        //getting the actual path of the image
        String path = getPath(filePath);

        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, ApiConfig.SAVE_FILE_URL)
                    .addFileToUpload(path, "image") //Adding file
                    .addParameter("caption", "pp_" + user.id + ".jpg")
                    //.setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String getPath(Uri uri) {
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

    public void tryModifyUser(String email, String firstname, String lastname, PopupWindow popupWindow){
        this.user.email = email;
        this.user.firstName = firstname;
        this.user.lastName = lastname;

        popupWindow.dismiss();

        new Thread(new Runnable() {
            public void run() {
                uploadMultipart();
                UserApi userApi = new UserApi();
                try {
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
    }

    public void showMenu(final View view){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.menu_users, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.left_in));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        final ListView categoriesListView = popupView.findViewById(R.id.listOfCategories);
        final List<Category> categoryList;

        popupWindow.showAtLocation(view, Gravity.LEFT, 0, 0);

        CategoryApi categoryApi = new CategoryApi();

        try {
            categoryList = categoryApi.findAll();

            CategoriesListAdapter adapter = new CategoriesListAdapter(this, R.layout.list_of_categories, categoryList, user);
            categoriesListView.setAdapter(adapter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        popupView.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.categories = new ArrayList<>();
                for(int i = 0; i < categoriesListView.getChildCount(); i++){
                    if(((CheckBox)categoriesListView.getChildAt(i).findViewById(R.id.categoryCheck)).isChecked()){
                        final int value = i;
                        new Thread(new Runnable() {
                            public void run() {
                                CategoryApi categoryApi = new CategoryApi();
                                try {
                                    String string = ((TextView)categoriesListView.getChildAt(value).findViewById(R.id.categoryId)).getText().toString();
                                    user.categories.add(categoryApi.findById(Integer.parseInt(string)));
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                logout();
            }
        });

        popupView.findViewById(R.id.btnProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.categories = new ArrayList<>();
                for(int i = 0; i < categoriesListView.getChildCount(); i++){
                    if(((CheckBox)categoriesListView.getChildAt(i).findViewById(R.id.categoryCheck)).isChecked()){
                        final int value = i;
                        new Thread(new Runnable() {
                            public void run() {
                                CategoryApi categoryApi = new CategoryApi();
                                try {
                                    String string = ((TextView)categoriesListView.getChildAt(value).findViewById(R.id.categoryId)).getText().toString();
                                    user.categories.add(categoryApi.findById(Integer.parseInt(string)));
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                popupWindow.dismiss();
                showModifyUser(view);
            }
        });

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new OnSwipeTouchListener(DashboardActivity.this) {
            public void onSwipeLeft(float x, float y) {
                user.categories = new ArrayList<>();
                for(int i = 0; i < categoriesListView.getChildCount(); i++){
                    if(((CheckBox)categoriesListView.getChildAt(i).findViewById(R.id.categoryCheck)).isChecked()){
                        final int value = i;
                        new Thread(new Runnable() {
                            public void run() {
                                CategoryApi categoryApi = new CategoryApi();
                                try {
                                    String string = ((TextView)categoriesListView.getChildAt(value).findViewById(R.id.categoryId)).getText().toString();
                                    user.categories.add(categoryApi.findById(Integer.parseInt(string)));
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                popupWindow.dismiss();
            }
            public void onSwipeTop() {
            }
        });
    }

    public void logout(){
        if(facebookLogin){
            LoginManager.getInstance().logOut();
        }
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
                        try {
                            userApi.addConcerned(user.id, idWishlist);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
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

    public void showDeleteWishlist(View view, final int id, String name, final int position, final boolean owner){
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
                if(owner){
                    tryDeleteWishlist(id, position);
                }else{
                    tryDeleteConcernedWishlist(id, position);
                }
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
            user.wishlists.remove(position);
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

    public void tryDeleteConcernedWishlist(int id, int position){
        UserApi userApi = new UserApi();
        try {
            userApi.deleteConcerned(user.id, id);
            user.wishlists.remove(position);
            loadWishList();
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
        for(Wishlist wishlist: user.wishlists){
            rowWishLists.add(new RowWishList(wishlist, true));
        }
        for(Wishlist wishlist: user.concernedWishlists){
            rowWishLists.add(new RowWishList(wishlist, false));
        }
    }

}
