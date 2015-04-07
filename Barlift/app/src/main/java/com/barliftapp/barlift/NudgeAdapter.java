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

import java.util.ArrayList;
import java.util.HashMap;

public class NudgeAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Object> friends;

    public NudgeAdapter(Context c, ArrayList<Object> friends) {
        this.mContext = c;
        this.friends = friends;
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
            convertView = inflater.inflate(R.layout.list_item, null);
            mHolder = new ViewHolder();

            mHolder.textView=(TextView) convertView.findViewById(R.id.tv_item);
            mHolder.imageView=(ImageView) convertView.findViewById(R.id.iv_friend_item);


        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> deets = (HashMap<String, String>) friends.get(position);

        mHolder.textView.setText(deets.get("name"));
//            mHolder.imageView.setProfileId(deets.get("fb_id"));
        Picasso.with(mContext)
                .load("https://graph.facebook.com/" + deets.get("fb_id") + "/picture?type=normal&height=100&width=100")
                .transform(new CircleTransform())
                .into(mHolder.imageView);

        convertView.setTag(mHolder);

        return convertView;
    }

    private class ViewHolder {
        private TextView textView;
        private ImageView imageView;
    }
}