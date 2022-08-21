package com.kdt.extended;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.R;

import java.util.Locale;

public class ExtendedSpinner extends androidx.appcompat.widget.AppCompatSpinner implements ExtendedView {

    private final ExtendedViewData mExtendedViewData = new ExtendedViewData(this);
    private Drawable[] mDrawables;

    public ExtendedSpinner(@NonNull Context context) {
        super(context);
        initExtendedView(context, null);
    }

    public ExtendedSpinner(@NonNull Context context, int mode) {
        super(context, mode);
        initExtendedView(context, null);
    }

    public ExtendedSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initExtendedView(context, attrs);
    }

    public ExtendedSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initExtendedView(context, attrs);
    }

    public ExtendedSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
        initExtendedView(context, attrs);
    }

    public ExtendedSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
        initExtendedView(context, attrs);
    }




    @Override
    public void getAttributes(@NonNull Context context, @Nullable AttributeSet set) {
        ExtendedView.super.getAttributes(context, set);
        if(set == null) return;
        TypedArray array = context.getTheme().obtainStyledAttributes(set, R.styleable.ExtendedView, 0, 0);
        try {
            mDrawables = new Drawable[]{
                array.getDrawable(R.styleable.ExtendedSpinner_drawableStart)
            };
        }finally {
            array.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the whole view, regardless of the padding for now

        //TODO implement rtl support ?
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LAYOUT_DIRECTION_RTL;
        int[] paddings = mExtendedViewData.getPaddingCompounds();
        Drawable[] drawables = getCompoundsDrawables();
        float halfHeight = getHeight()/2f;
        Rect tmpRect = new Rect();
        // Start drawable

        if(drawables[0] != null){
            canvas.save();
            drawables[0].setBounds(0,0,64,64);
            drawables[0].copyBounds(tmpRect);
            canvas.translate(paddings[0], halfHeight - tmpRect.bottom/2f);
            canvas.restore();
        }




        //setPadding(15, 15, 15, 15);
        // TODO make the super drawing take into account the drawable size ?
        /*
        mDrawableEnd.setBounds(0,0, 64, 64);
        int translateWidth = getWidth() - mDrawableEnd.getBounds().right;
        canvas.save();
        canvas.translate(translateWidth, 0);
        mDrawableEnd.draw(canvas);
        canvas.restore();
        //canvas.translate(translateWidth, 0);
         */
    }

    @Override
    public ExtendedViewData getExtendedViewData() {
        return mExtendedViewData;
    }

    @Override
    public Drawable[] getCompoundsDrawables() {
        return mDrawables;
    }

    @Override
    public void setCompoundsDrawables(Drawable[] drawables) {
        mDrawables = drawables;
    }
}
