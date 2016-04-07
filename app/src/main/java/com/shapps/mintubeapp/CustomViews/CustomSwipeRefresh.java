package com.shapps.mintubeapp.CustomViews;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Created by shyam on 4/4/16.
 */
public class CustomSwipeRefresh extends SwipeRefreshLayout {

    private CanChildScrollUpCallback mCanChildScrollUpCallback;

    public CustomSwipeRefresh(Context context) {
        super(context);
    }
    public CustomSwipeRefresh(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public interface CanChildScrollUpCallback {
        boolean canSwipeRefreshChildScrollUp();
    }

    public void setCanChildScrollUpCallback(CanChildScrollUpCallback canChildScrollUpCallback) {
        mCanChildScrollUpCallback = canChildScrollUpCallback;
    }

    @Override
    public boolean canChildScrollUp() {
        if (mCanChildScrollUpCallback != null) {
            return mCanChildScrollUpCallback.canSwipeRefreshChildScrollUp();
        }
        return super.canChildScrollUp();
    }
}