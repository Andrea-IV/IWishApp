package com.example.IWish;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.IWish.Model.Donation;
import com.example.IWish.Model.Item;

import java.util.List;

import static com.example.IWish.ApiConfig.IMAGES_URL;

public class RecommendationListAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> links;

    public RecommendationListAdapter(Context context, int vg, List<String> title, List<String> links){
        super(context,vg, title);
        this.context = context;
        this.links = links;
    }

    static class ViewHolder {
        TextView titleView;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        DonationListAdapter.ViewHolder holder = null;
        String title = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_recommendation, null);
            holder = new DonationListAdapter.ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.recommendationTitle);
            convertView.setTag(holder);
        } else{
            holder = (DonationListAdapter.ViewHolder) convertView.getTag();
        }

        holder.titleView.setText(title);

        holder.titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(links.get(position)));
                context.startActivity(i);
            }
        });
        return convertView;
    }
}
