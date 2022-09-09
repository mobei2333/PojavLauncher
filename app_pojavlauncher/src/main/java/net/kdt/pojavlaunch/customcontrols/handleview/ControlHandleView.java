package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlButton;

public class ControlHandleView extends View {
    public ControlHandleView(Context context) {
        super(context);
        init();
    }

    public ControlHandleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private final Drawable mDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_view_handle, getContext().getTheme());
    private ControlButton mView;
    private float mXOffset, mYOffset;
    private final ViewTreeObserver.OnPreDrawListener mPositionListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if(mView == null || !mView.isShown()){
                hide();
                return true;
            }

            setX(mView.getX() + mView.getWidth());
            setY(mView.getY() + mView.getHeight());
            return true;
        }
    };

    private void init(){
        int size = getResources().getDimensionPixelOffset(R.dimen._22sdp);
        mDrawable.setBounds(0,0,size,size);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
        setLayoutParams(params);
        setBackground(mDrawable);
    }

    public void setControlButton(ControlButton controlButton){
        if(mView != null) mView.getViewTreeObserver().removeOnPreDrawListener(mPositionListener);

        setVisibility(VISIBLE);
        mView = controlButton;
        mView.getViewTreeObserver().addOnPreDrawListener(mPositionListener);

        setX(controlButton.getX() + controlButton.getWidth());
        setY(controlButton.getY() + controlButton.getHeight());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mXOffset = event.getX();
                mYOffset = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                setX(getX() + event.getX() - mXOffset);
                setY(getY() + event.getY() - mYOffset);

                System.out.println(getX() - mView.getX());
                System.out.println(getY() - mView.getY());


                mView.getProperties().setWidth(getX() - mView.getX());
                mView.getProperties().setHeight(getY() - mView.getY());
                mView.regenerateDynamicCoordinates();
                break;
        }

        return true;
    }

    public void hide(){
        if(mView != null)
            mView.getViewTreeObserver().removeOnPreDrawListener(mPositionListener);
        setVisibility(GONE);
    }
}
