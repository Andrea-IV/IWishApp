package com.example.IWish;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DashboardListAdapter extends ArrayAdapter<String> {
    private int groupid;
    private String[] item_list;
    private Context context;


    public DashboardListAdapter(Context context, int vg, String[] item_list){
        super(context,vg, item_list);
        this.context=context;
        groupid=vg;
        this.item_list=item_list;

    }

    static class ViewHolder {
        public LinearLayout linearLayout;
        public TextView textView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView= inflater.inflate(groupid, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.linearLayout= rowView.findViewById(R.id.wishButtonLayout);
            viewHolder.textView= rowView.findViewById(R.id.wishlistText);
            rowView.setTag(viewHolder);

        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.textView.setText(item_list[position]);
        return rowView;
    }
}
