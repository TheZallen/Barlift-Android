package com.barliftapp.barlift;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;

public class FriendAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Object> friends;

    public FriendAdapter(Context c, ArrayList<Object> friends) {
        this.friends = friends;
        mContext = c;
    }

    public int getCount() {
        return friends.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_item, null);
            ProfilePictureView imageView = (ProfilePictureView) grid.findViewById(R.id.ppv_friend);
            TextView textView = (TextView) grid.findViewById(R.id.tv_friend);
            ArrayList<String> friend_detail = (ArrayList<String>)friends.get(position);
            imageView.setProfileId(friend_detail.get(1));
            textView.setText(friend_detail.get(0));
        } else {
            grid = (View) convertView;
        }
        return grid;
    }
}