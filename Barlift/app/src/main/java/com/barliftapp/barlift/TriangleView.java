package com.barliftapp.barlift;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class TriangleView extends View {

    private boolean diagBottom;
    private int widthCut;
    private String backColor;

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TriangleView, 0, 0);
        try {
            diagBottom = ta.getBoolean(R.styleable.TriangleView_diagBottom, false);
            widthCut = ta.getInt(R.styleable.TriangleView_widthCut, 0);
            backColor = ta.getString(R.styleable.TriangleView_backColor);
        } finally {
            ta.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int heightOfDiagonal = 20;
        Paint wallpaint = new Paint();
        wallpaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        Log.d("HEY", backColor);
        if (backColor.equals("orange")){
            wallpaint.setColor(getResources().getColor(R.color.orange));
        }else if (backColor.equals("white")){
            wallpaint.setColor(Color.WHITE);
        }else if (backColor.equals("gray")){
            wallpaint.setColor(getResources().getColor(R.color.grayback));
        }

        wallpaint.setStyle(Paint.Style.FILL);
        if (diagBottom){
            canvas.drawRect(widthCut ,0f,(float)canvas.getWidth() - widthCut, canvas.getHeight() - heightOfDiagonal, wallpaint);
        }else{
            canvas.drawRect(widthCut ,heightOfDiagonal,(float)canvas.getWidth() - widthCut, canvas.getHeight(), wallpaint);
        }

        Path wallpath = new Path();
        wallpath.reset(); // only needed when reusing this path for a new build
        if (diagBottom){
            wallpath.moveTo(0, canvas.getHeight()-heightOfDiagonal); // used for first point
            wallpath.lineTo(canvas.getWidth(), canvas.getHeight()-heightOfDiagonal);
            wallpath.lineTo(0, canvas.getHeight());
            wallpath.lineTo(0, canvas.getHeight()-heightOfDiagonal);
        }else{
            wallpath.moveTo(widthCut, heightOfDiagonal); // used for first point
            wallpath.lineTo(canvas.getWidth() - widthCut, 0);
            wallpath.lineTo(canvas.getWidth() - widthCut, heightOfDiagonal);
            wallpath.lineTo(widthCut, heightOfDiagonal);
        }


        canvas.drawPath(wallpath, wallpaint);
    }
}
