package com.simple.sd.sharkfeed.view.recycler;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

public class CustomLayoutManager extends GridLayoutManager {

    private boolean isScrollable = true;

    public CustomLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public CustomLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }
    public void setScrollable(boolean isScrollable){
        this.isScrollable = isScrollable;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollable && super.canScrollVertically();
    }
}
