package com.barliftapp.barlift;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SecondFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.viewpager_item, container, false);

        TextView tv = (TextView) v.findViewById(R.id.aboutlabel);
        tv.setText(getArguments().getString("msg"));
        TextView tvTitle = (TextView) v.findViewById(R.id.titlelabel);
        tvTitle.setText(getArguments().getString("title"));

        ImageView iv = (ImageView) v.findViewById(R.id.pic);
        iv.setImageResource(getArguments().getInt("image"));

        return v;
    }

    public static SecondFragment newInstance(String title, String text, int draw) {

        SecondFragment f = new SecondFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        b.putString("title", title);
        b.putInt("image", draw);
        f.setArguments(b);

        return f;
    }
}