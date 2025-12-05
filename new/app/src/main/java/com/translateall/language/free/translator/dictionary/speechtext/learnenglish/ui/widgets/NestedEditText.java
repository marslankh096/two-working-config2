package com.translateall.language.free.translator.dictionary.speechtext.learnenglish.ui.widgets;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Keep;
import androidx.appcompat.widget.AppCompatEditText;

@Keep
public class NestedEditText extends AppCompatEditText {

    /* renamed from: a */
    public Layout layout;

    /* renamed from: b */
    public int paddingTop;

    /* renamed from: c */
    public int paddingBottom;

    /* renamed from: d */
    public int height;

    /* renamed from: e */
    public int layoutHeight;

    public NestedEditText(Context context) {
        super(context);
        init();
    }

    public NestedEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public NestedEditText(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    /* renamed from: a */
    private void init() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.layout = getLayout();
        this.layoutHeight = this.layout.getHeight();
        this.paddingTop = getTotalPaddingTop();
        this.paddingBottom = getTotalPaddingBottom();
        this.height = getHeight();
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        if (vert == ((this.layoutHeight + this.paddingTop) + this.paddingBottom) - this.height) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try{
            getParent().requestDisallowInterceptTouchEvent(true);
            return super.onTouchEvent(event);
        }
        catch (Exception e){
            return false;
        }


    }
}
