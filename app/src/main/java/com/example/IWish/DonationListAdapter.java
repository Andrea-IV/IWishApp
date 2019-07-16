package com.example.IWish;

import android.app.Activity;
import android.content.Context;
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

public class DonationListAdapter extends ArrayAdapter<Donation> {
    private Context context;

    public DonationListAdapter(Context context, int vg, List<Donation> rowItemList){
        super(context,vg, rowItemList);
        this.context = context;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView donationAmount;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        DonationListAdapter.ViewHolder holder = null;
        Donation donation = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_of_donation, null);
            holder = new DonationListAdapter.ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.donationTitle);
            holder.donationAmount = (TextView) convertView.findViewById(R.id.donationAmount);
            holder.imageView = (ImageView) convertView.findViewById(R.id.itemImage);
            convertView.setTag(holder);
        } else{
            holder = (DonationListAdapter.ViewHolder) convertView.getTag();
        }

        holder.titleView.setText(donation.concernedDonor.firstName + " " + donation.concernedDonor.lastName);
        holder.donationAmount.setText(donation.amount + " €");

        return convertView;
    }
}
