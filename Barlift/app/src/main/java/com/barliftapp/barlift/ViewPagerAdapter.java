package com.barliftapp.barlift;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewPagerAdapter extends PagerAdapter{
    // Declare Variables
    Context context;
    String[] aboutText;
    int[] pic;
    LayoutInflater inflater;

    public ViewPagerAdapter(Context context, String[] aboutText, int[] pic) {
        this.context = context;
        this.aboutText = aboutText;
        this.pic = pic;
    }

    @Override
    public int getCount() {
        return aboutText.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        // Declare Variables
        TextView txtabout;
        ImageView imgpic;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        // Locate the TextViews in viewpager_item.xml
        txtabout = (TextView) itemView.findViewById(R.id.aboutlabel);


        // Capture position and set to the TextViews
        txtabout.setText(aboutText[position]);

        // Locate the ImageView in viewpager_item.xml
        imgpic = (ImageView) itemView.findViewById(R.id.pic);
        // Capture position and set to the ImageView
        imgpic.setImageResource(pic[position]);

        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}
