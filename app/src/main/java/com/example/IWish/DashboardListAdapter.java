package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DashboardListAdapter extends ArrayAdapter<RowItem> {
    private int groupid;
    private String[] item_list;
    private Context context;
    private List<RowItem> wishList;


    public DashboardListAdapter(Context context, int vg, List<RowItem> rowItem){
        super(context,vg, rowItem);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;
        this.wishList = rowItem;
    }

    static class ViewHolder {
        ImageView imageView;
        ImageView locked;
        TextView textView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_wishelist, null);
            holder = new ViewHolder();
            holder.locked = (ImageView) convertView.findViewById(R.id.locked);
            holder.textView = (TextView) convertView.findViewById(R.id.wishlistText);
            holder.imageView = (ImageView) convertView.findViewById(R.id.profile_image);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(rowItem.isPublic()){
            holder.locked.setVisibility(View.INVISIBLE);
        }else{
            holder.locked.setVisibility(View.VISIBLE);
        }
        holder.textView.setText(rowItem.getTitle());

        return convertView;
    }
}
