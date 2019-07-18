package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IWish.Model.Category;
import com.example.IWish.Model.Donation;
import com.example.IWish.Model.Item;

import java.util.List;

public class CategoriesListAdapter extends ArrayAdapter<Category> {
    private Context context;

    public CategoriesListAdapter(Context context, int vg, List<Category> rowItemList){
        super(context,vg, rowItemList);
        this.context = context;
    }

    static class ViewHolder {
        CheckBox checkBox;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CategoriesListAdapter.ViewHolder holder = null;
        Category category = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_categories, null);
            holder = new CategoriesListAdapter.ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.categoryCheck);
            convertView.setTag(holder);
        } else{
            holder = (CategoriesListAdapter.ViewHolder) convertView.getTag();
        }

        holder.checkBox.setText(category.name);

        return convertView;
    }
}
