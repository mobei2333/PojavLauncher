package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.customcontrols.ControlData;
import net.kdt.pojavlaunch.customcontrols.buttons.ControlDrawer;

public class AddSubButton extends FloatAroundButton{
    public AddSubButton(Context context) {super(context);}
    public AddSubButton(Context context, @Nullable AttributeSet attrs) {super(context, attrs);}

    @Override
    protected void init() {
        int size = getResources().getDimensionPixelOffset(R.dimen._36sdp);
        setLayoutParams(new ViewGroup.LayoutParams(size, size));

        setText("Add Sub Button");
    }

    @Override
    public void setFollowedView(View view) {
        if(view instanceof ControlDrawer)
            super.setFollowedView(view);
        else hide();
    }

    @Override
    public void onClick(View v) {
        if(getFollowedView() instanceof ControlDrawer){
            ((ControlDrawer)getFollowedView()).getControlLayoutParent().addSubButton(
                    (ControlDrawer)getFollowedView(),
                    new ControlData()
            );
        }
    }


}
