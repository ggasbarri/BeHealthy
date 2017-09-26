package com.behealthy.gincos.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * TextView implementing the BreeSerif font family.
 */
public class MyTextView extends android.support.v7.widget.AppCompatTextView {

    public MyTextView(Context context) {
        super(context);
        setTypeface(Typeface.createFromAsset(context.getAssets(),"BreeSerif-Regular.ttf"));
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Typeface.createFromAsset(context.getAssets(),"BreeSerif-Regular.ttf"));
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Typeface.createFromAsset(context.getAssets(),"BreeSerif-Regular.ttf"));
    }
}
