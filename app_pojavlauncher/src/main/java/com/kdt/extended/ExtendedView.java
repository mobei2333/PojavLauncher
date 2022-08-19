package com.kdt.extended;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;

public interface ExtendedView {

    /** @return The extended view data */
    ExtendedViewData getExtendedViewData();

    /** @return The compounds drawables (start, top, end, bottom) */
    Drawable[] getCompoundsDrawables();
    void setCompoundsDrawables(Drawable[] drawables);

    /** Initialize stuff specific to this view */
    default void initExtendedView(@NonNull Context context, @Nullable AttributeSet set){
        getAttributes(context, set);
        postProcessDrawables();
    }

    /** Get extended view attribute */
    default void getAttributes(@NonNull Context context, @Nullable AttributeSet set){
        if(set == null) return;
        TypedArray values = context.getTheme().obtainStyledAttributes(set, R.styleable.ExtendedView, 0, 0);
        try {
            boolean[] integerCompounds = new boolean[]{
                values.getBoolean(R.styleable.ExtendedView_drawableStartIntegerScaling, false),
                values.getBoolean(R.styleable.ExtendedView_drawableTopIntegerScaling, false),
                values.getBoolean(R.styleable.ExtendedView_drawableEndIntegerScaling, false),
                values.getBoolean(R.styleable.ExtendedView_drawableBottomIntegerScaling, false)
            };
            getExtendedViewData().setIntegerCompounds(integerCompounds);

            int[] squareBounds = new int[]{
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableStartSize, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableTopSize, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableEndSize, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableBottomSize, -1)
            };
            getExtendedViewData().setSizeCompounds(squareBounds);

            int[] padding = new int[]{
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableStartPadding, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableTopPadding, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableEndPadding, -1),
                values.getDimensionPixelSize(R.styleable.ExtendedView_drawableBottomPadding, -1)
            };
            getExtendedViewData().setPaddingCompounds(padding);
        }finally {
            values.recycle();
        }
    }

    default void postProcessDrawables(){
        if(getExtendedViewData() == null) return;
        makeDrawablesIntegerScaled();
        scaleDrawablesToDesiredSize();
    }

    default void scaleDrawablesToDesiredSize(){
        Drawable[] drawables = getCompoundsDrawables();
        int[] sizeCompounds = getExtendedViewData().getSizeCompounds();
        int index = -1;
        boolean shouldUpdate = false;
        Rect bounds = new Rect();

        for(Drawable drawable : drawables){
            index++;
            if(sizeCompounds[index] == -1 || drawable == null) continue;
            drawable.copyBounds(bounds);

            if(bounds.right != sizeCompounds[index] && bounds.bottom != sizeCompounds[index]){
                // Failsafe to avoid division by 0
                bounds.right = Math.max(bounds.right, 1);
                bounds.bottom = Math.max(bounds.bottom, 1);

                if(bounds.right == bounds.bottom){
                    bounds.bottom = sizeCompounds[index];
                    bounds.right = sizeCompounds[index];
                }else{
                    // Compute new size, respecting the aspect ratio
                    if(bounds.right > bounds.bottom){
                        bounds.bottom = bounds.bottom * sizeCompounds[index] / bounds.right;
                        bounds.right = sizeCompounds[index];
                    }else {
                        bounds.right = bounds.right * sizeCompounds[index] / bounds.bottom;
                        bounds.bottom = sizeCompounds[index];
                    }
                }

                drawable.setBounds(0,0, bounds.right, bounds.bottom);
                shouldUpdate = true;
            }
        }
        if(shouldUpdate)
            setCompoundsDrawables(drawables);
    }

    /** Makes all the compound drawables scaled with NN */
    default void makeDrawablesIntegerScaled(){
        int index = 0;
        boolean[] integerCompounds = getExtendedViewData().getIntegerCompounds();
        for(Drawable compoundDrawable : getCompoundsDrawables()){
            if(integerCompounds[index]) makeDrawableIntegerScaled(compoundDrawable);
            index++;
        }
    }

    /** Make a single drawable scaled with NN */
    default void makeDrawableIntegerScaled(Drawable drawable){
        if(drawable == null) return;
        drawable.setDither(false);
        drawable.setFilterBitmap(false);
    }
}
