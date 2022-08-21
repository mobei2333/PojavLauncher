package com.kdt.mcgui;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.extra.ExtraCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/** Class staring at specific values and automatically show something if the progress is present
 * Since progress is posted in a specific way, The packing/unpacking is handheld by the class
 *
 * This class relies on ExtraCore for its behavior.
 */
public class ProgressLayout extends ConstraintLayout implements View.OnClickListener {
    public ProgressLayout(@NonNull Context context) {
        super(context);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public ProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private final ArrayMap<String, TextProgressBar> mMap = new ArrayMap<>();
    private LinearLayout mLinearLayout;
    private TextView mTaskNumberDisplayer;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mCheckProgressRunnable = new Runnable() {
        @Override
        public void run() {
            for(String progressKey : mMap.keySet()){
                Object object = ExtraCore.consumeValue(progressKey);

                if(object != null){
                    String[] progressStuff = ((String) object).split("¤");
                    int progress = Integer.parseInt(progressStuff[0]);
                    int resourceString = Integer.parseInt(progressStuff[1]);

                    // Prepare the progressbar
                    if(mMap.get(progressKey) == null){
                        TextProgressBar textView = new TextProgressBar(getContext());
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen._20sdp));
                        params.bottomMargin = getResources().getDimensionPixelOffset(R.dimen._6sdp);
                        
                        mLinearLayout.addView(textView, params);
                        mMap.put(progressKey, textView);
                    }

                    mMap.get(progressKey).setProgress(progress);
                    if(resourceString != -1){
                        mMap.get(progressKey).setText(getResources().getString(resourceString, Arrays.copyOfRange(progressStuff, 2, progressStuff.length - 1)));
                    }else{
                        mMap.get(progressStuff[2]);
                    }


                    // Remove when we don't have progress
                    if(progress == 0){
                        mLinearLayout.removeView(mMap.get(progressKey));
                        mMap.remove(progressKey);
                    }
                }
            }

            setVisibility(mMap.size() == 0 ? GONE : VISIBLE);

            mTaskNumberDisplayer.setText(mMap.keySet().size() + " tasks in progress");
            mHandler.postDelayed(this, 1000);
        }
    };



    public void observe(String progressKey){
        mMap.put(progressKey, null);
    }

    private void init(){
        inflate(getContext(), R.layout.view_progress, this);
        mLinearLayout = findViewById(R.id.progress_linear_layout);
        mTaskNumberDisplayer = findViewById(R.id.progress_textview);
        mHandler.postDelayed(mCheckProgressRunnable, 3000);

        setBackgroundColor(getResources().getColor(R.color.background_bottom_bar));
        setVisibility(GONE);

        setOnClickListener(this);
    }

    public static void setProgress(String progressKey, int progress, @IntegerRes int resource, String... message){
        StringBuilder builder = new StringBuilder();
        for(String bit : message){
            builder.append(bit).append("¤");
        }

        ExtraCore.setValue(progressKey, progress + "¤" + resource + "¤" + builder);
    }

    public static void setProgress(String progressKey, int progress, String message){
        setProgress(progressKey,progress, -1, message);
    }

    @Override
    public void onClick(View v) {
        mLinearLayout.setVisibility(mLinearLayout.getVisibility() == GONE ? VISIBLE : GONE);
    }
}
