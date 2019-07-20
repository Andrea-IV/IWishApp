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
import com.example.IWish.Model.User;

import java.util.List;

import static com.example.IWish.ApiConfig.IMAGES_URL;

public class UserListAdapter extends ArrayAdapter<User> {
    private Context context;

    public UserListAdapter(Context context, int vg, List<User> rowItemList){
        super(context,vg, rowItemList);
        this.context = context;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView idView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        UserListAdapter.ViewHolder holder = null;
        User user = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_user, null);
            holder = new UserListAdapter.ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.userTitle);
            holder.idView = (TextView) convertView.findViewById(R.id.userId);
            holder.imageView = (ImageView) convertView.findViewById(R.id.profile_image);
            convertView.setTag(holder);
        } else{
            holder = (UserListAdapter.ViewHolder) convertView.getTag();
        }

        holder.titleView.setText("#" + user.id + " " + user.firstName + " " + user.lastName);
        holder.idView.setText(String.valueOf(user.id));

        new DownloadImageTask(holder.imageView)
                .execute(IMAGES_URL + "pp_" + user.id + ".jpg");

        if(holder.imageView.getDrawable() == null){
            holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.example));
        }

        return convertView;
    }
}
