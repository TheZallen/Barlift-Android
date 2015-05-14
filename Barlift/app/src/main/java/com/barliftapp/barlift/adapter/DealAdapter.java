package com.barliftapp.barlift.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barliftapp.barlift.R;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            mHolder = new ViewHolder();
            if (deals.get(position).getBoolean("main")) {
                convertView = inflater.inflate(R.layout.item_deal, null);

                mHolder.barNameView = (TextView) convertView.findViewById(R.id.di_barName);
                mHolder.dealView = (TextView) convertView.findViewById(R.id.di_barDeal);
                mHolder.communityView = (TextView) convertView.findViewById(R.id.di_community);
                mHolder.numberView = (TextView) convertView.findViewById(R.id.di_number);
                mHolder.extraView = (TextView) convertView.findViewById(R.id.di_moredealsbutton);
                mHolder.backView = (ImageView) convertView.findViewById(R.id.di_dealback);
            }else{
                convertView = inflater.inflate(R.layout.item_deal_small, null);

                mHolder.barNameView = (TextView) convertView.findViewById(R.id.di_barNameSmall);
                mHolder.dealView = (TextView) convertView.findViewById(R.id.di_barDealSmall);
                mHolder.communityView = (TextView) convertView.findViewById(R.id.di_communitySmall);
                mHolder.numberView = (TextView) convertView.findViewById(R.id.di_numberSmall);
                mHolder.backView = (ImageView) convertView.findViewById(R.id.di_dealbackSmall);
            }

        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.barNameView.setText(deals.get(position).getParseObject("venue").getString("bar_name"));
        mHolder.dealView.setText(deals.get(position).getString("name").replace("\\n", "\n"));
        mHolder.communityView.setText(deals.get(position).getString("community_name"));
        mHolder.numberView.setText(deals.get(position).getNumber("num_accepted").toString());
        if (deals.get(position).getBoolean("main") && deals.get(position).getList("add_deals") != null && mHolder.extraView != null && deals.get(position).getList("add_deals").size() > 0) {
            mHolder.extraView.setText("+" + (deals.get(position).getList("add_deals").size()) + " more " + ((deals.get(position).getList("add_deals").size() - 1 == 1) ? "deal" : "deals"));
        }
        Picasso.with(mContext)
                .load(deals.get(position).getString("image_url"))
                .into(mHolder.backView);

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
            holder.date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.number = (TextView) convertView.findViewById(R.id.tv_number);
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
            headerText = "TODAY";
        } else if (c.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)+1){
            headerText = "TOMORROW";
        } else {
//            headerText = format.format(c.get(Calendar.DAY_OF_WEEK));
            headerText = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toUpperCase();
        }
        holder.date.setText(c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + c.get(Calendar.DAY_OF_MONTH));
        holder.text.setText(headerText);

        int tempDay = c.get(Calendar.DAY_OF_MONTH);
        int counter = 0;
        while (c.get(Calendar.DAY_OF_MONTH) == tempDay){
            counter++;
            if (position+counter >= deals.size())
                break;
            c.setTime(deals.get(position+counter).getDate("deal_start_date"));
        }
        holder.number.setText("" + counter);
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
        private TextView text;
        private TextView date;
        private TextView number;
    }

    private class ViewHolder {
        private TextView barNameView;
        private TextView dealView;
        private TextView communityView;
        private TextView numberView;
        private TextView extraView;
        private ImageView backView;
    }
}
