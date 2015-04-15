package com.barliftapp.barlift;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;


/**
 * DealAdapter for Listview in ListActivity
 */
public class DealAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private Context mContext;
    private List<ParseObject> deals;
    private LayoutInflater inflater;

    public DealAdapter(Context c, List<ParseObject> deals) {
        this.deals = deals;
        mContext = c;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return deals.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.item_deal, null);

            mHolder = new ViewHolder();

            mHolder.barNameView=(TextView) convertView.findViewById(R.id.di_barName);
            mHolder.dealView=(TextView) convertView.findViewById(R.id.di_barDeal);


        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.barNameView.setText(deals.get(position).getParseObject("user").getString("bar_name"));
        mHolder.dealView.setText(deals.get(position).getString("name"));

        convertView.setTag(mHolder);

        return convertView;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.item_sticky_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.tv_header);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        Calendar c = Calendar.getInstance();
        Calendar current = Calendar.getInstance();
        c.setTime(deals.get(position).getDate("deal_start_date"));
        DateFormat format =new SimpleDateFormat("EEEE");
        String headerText = "";
        if (c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {
            headerText = "Today";
        } else if (c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)+1){
            headerText = "Tomorrow";
        } else {
            headerText = format.format(c.get(Calendar.DAY_OF_WEEK));
        }
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        Calendar c = Calendar.getInstance();
        c.setTime(deals.get(position).getDate("deal_start_date"));
        return c.get(Calendar.DAY_OF_MONTH);
    }

    private class HeaderViewHolder {
        TextView text;
    }

    private class ViewHolder {
        private TextView barNameView;
        private TextView dealView;
    }
}
