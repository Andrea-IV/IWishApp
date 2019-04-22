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

public class DashboardActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        context=this;
        ListView listview = findViewById(R.id.listOfWishlist);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Toast.makeText(context, "An item of the ListView is clicked.", Toast.LENGTH_LONG).show();
            }
        });

        String[] items={"Winter Wishes","For The People","Sharing is Caring","Give It To Me!","5","5","5","5","5","5","5","5","5","5","5","5","5","5"};
        DashboardListAdapter adapter = new DashboardListAdapter(this,R.layout.list_of_wishelist,items);
        listview.setAdapter(adapter);
    }

    public void clickMe(View view){
        LinearLayout lay = (LinearLayout)view;
        TextView txt = (TextView)lay.getChildAt(0);
        Toast.makeText(this, "Button "+txt.getText().toString(),Toast.LENGTH_LONG).show();
    }

}
