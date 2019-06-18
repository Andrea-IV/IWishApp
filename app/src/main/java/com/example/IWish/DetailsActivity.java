package com.example.IWish;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.IWish.Model.Item;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    protected ArrayList<Item> wishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle b = getIntent().getExtras();
        if(b != null){
            TextView title = findViewById(R.id.title);
            title.setText(b.getString("TITLE"));
            loadItem(b.getInt("ID"));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void loadItem(int wishListID){
        ListView listview = findViewById(R.id.listOfItem);
        callItem(wishListID);
        DetailsListAdapter adapter = new DetailsListAdapter(this,R.layout.list_of_item, wishes);
        listview.setAdapter(adapter);
    }

    public void goToDetails(View view){
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_product, null);
        popupView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bottom_up));

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
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
    }

    private void callItem(int wishListID){
        wishes = new ArrayList<>();
    /*
        wishes.add(new WishedItem(1, "Gloves", "Warm gloves for winter", 15.20, "1", "google.fr", 1, "clothes"));

        wishes.add(new WishedItem(2, "Hot Coffee Mug", "A great mug for warm coffee", 20.20, "2", "google.fr", 2, "house"));

        wishes.add(new WishedItem(3, "Travel to Sweden", "Sweden covered in snow would be a great gift", 400.0, "3", "google.fr", 3, "travel"));

        wishes.add(new WishedItem(4, "Timberland", "Warm shoes for winter", 120.20, "4", "google.fr", 4, "clothes"));

        wishes.add(new WishedItem(5, "Snowboard", "I want to learn snowboard, but i want my own", 80.20, "1", "google.fr", 5, "clothes"));

        wishes.add(new WishedItem(6, "Winter Hat", "To keep my ears warm", 14.20, "6", "google.fr", 6, "clothes"));
    */
    }
}
