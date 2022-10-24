package com.dfsl.mybabble;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

public class Fonts {

    public static Typeface SetTypeFace(Context context, int fontResource){
        Typeface typeface = ResourcesCompat.getFont(context, fontResource);
        return typeface;
    }
}
