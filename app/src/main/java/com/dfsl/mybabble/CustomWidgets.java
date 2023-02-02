package com.dfsl.mybabble;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.CheckedTextView;
import android.widget.TextView;

public class CustomWidgets {

    public static TextView SimpleTextView (Context context, int fontSize, String text)
    {
        TextView newView = new TextView(context);
        newView.setText(text);
        newView.setGravity(Gravity.CENTER);
        newView.setTextSize(fontSize);
        newView.setTextColor(Color.BLACK);
        newView.setTypeface(Fonts.SetTypeFace(context, R.font.cera_pro_b));
        return newView;
    }

    public static TextView SimpleTextView (Context context, int fontSize, String text, int textColor)
    {
        TextView newView = new TextView(context);
        newView.setText(text);
        newView.setGravity(Gravity.CENTER);
        newView.setTextSize(fontSize);
        newView.setTextColor(textColor);
        newView.setTypeface(Fonts.SetTypeFace(context, R.font.cera_pro_b));
        return newView;
    }

    public static TextView SimpleTextView (Context context, int fontSize, String text, int textColor, int gravity)
    {
        TextView newView = new TextView(context);
        newView.setText(text);
        newView.setGravity(gravity);
        newView.setTextSize(fontSize);
        newView.setTextColor(textColor);
        newView.setTypeface(Fonts.SetTypeFace(context, R.font.cera_pro_b));
        return newView;
    }


}
