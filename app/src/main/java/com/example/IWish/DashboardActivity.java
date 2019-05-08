package com.example.IWish;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    Context context;
    List<RowItem> rowItems;
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
        TextView txt = (TextView)lay.getChildAt(1);
        Toast.makeText(this, "Button "+txt.getText().toString(),Toast.LENGTH_LONG).show();
    }

    public void loadWishList(){
        ListView listview = findViewById(R.id.listOfWishlist);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Toast.makeText(context, "An item of the ListView is clicked.", Toast.LENGTH_LONG).show();
            }
        });

        callWishList();
        DashboardListAdapter adapter = new DashboardListAdapter(this,R.layout.list_of_wishelist, rowItems);
        listview.setAdapter(adapter);
    }

    public void callWishList(){
        wishes = new ArrayList<>();
        rowItems = new ArrayList<>();

        wishes.add(new WishList(1, "Winter Wishes", true));
        rowItems.add(new RowItem(1, "Winter Wishes", true));

        wishes.add(new WishList(2, "For The People", true));
        rowItems.add(new RowItem(2, "For The People", true));

        wishes.add(new WishList(3, "Sharing is Caring", false));
        rowItems.add(new RowItem(3, "Sharing is Caring", false));

        wishes.add(new WishList(4, "Give It To Me!", false));
        rowItems.add(new RowItem(4, "Give It To Me!", false));

        wishes.add(new WishList(5, "Winter Wishes 2", true));
        rowItems.add(new RowItem(5, "Winter Wishes 2", true));

        wishes.add(new WishList(6, "For The People 2", false));
        rowItems.add(new RowItem(6, "For The People 2", false));

        wishes.add(new WishList(7, "Sharing is Caring 2", true));
        rowItems.add(new RowItem(7, "Sharing is Caring 2", true));

        wishes.add(new WishList(8, "Give It To Me! 2", false));
        rowItems.add(new RowItem(8, "Give It To Me! 2", false));
    }

}
