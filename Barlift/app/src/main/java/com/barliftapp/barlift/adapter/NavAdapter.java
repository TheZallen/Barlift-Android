package com.barliftapp.barlift.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.barliftapp.barlift.R;
import com.barliftapp.barlift.activity.FriendActivity;
import com.barliftapp.barlift.activity.LoginActivity;
import com.barliftapp.barlift.activity.MainActivity;
import com.barliftapp.barlift.activity.NudgeActivity;
import com.barliftapp.barlift.activity.ProfileActivity;
import com.parse.ParseUser;


public class NavAdapter extends RecyclerView.Adapter<NavAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
    // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;

    private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
    private int mIcons[];       // Int Array to store the passed icons resource value from MainActivity.java

    private String name;        //String Resource for header View Name
    private static Context mContext;


    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        int Holderid;

        TextView textView;
        ImageView imageView;
        TextView Name;


        public ViewHolder(View itemView,int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);


            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created
            itemView.setOnClickListener(this);
            if(ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText); // Creating TextView object with the id of textView from item_row.xml

                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);// Creating ImageView object with the id of ImageView from item_row.xml
                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            }
            else{


                Name = (TextView) itemView.findViewById(R.id.name);         // Creating Text View object from header.xml for name
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            }
        }

        @Override
        public void onClick(View v) {
            switch(getPosition()) {
                case 1:
                    mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                    break;
                case 2:
                    mContext.startActivity(new Intent(mContext, FriendActivity.class));
                    break;
                case 3:
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String shareBody = "Download BarLift! Go to http://www.barliftapp.com to get the app.";
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "BarLift");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                    mContext.startActivity(Intent.createChooser(sharingIntent, "Share deal via"));
                    break;
                case 4:
                    mContext.startActivity(new Intent(mContext, NudgeActivity.class));
                    break;
                case 5:
                    int x;
                    for (x = 0; x < MainActivity.mCommunities.length; x++){
                        if (MainActivity.mCommunities[x].equals(ParseUser.getCurrentUser().getString("community_name"))){break;}
                    }
                    new MaterialDialog.Builder(mContext)
                            .title("Choose Location")
                            .items(MainActivity.mCommunities)
                            .theme(Theme.LIGHT)
                            .itemsCallbackSingleChoice(x, new MaterialDialog.ListCallbackSingleChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                    ParseUser.getCurrentUser().put("community_name", MainActivity.mCommunities[which]);
                                    ParseUser.getCurrentUser().saveInBackground();
                                    return true;
                                }
                            })
                            .positiveText("Select")
                            .show();
                    break;
                case 6:
                    logout();
                    break;
                default:
                    break;
            }
        }

        public static void logout() {
            // Log the user out
            ParseUser.logOut();

            // Go to the login view
            startLoginActivity();
        }

        private static void startLoginActivity() {
            Intent intent = new Intent(mContext, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            ((Activity)mContext).finish();
        }
    }



    public NavAdapter(String Titles[],int Icons[],String Name, Context context){ // MyAdapter Constructor with titles and icons parameter
        // titles, icons, name, email, profile pic are passed from the main activity as we
        mNavTitles = Titles;                //have seen earlier
        mIcons = Icons;
        name = Name;
        mContext = context;
        //in adapter
    }



    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder

    @Override
    public NavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav,parent,false); //Inflating the layout

            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhItem; // Returning the created object

            //inflate your layout and pass it to view holder

        } else if (viewType == TYPE_HEADER) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header,parent,false); //Inflating the layout

            ViewHolder vhHeader = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view

            return vhHeader; //returning the object created


        }
        return null;

    }

    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(NavAdapter.ViewHolder holder, int position) {
        if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
            // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            holder.imageView.setImageResource(mIcons[position -1]);// Settimg the image with array of our icons
        }
        else{
            holder.Name.setText(name);
        }
    }

    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length+1; // the number of items in the list will be +1 the titles including the header view.
    }


    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }



    private boolean isPositionHeader(int position) {
        return position == 0;
    }

}
