package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IWish.Model.Wishlist;

import java.util.List;

public class DashboardListAdapter extends ArrayAdapter<RowWishList> {
    private Context context;


    public DashboardListAdapter(Context context, int vg, List<RowWishList> rowWishList){
        super(context,vg, rowWishList);
        this.context=context;
    }

    static class ViewHolder {
        ImageView imageView;
        ImageView locked;
        TextView textView;
        TextView idView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowWishList rowWishList = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_wishlist, null);
            holder = new ViewHolder();
            holder.locked = (ImageView) convertView.findViewById(R.id.locked);
            holder.textView = (TextView) convertView.findViewById(R.id.wishlistText);
            holder.idView = (TextView) convertView.findViewById(R.id.wishlistId);
            holder.imageView = (ImageView) convertView.findViewById(R.id.profile_image);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(rowWishList.isPublic()){
            holder.locked.setVisibility(View.INVISIBLE);
        }else{
            holder.locked.setVisibility(View.VISIBLE);
        }
        holder.textView.setText(rowWishList.getTitle());
        holder.idView.setText(String.valueOf(rowWishList.getId()));

        return convertView;
    }
}
