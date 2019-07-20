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
import com.example.IWish.Model.User;
import com.example.IWish.api.UserApi;

import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CategoriesListAdapter extends ArrayAdapter<Category> {
    private Context context;
    private User user;

    public CategoriesListAdapter(Context context, int vg, List<Category> rowItemList, User user){
        super(context,vg, rowItemList);
        this.context = context;
        this.user = user;
    }

    static class ViewHolder {
        CheckBox checkBox;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        CategoriesListAdapter.ViewHolder holder = null;
        final Category category = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_categories, null);
            holder = new CategoriesListAdapter.ViewHolder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.categoryCheck);
            convertView.setTag(holder);
            for(Category userCategory: user.categories){
                if(userCategory.id == category.id){
                    holder.checkBox.setChecked(true);
                }
            }
        } else{
            holder = (CategoriesListAdapter.ViewHolder) convertView.getTag();
        }

        holder.checkBox.setText(category.name);

        final CategoriesListAdapter.ViewHolder finalHolder = holder;

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalHolder.checkBox.isChecked()){
                    new Thread(new Runnable() {
                        public void run() {
                            UserApi userApi = new UserApi();
                            try {
                                userApi.addCategory(user.id, category.id);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }else{
                    new Thread(new Runnable() {
                        public void run() {
                            UserApi userApi = new UserApi();
                            try {
                                userApi.deleteCategory(user.id, category.id);
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
        });

        return convertView;
    }
}
