package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * View pager used for a finite, low number of pages, where there is no need for
 * optimization.
 */

public class SmartViewPager extends ViewPager {
    /**
     * Initialize the view.
     *
     * @param context The application context.
     */
    public SmartViewPager(final Context context) {
        super(context);
    }

    /**
     * Initialize the view.
     *
     * @param context The application context.
     * @param attrs   The requested attributes.
     */
    public SmartViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean isPagingEnabled = true;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // Make sure all are loaded at once
        final int childrenCount = getChildCount();
        setOffscreenPageLimit(childrenCount - 1);

        // Attach the adapter
        setAdapter(new PagerAdapter() {


            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                return container.getChildAt(position);
            }

            @Override
            public boolean isViewFromObject(final View arg0, final Object arg1) {
                return arg0 == arg1;

            }

            @Override
            public int getCount() {
                return childrenCount;
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
            }
        });

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }
    public void setPagingEnabled(boolean b) {

        this.isPagingEnabled = b;
    }
}