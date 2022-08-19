package com.kdt.extended;

import android.view.View;

/**
 * Class intended to store any data specific to the (default) ExtendedView implementation
 * it enables automatic invalidation and redundant boilerplate.
 */
public class ExtendedViewData {
    /* Whether the compounds drawable are scaled via NN */
    private boolean[] mIntegerCompounds = new boolean[]{false, false, false, false};
    /* The square size of the compounds drawables */
    private int[] mSizeCompounds = new int[]{-1, -1, -1, -1};
    /* The padding of the compounds drawables */
    private int[] mPaddingCompounds = new int[]{0, 0, 0, 0};
    /* The view holding the data */
    private final View mView;

    public ExtendedViewData(View view){
        mView = view;
    }

    /** @return Whether compounds drawables are NN scaled */
    public boolean[] getIntegerCompounds() {
        return mIntegerCompounds;
    }

    public void setIntegerCompounds(boolean[] mIntegerCompounds) {
        this.mIntegerCompounds = mIntegerCompounds;
        mView.invalidate();
    }

    public void setIntegerCompound(int i, boolean integer){
        mIntegerCompounds[i] = integer;
        mView.invalidate();
    }

    /** @return The square size of the compounds drawables */
    public int[] getSizeCompounds() {
        return mSizeCompounds;
    }

    public void setSizeCompounds(int[] mSizeCompounds) {
        this.mSizeCompounds = mSizeCompounds;
        mView.invalidate();
    }

    public void setSizeCompound(int i, int size){
        mSizeCompounds[i] = size;
        mView.invalidate();
    }

    /** @return The padding of the compounds drawables */
    public int[] getPaddingCompounds() {
        return mPaddingCompounds;
    }

    public void setPaddingCompounds(int[] mPaddingCompounds) {
        this.mPaddingCompounds = mPaddingCompounds;
        mView.invalidate();
    }

    public void setPaddingCompound(int i, int padding){
        mPaddingCompounds[i] = padding;
        mView.invalidate();
    }
}
