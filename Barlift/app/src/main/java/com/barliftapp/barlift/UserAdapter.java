package com.barliftapp.barlift;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class UserAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context mContext;
    private ArrayList<Object> friends;
    private LayoutInflater inflater;
    private boolean hash;

    public UserAdapter(Context c, ArrayList<Object> friends, boolean hash) {
        this.mContext = c;
        this.friends = friends;
        this.hash = hash;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return friends.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_user, null);
            mHolder = new ViewHolder();

            mHolder.textView=(TextView) convertView.findViewById(R.id.tv_item);
            mHolder.imageView=(ImageView) convertView.findViewById(R.id.iv_friend_item);


        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        if (hash) {
            HashMap<String, String> deets = (HashMap<String, String>) friends.get(position);

            mHolder.textView.setText(deets.get("name"));
            //            mHolder.imageView.setProfileId(deets.get("fb_id"));
            Picasso.with(mContext)
                    .load("https://graph.facebook.com/" + deets.get("fb_id") + "/picture?type=normal&height=100&width=100")
                    .transform(new CircleTransform())
                    .into(mHolder.imageView);


        }else{
            ArrayList<String> deets = (ArrayList<String>)friends.get(position);
            mHolder.textView.setText(deets.get(0));
            //            mHolder.imageView.setProfileId(deets.get("fb_id"));
            Picasso.with(mContext)
                    .load("https://graph.facebook.com/" + deets.get(1) + "/picture?type=normal&height=100&width=100")
                    .transform(new CircleTransform())
                    .into(mHolder.imageView);
        }
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

        String headerText = "Friends";

        holder.text.setText(headerText);
//        convertView.setVisibility((false) ? View.GONE : View.VISIBLE);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return 0;
    }

    private class HeaderViewHolder {
        TextView text;
    }

    private class ViewHolder {
        private TextView textView;
        private ImageView imageView;
    }
}