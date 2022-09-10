package net.kdt.pojavlaunch.customcontrols.handleview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawerPullButton extends View {
    public DrawerPullButton(Context context) {super(context); init();}
    public DrawerPullButton(Context context, @Nullable AttributeSet attrs) {super(context, attrs); init();}

    private final Paint mPaint = new Paint();

    private void init(){
        setAlpha(0.33f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.BLACK);
        canvas.drawArc(0,-getHeight(),getWidth(), getHeight(), 0, 180, true, mPaint);

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth()/2f, getHeight()/2.1f, Math.min(getWidth(), getHeight()) / 6f, mPaint);
    }
}
