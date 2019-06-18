package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IWish.Model.Item;

import java.util.List;

public class DetailsListAdapter extends ArrayAdapter<Item> {
    private Context context;

    public DetailsListAdapter(Context context, int vg, List<Item> rowItemList){
        super(context,vg, rowItemList);
        this.context = context;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView idView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DetailsListAdapter.ViewHolder holder = null;
        Item wishedItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_item, null);
            holder = new DetailsListAdapter.ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.itemTitle);
            holder.idView = (TextView) convertView.findViewById(R.id.itemId);
            holder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
            convertView.setTag(holder);
        } else{
            holder = (DetailsListAdapter.ViewHolder) convertView.getTag();
        }

        holder.titleView.setText(wishedItem.name);
        holder.idView.setText(String.valueOf(wishedItem.id));

        return convertView;
    }
}
