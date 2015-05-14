package com.barliftapp.barlift.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barliftapp.barlift.R;
import com.barliftapp.barlift.util.CircleTransform;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class NudgeAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Object> nudges;

    public NudgeAdapter(Context c, ArrayList<Object> nudges) {
        this.nudges = nudges;
        mContext = c;
    }

    public int getCount() {
        return nudges.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            convertView = inflater.inflate(R.layout.item_nudge, null);

            mHolder = new ViewHolder();

            mHolder.textView=(TextView) convertView.findViewById(R.id.tv_nudge_item);
            mHolder.dateView=(TextView) convertView.findViewById(R.id.tv_nudge_date);
            mHolder.imageView=(ImageView) convertView.findViewById(R.id.iv_nudge_item);


        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        ParseObject nudge = (ParseObject) nudges.get(position);
        mHolder.textView.setText(nudge.getString("text"));
        String date = new SimpleDateFormat("MMMM d").format(nudge.getCreatedAt());
        mHolder.dateView.setText(date);
        ParseObject fromUser = nudge.getParseObject("from_user");
        Picasso.with(mContext)
                .load("https://graph.facebook.com/" + fromUser.getString("fb_id") + "/picture?type=normal&height=150&width=150")
                .transform(new CircleTransform())
                .into(mHolder.imageView);

        convertView.setTag(mHolder);

        return convertView;
    }
    private class ViewHolder {
        private TextView textView;
        private TextView dateView;
        private ImageView imageView;
    }
}