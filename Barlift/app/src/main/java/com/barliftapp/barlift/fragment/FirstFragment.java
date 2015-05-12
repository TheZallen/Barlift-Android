package com.barliftapp.barlift.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.barliftapp.barlift.R;
import com.squareup.picasso.Picasso;

public class FirstFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pager_video, container, false);

        Picasso.with(getActivity())
                .load(R.drawable.barlift_login_logo)
                .into((ImageView)v.findViewById(R.id.imageView));

//        YoYo.with(Techniques.FadeInDown)
//                .duration(1000)
//                .playOn(v.findViewById(R.id.imageView));
//
//        YoYo.with(Techniques.SlideInUp)
//                .duration(2000)
//                .playOn(v.findViewById(R.id.textView2));
//
//        YoYo.with(Techniques.SlideInUp)
//                .duration(3000)
//                .playOn(v.findViewById(R.id.textView3));

        return v;
    }

    public static FirstFragment newInstance() {

        FirstFragment f = new FirstFragment();
        Bundle b = new Bundle();
        f.setArguments(b);

        return f;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()){
//            if (!isVisibleToUser)
//                videoHolder.suspend();
//            else
//                videoHolder.resume();
        }
    }
}