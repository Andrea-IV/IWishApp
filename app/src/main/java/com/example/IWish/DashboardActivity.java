package com.example.IWish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    Context context;
    List<RowWishList> rowWishLists;
    ArrayList<WishList> wishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        context=this;
        loadWishList();
    }

    public void clickMe(View view){
        LinearLayout lay = (LinearLayout)view;
        String id = ((TextView)lay.getChildAt(2)).getText().toString();
        String title = ((TextView)lay.getChildAt(1)).getText().toString();

        Intent intent = new Intent(view.getContext(), DetailsActivity.class);
        Bundle b = new Bundle();
        b.putInt("ID", Integer.parseInt(id)); //Your id
        b.putString("TITLE", title);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void loadWishList(){
        ListView listview = findViewById(R.id.listOfWishlist);
        callWishList();
        DashboardListAdapter adapter = new DashboardListAdapter(this,R.layout.list_of_wishlist, rowWishLists);
        listview.setAdapter(adapter);
    }

    public void callWishList(){
        wishes = new ArrayList<>();
        rowWishLists = new ArrayList<>();

        wishes.add(new WishList(1, "Winter Wishes", true));
        rowWishLists.add(new RowWishList(1, 1, "Winter Wishes", true));

        wishes.add(new WishList(2, "For The People", true));
        rowWishLists.add(new RowWishList(2, 2, "For The People", true));

        wishes.add(new WishList(3, "Sharing is Caring", false));
        rowWishLists.add(new RowWishList(3, 3, "Sharing is Caring", false));

        wishes.add(new WishList(4, "Give It To Me!", false));
        rowWishLists.add(new RowWishList(4, 4, "Give It To Me!", false));

        wishes.add(new WishList(5, "Winter Wishes 2", true));
        rowWishLists.add(new RowWishList(5, 5, "Winter Wishes 2", true));

        wishes.add(new WishList(6, "For The People 2", false));
        rowWishLists.add(new RowWishList(6, 6, "For The People 2", false));

        wishes.add(new WishList(7, "Sharing is Caring 2", true));
        rowWishLists.add(new RowWishList(7, 7, "Sharing is Caring 2", true));

        wishes.add(new WishList(8, "Give It To Me! 2", false));
        rowWishLists.add(new RowWishList(8, 8, "Give It To Me! 2", false));
    }

}
