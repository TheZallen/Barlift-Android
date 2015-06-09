package com.barliftapp.barlift.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.barliftapp.barlift.R;
import com.squareup.picasso.Picasso;

/**
 * Created by Zak on 5/11/15.
 */
public class DealFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pager_deal, container, false);

        TextView tvTitle = (TextView) v.findViewById(R.id.deal_title);
        tvTitle.setText(getArguments().getString("title").replace("\\n", "\n"));

        return v;
    }

    public static DealFragment newInstance(String title) {

        DealFragment f = new DealFragment();
        Bundle b = new Bundle();
        b.putString("title", title);
        f.setArguments(b);

        return f;
    }
}
