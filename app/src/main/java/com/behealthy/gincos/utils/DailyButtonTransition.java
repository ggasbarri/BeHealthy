package com.behealthy.gincos.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Shared element transition from {@link com.behealthy.gincos.fragments.MainFragment} to {@link com.behealthy.gincos.fragments.DailyRegistryFragment} and vice versa.
 */
@SuppressLint("NewApi")
public class DailyButtonTransition extends TransitionSet {

    public static final int MODE_ENTER = 1;
    public static final int MODE_EXIT = 2;

    public DailyButtonTransition(int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOrdering(ORDERING_TOGETHER);
            addTransition(new ChangeBounds());
            if (mode == MODE_ENTER) {
                setInterpolator(new DecelerateInterpolator());
                setDuration(400);
            }else{
                setInterpolator(new AccelerateInterpolator());
                setDuration(240);
            }
        }
    }
}