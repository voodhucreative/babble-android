package com.dfsl.mybabble;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import static java.security.AccessController.getContext;

public class BackgroundManager {

    public static RelativeLayout homeBackgroundView(Context context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        RelativeLayout rl = new RelativeLayout(context);

        RelativeLayout.LayoutParams tLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tLay.addRule(RelativeLayout.ALIGN_PARENT_TOP|RelativeLayout.CENTER_HORIZONTAL);
        tLay.setMargins(72,40,72,0);

        RelativeLayout.LayoutParams bLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bLay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //bLay.setMargins(0, 800,0,0);

        ImageView title = new ImageView(context);
        title.setImageResource(R.drawable.mybabblelogo4);
        title.setLayoutParams(tLay);

        title.setAdjustViewBounds(true);

        ImageView footer = new ImageView(context);
        footer.setImageResource(R.drawable.ic_homefooter2);
        footer.setLayoutParams(bLay);
        footer.setScaleType(ImageView.ScaleType.FIT_XY);
//        footer.setAdjustViewBounds(true);
        if(displayMetrics.widthPixels > 1500) {
            title.setMinimumHeight((int) (displayMetrics.heightPixels * 0.35));
            footer.setMinimumHeight((int) (displayMetrics.heightPixels * 0.35));
        }

        rl.addView(title);
        rl.addView(footer);


        return rl;
    }

    public static RelativeLayout landingBackgroundView(Context context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        RelativeLayout rl = new RelativeLayout(context);

        RelativeLayout.LayoutParams tLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tLay.addRule(RelativeLayout.ALIGN_PARENT_TOP|RelativeLayout.CENTER_HORIZONTAL);
        tLay.setMargins(0,0,0,0);

        RelativeLayout.LayoutParams bLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bLay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //bLay.setMargins(0, 800,0,0);

        ImageView title = new ImageView(context);
        title.setImageResource(R.drawable.ic_homeheader2);
        title.setLayoutParams(tLay);
        title.setScaleType(ImageView.ScaleType.FIT_XY);
//        title.setAdjustViewBounds(true);
        if(displayMetrics.widthPixels > 1500) {
            title.setMinimumHeight((int) (displayMetrics.heightPixels * 0.35));
        }

        ImageView footer = new ImageView(context);
        footer.setImageResource(R.drawable.ic_homefooter2);
        footer.setLayoutParams(bLay);
        footer.setScaleType(ImageView.ScaleType.FIT_XY);
//        footer.setAdjustViewBounds(true);
        if(displayMetrics.widthPixels > 1500) {
            footer.setMinimumHeight((int) (displayMetrics.heightPixels * 0.40));
        }
        rl.addView(title);
        rl.addView(footer);

        return rl;
    }

    public static RelativeLayout callBackgroundView(Context context)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);

        RelativeLayout rl = new RelativeLayout(context);

        RelativeLayout.LayoutParams tLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tLay.addRule(RelativeLayout.ALIGN_PARENT_TOP|RelativeLayout.CENTER_HORIZONTAL);
        tLay.setMargins(0,0,0,0);

        RelativeLayout.LayoutParams bLay = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bLay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //bLay.setMargins(0, 800,0,0);

        ImageView title = new ImageView(context);
        //title.setImageResource(R.drawable.ic_babble_logo_speech);
        title.setImageResource(R.drawable.mybabblelogo3);//
        title.setLayoutParams(tLay);
        title.setAdjustViewBounds(true);
        title.setScaleType(ImageView.ScaleType.FIT_XY);

        ImageView footer = new ImageView(context);
        footer.setImageResource(R.drawable.ic_bfooter);
        footer.setLayoutParams(bLay);
        footer.setScaleType(ImageView.ScaleType.FIT_XY);
//        footer.setAdjustViewBounds(true);
        if(displayMetrics.widthPixels > 1500) {
            title.setMinimumHeight((int) (displayMetrics.heightPixels * 0.45));
            footer.setMinimumHeight((int) (displayMetrics.heightPixels * 0.3));
        }

        rl.addView(title);
        rl.addView(footer);

        return rl;
    }


}
