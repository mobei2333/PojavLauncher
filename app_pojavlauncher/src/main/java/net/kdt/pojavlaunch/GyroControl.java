package net.kdt.pojavlaunch;

import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static android.view.Surface.ROTATION_270;
import static android.view.Surface.ROTATION_90;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;

import org.lwjgl.glfw.CallbackBridge;

import java.util.Arrays;

public class GyroControl implements SensorEventListener, GrabListener {
    /* How much distance has to be moved before taking into account the gyro */
    private static final float REALLY_LOW_PASS_THRESHOLD = 1.3F;


    private final SensorManager mSensorManager;
    private final Sensor mSensor;
    private boolean mShouldHandleEvents;
    private boolean mFirstPass;
    private float xFactor; // -1 or 1 depending on device orientation
    private float yFactor;
    private boolean mSwapXY;

    private final float[] mPreviousRotation = new float[16];
    private final float[] mCurrentRotation = new float[16];
    private final float[] mAngleDifference = new float[3];

    /* Store the gyro movement under the threshold */
    private float mStoredX = 0;
    private float mStoredY = 0;

    public GyroControl(Activity activity) {
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        updateOrientation(activity.getWindowManager().getDefaultDisplay().getRotation());
    }

    public void enable() {
        if(mSensor == null) return;
        mFirstPass = true;
        mSensorManager.registerListener(this, mSensor, 1000 * LauncherPreferences.PREF_GYRO_SAMPLE_RATE);

        mShouldHandleEvents = CallbackBridge.isGrabbing();
        CallbackBridge.addGrabListener(this);
    }

    public void disable() {
        if(mSensor == null) return;
        mSensorManager.unregisterListener(this);

        CallbackBridge.removeGrabListener(this);
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (!mShouldHandleEvents) return;
        // Copy the old array content
        System.arraycopy(mCurrentRotation, 0, mPreviousRotation, 0, 16);
        SensorManager.getRotationMatrixFromVector(mCurrentRotation, sensorEvent.values);

        if(mFirstPass){  // Setup initial position
            mFirstPass = false;
            return;
        }
        SensorManager.getAngleChange(mAngleDifference, mCurrentRotation, mPreviousRotation);

        mStoredX += mAngleDifference[1] * 1000;
        mStoredY += mAngleDifference[2] * 1000;

        if(Math.abs(mStoredX) + Math.abs(mStoredY) > REALLY_LOW_PASS_THRESHOLD){
            CallbackBridge.mouseX -= ((mSwapXY ? mStoredY : mStoredX)  * LauncherPreferences.PREF_GYRO_SENSITIVITY * xFactor);
            CallbackBridge.mouseY += ((mSwapXY ? mStoredX : mStoredY) * LauncherPreferences.PREF_GYRO_SENSITIVITY * yFactor);
            CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);

            mStoredX = 0;
            mStoredY = 0;
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        mFirstPass = true;
        mShouldHandleEvents = isGrabbing;
    }

    /** Update the axis mapping in accordance to activity rotation */
    public void updateOrientation(int rotation){
        switch (rotation){
            case ROTATION_0:
                mSwapXY = true;
                xFactor = 1;
                yFactor = 1;
                break;
            case ROTATION_90:
                mSwapXY = false;
                xFactor = -1;
                yFactor = 1;
                break;
            case ROTATION_180:
                mSwapXY = true;
                xFactor = -1;
                yFactor = -1;
                break;
            case ROTATION_270:
                mSwapXY = false;
                xFactor = 1;
                yFactor = -1;
                break;
        }

        if(LauncherPreferences.PREF_GYRO_INVERT_X) xFactor *= -1;
        if(LauncherPreferences.PREF_GYRO_INVERT_Y) yFactor *= -1;
    }


}
