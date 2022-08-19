package com.kdt.extended;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class ExtendedTextView extends AppCompatTextView implements ExtendedView {

    private final ExtendedViewData mExtendedViewData = new ExtendedViewData(this);

    public ExtendedTextView(@NonNull Context context) {
        super(context);
        initExtendedView(context, null);
    }

    public ExtendedTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initExtendedView(context,  attrs);
    }

    public ExtendedTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initExtendedView(context,  attrs);
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(left, top, right, bottom);
        postProcessDrawables();
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(left, top, right, bottom);
        postProcessDrawables();
    }

    @Override
    public void setCompoundDrawablesWithIntrinsicBounds(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        super.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        postProcessDrawables();
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        postProcessDrawables();
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        postProcessDrawables();
    }

    @Override
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(@Nullable Drawable start, @Nullable Drawable top, @Nullable Drawable end, @Nullable Drawable bottom) {
        super.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
        postProcessDrawables();
    }

    @Override
    public ExtendedViewData getExtendedViewData() {
        return mExtendedViewData;
    }

    @Override
    public Drawable[] getCompoundsDrawables() {
        return getCompoundDrawablesRelative();
    }

    @Override
    public void setCompoundsDrawables(Drawable[] drawables) {
        setCompoundDrawablesRelative(drawables[0], drawables[1], drawables[2], drawables[3]);
    }
}
