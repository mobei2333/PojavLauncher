package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlButton;

public class CloneButton extends FloatAroundButton {
    public CloneButton(Context context) {super(context);}
    public CloneButton(Context context, @Nullable AttributeSet attrs) {super(context, attrs);}

    @Override
    protected void init() {
        int size = getResources().getDimensionPixelOffset(R.dimen._36sdp);
        setLayoutParams(new ViewGroup.LayoutParams(size, size));


        setText("CLONE");
    }

    private ControlButton mCurrentlySelectedButton = null;

    public void setFollowedView(View view) {
        super.setFollowedView(view);
        mCurrentlySelectedButton = (ControlButton) view;
    }


    @Override
    public void onClick(View v) {
        if(mCurrentlySelectedButton == null) return;

        mCurrentlySelectedButton.cloneButton();
        ((ControlLayout) mCurrentlySelectedButton.getParent()).removeEditWindow();
    }
}
