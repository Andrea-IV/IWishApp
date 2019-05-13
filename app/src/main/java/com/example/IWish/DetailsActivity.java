package com.example.IWish;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    protected ArrayList<WishedItem> wishes;

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

    public void loadItem(int wishListID){
        ListView listview = findViewById(R.id.listOfItem);
        callItem(wishListID);
        DetailsListAdapter adapter = new DetailsListAdapter(this,R.layout.list_of_item, wishes);
        listview.setAdapter(adapter);
    }

    public void goToDetails(View view){
        Intent intent = new Intent(view.getContext(), ProductActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void callItem(int wishListID){
        wishes = new ArrayList<>();

        wishes.add(new WishedItem(1, "Gloves", "Warm gloves for winter", 15.20, "1", "google.fr", 1, "clothes"));

        wishes.add(new WishedItem(2, "Hot Coffee Mug", "A great mug for warm coffee", 20.20, "2", "google.fr", 2, "house"));

        wishes.add(new WishedItem(3, "Travel to Sweden", "Sweden covered in snow would be a great gift", 400.0, "3", "google.fr", 3, "travel"));

        wishes.add(new WishedItem(4, "Timberland", "Warm shoes for winter", 120.20, "4", "google.fr", 4, "clothes"));

        wishes.add(new WishedItem(5, "Snowboard", "I want to learn snowboard, but i want my own", 80.20, "1", "google.fr", 5, "clothes"));

        wishes.add(new WishedItem(6, "Winter Hat", "To keep my ears warm", 14.20, "6", "google.fr", 6, "clothes"));

    }
}
