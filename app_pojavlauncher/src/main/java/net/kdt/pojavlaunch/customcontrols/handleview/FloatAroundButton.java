package net.kdt.pojavlaunch.customcontrols.handleview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;

/**
 * Button floating around another view.
 * The style of this view is not defined and should be handheld by children of this class
 */
@SuppressLint("AppCompatCustomView")
public abstract class FloatAroundButton extends TextView implements View.OnClickListener {
    public FloatAroundButton(Context context) {
        super(context);
        initAttributes();
    }
    public FloatAroundButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttributes();
    }

    private void initAttributes(){
        setOnClickListener(this);
        setAllCaps(true);
        setGravity(Gravity.CENTER);
        setTextColor(Color.BLACK);
        setBackgroundColor(Color.WHITE);
        setClipToOutline(true);
        setTextSize(getResources().getDimensionPixelOffset(R.dimen._3ssp));
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 9999);
            }
        });
        setElevation(2);
        setVisibility(GONE);
        init();
    }


    public static int SIDE_LEFT = 0x0;
    public static int SIDE_TOP = 0x1;
    public static int SIDE_RIGHT = 0x2;
    public static int SIDE_BOTTOM = 0x3;
    public static int SIDE_AUTO = 0x4;

    private View mFollowedView;
    private int mSide = SIDE_AUTO;
    private float mSideOffset = getResources().getDimensionPixelOffset(R.dimen._4sdp), mAxisOffset = 0;
    public final ViewTreeObserver.OnPreDrawListener mFollowedViewListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            if(mFollowedView == null || !mFollowedView.isShown()){
                hide();
                return true;
            }

            setNewPosition();
            return true;
        }
    };


    protected void init(){
        setVisibility(GONE);
        setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        setBackgroundColor(Color.RED);
    }

    private int pickSide(){
        if(mSide != SIDE_AUTO) return mSide;
        //TODO improve the "algo"
        ViewGroup parent = ((ViewGroup) mFollowedView.getParent());

        int side = mFollowedView.getX() + getWidth()/2f > parent.getWidth()/2f
                ? SIDE_LEFT
                : SIDE_RIGHT;

        float futurePos = getYPosition(side);
        if(futurePos + getHeight() > (parent.getHeight() + getHeight()/2)){
            side = SIDE_TOP;
        }else if (futurePos < -getHeight()/2){
            side = SIDE_BOTTOM;
        }

        return side;
    }

    private void setNewPosition(){
        if(mFollowedView == null) return;
        int side = pickSide();

        setX(getXPosition(side));
        setY(getYPosition(side));
    }

    private float getXPosition(int side){
        if(side == SIDE_LEFT){
            return mFollowedView.getX() - getWidth() - mSideOffset;
        }else if(side == SIDE_RIGHT){
            return mFollowedView.getX() + mFollowedView.getWidth() + mSideOffset;
        }else{
            if(side == SIDE_TOP)
                return mFollowedView.getX() + mFollowedView.getWidth()/2f - getWidth()/2f + mAxisOffset;
            else
                return mFollowedView.getX() + mFollowedView.getWidth()/2f - getWidth()/2f - mAxisOffset;
        }
    }

    private float getYPosition(int side){
        if(side == SIDE_TOP){
            return mFollowedView.getY() - getHeight() - mSideOffset;
        } else if(side == SIDE_BOTTOM){
            return mFollowedView.getY() + mFollowedView.getHeight() + mSideOffset;
        }else{
            if(side == SIDE_RIGHT){
                return mFollowedView.getY() + mFollowedView.getHeight()/2f - getHeight()/2f - mAxisOffset;
            }else{
                return mFollowedView.getY() + mFollowedView.getHeight()/2f - getHeight()/2f + mAxisOffset;
            }
        }
    }

    public final void setOffset(float sideOffset, float axisOffset){
        mSideOffset = sideOffset;
        mAxisOffset = axisOffset;
        setNewPosition();
    }

    public final void setSideOffset(float sideOffset){
        mSideOffset = sideOffset;
        setNewPosition();
    }

    public final void setAxisOffset(float axisOffset){
        mAxisOffset = axisOffset;
        setNewPosition();
    }

    public void setFollowedView(View view){
        if(mFollowedView != null)
            mFollowedView.getViewTreeObserver().removeOnPreDrawListener(mFollowedViewListener);

        setVisibility(VISIBLE);
        mFollowedView = view;
        if(mFollowedView != null)
            mFollowedView.getViewTreeObserver().addOnPreDrawListener(mFollowedViewListener);
    }


    public View getFollowedView(){
        return mFollowedView;
    }

    public void setSide(int side){
        mSide = side;
    }


    public void hide(){
        if(mFollowedView != null)
            mFollowedView.getViewTreeObserver().removeOnPreDrawListener(mFollowedViewListener);
        setVisibility(GONE);
    }
}
