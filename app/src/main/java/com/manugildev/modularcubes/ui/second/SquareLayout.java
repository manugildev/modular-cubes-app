package com.manugildev.modularcubes.ui.second;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class SquareLayaout extends CardView {


    public SquareLayaout(Context context) {
        super(context);
    }

    public SquareLayaout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLayaout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
