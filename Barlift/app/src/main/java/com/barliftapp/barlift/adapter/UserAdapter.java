package com.barliftapp.barlift.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.barliftapp.barlift.activity.FriendActivity;
import com.barliftapp.barlift.util.CircleTransform;
import com.barliftapp.barlift.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class UserAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context mContext;
    private ArrayList<Object> friends;
    private LayoutInflater inflater;
    private boolean hash = false;
    private boolean nudge = false;

    public UserAdapter(Context c, ArrayList<Object> friends, boolean hash, boolean nudge) {
        this.mContext = c;
        this.friends = friends;
        this.hash = hash;
        this.nudge = nudge;
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

            mHolder.textView = (TextView) convertView.findViewById(R.id.tv_item);
            mHolder.imageView = (ImageView) convertView.findViewById(R.id.iv_friend_item);
            mHolder.toggleButton = (TextView) convertView.findViewById(R.id.toggle_nudge);
            mHolder.arrowView = (ImageView) convertView.findViewById(R.id.iv_right);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        if (nudge){
            mHolder.toggleButton.setVisibility(View.VISIBLE);
            mHolder.arrowView.setVisibility(View.GONE);
        }else{
            mHolder.toggleButton.setVisibility(View.GONE);
            mHolder.arrowView.setVisibility(View.VISIBLE);
        }

        if (hash) {
            HashMap<String, String> deets = (HashMap<String, String>) friends.get(position);

            if(FriendActivity.pendingNudges.containsKey(deets.get("fb_id"))){
                mHolder.toggleButton.setBackgroundColor(mContext.getResources().getColor(R.color.green));
                mHolder.toggleButton.setText("X");
            }else{
                mHolder.toggleButton.setBackgroundColor(mContext.getResources().getColor(R.color.dblue));
                mHolder.toggleButton.setText("");
            }

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
            if (!hash && !nudge) {
                convertView = inflater.inflate(R.layout.item_sticky_header, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.tv_header);
                holder.date = (TextView) convertView.findViewById(R.id.tv_date);
                holder.number = (TextView) convertView.findViewById(R.id.tv_number);
            }else{
                convertView = inflater.inflate(R.layout.item_invisible_header, parent, false);
            }
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (!hash && !nudge) {
            ArrayList<String> deets = (ArrayList<String>) friends.get(position);
            if (deets.get(2).equals("1")){
                holder.text.setText("Friends");
            }else{
                holder.text.setText("Others");
            }
            holder.date.setVisibility(View.GONE);
            holder.number.setVisibility(View.GONE);
        }

//        convertView.setVisibility((false) ? View.GONE : View.VISIBLE);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        if (!hash && !nudge){
            ArrayList<String> deets = (ArrayList<String>)friends.get(position);
            return Integer.parseInt(deets.get(2));
        }else{
            return 0;
        }
    }

    private class HeaderViewHolder {
        TextView text;
        TextView date;
        TextView number;
    }

    private class ViewHolder {
        private TextView textView;
        private ImageView imageView;
        private TextView toggleButton;
        private ImageView arrowView;
    }
}