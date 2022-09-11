package net.kdt.pojavlaunch;

import static android.os.Build.VERSION_CODES.P;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.kdt.mcgui.ProgressLayout;
import com.kdt.mcgui.mcAccountSpinner;

import net.kdt.pojavlaunch.fragments.LocalLoginFragment;
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.extra.ExtraListener;
import net.kdt.pojavlaunch.fragments.MojangLoginFragment;
import net.kdt.pojavlaunch.fragments.SelectAuthFragment;
import net.kdt.pojavlaunch.multirt.MultiRTConfigDialog;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.prefs.screens.settings.LauncherPreferenceFragment;
import net.kdt.pojavlaunch.tasks.AsyncAssetManager;
import net.kdt.pojavlaunch.tasks.AsyncMinecraftDownloader;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.util.List;

public class LauncherActivity extends BaseActivity {
    public static final String SETTING_FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    private final int REQUEST_STORAGE_REQUEST_CODE = 1;
    private final Object mLockStoragePerm = new Object();


    private mcAccountSpinner mAccountSpinner;
    private FragmentContainerView mFragmentView;
    private ImageButton mSettingsButton, mDeleteAccountButton;
    private ProgressLayout mProgressLayout;


    /* Listener for the back button in settings */
    private final ExtraListener<String> mBackPreferenceListener = (key, value) -> {
        if(value.equals("true")) onBackPressed();
        return false;
    };

    /* Listener for the auth method selection screen */
    private final ExtraListener<Boolean> mSelectAuthMethod = (key, value) -> {
        if(isFragmentVisible(SelectAuthFragment.TAG)
                || isFragmentVisible(LocalLoginFragment.TAG)
                || isFragmentVisible(MicrosoftLoginFragment.TAG)
                || isFragmentVisible(MojangLoginFragment.TAG)
        ) return false;
        Tools.swapFragment(this, SelectAuthFragment.class, SelectAuthFragment.TAG, true, null);
        return false;
    };

    /* Listener for the settings fragment */
    private final View.OnClickListener mSettingButtonListener = v -> {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if(fragments.get(fragments.size() - 1) instanceof LauncherPreferenceFragment) return;

        Tools.swapFragment(this, LauncherPreferenceFragment.class, SETTING_FRAGMENT_TAG, true, null);
    };

    /* Listener for account deletion */
    private final View.OnClickListener mAccountDeleteButtonListener = v -> {
        new AlertDialog.Builder(this)
                .setMessage(R.string.warning_remove_account)
                .setNeutralButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.global_delete, (dialog, which) -> mAccountSpinner.removeCurrentAccount())
                .show();
    };

    private final ExtraListener<Boolean> mLaunchGameListener = (key, value) -> {
        if(mProgressLayout.hasProcesses()){
            Toast.makeText(this, "Tasks are in progress, please wait", Toast.LENGTH_LONG).show();
            return false;
        }

        String selectedProfile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE,"");
        if (LauncherProfiles.mainProfileJson == null  || LauncherProfiles.mainProfileJson.profiles == null
                || !LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile)){
            Toast.makeText(this, "No selected version", Toast.LENGTH_LONG).show();
            return false;
        }
        MinecraftProfile prof = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
        if (prof == null || prof.lastVersionId == null){
            Toast.makeText(this, "No selected version", Toast.LENGTH_LONG).show();
            return false;
        }

        if(mAccountSpinner.getSelectedAccount() == null){
            Toast.makeText(this, "No selected minecraft account", Toast.LENGTH_LONG).show();
            return false;
        }

        new AsyncMinecraftDownloader(this, AsyncMinecraftDownloader.findVersion(prof.lastVersionId), () -> runOnUiThread(() -> {
            try {
                Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                startActivity(mainIntent);
                finish();
                Log.i("ActCheck","mainActivity finishing=" + isFinishing() + ", destroyed=" + isDestroyed());
            } catch (Throwable e) {
                Tools.showError(getBaseContext(), e);
            }
        }));
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);
        getWindow().setBackgroundDrawable(null);
        bindViews();

        askForStoragePermission(); // Will wait here

        mSettingsButton.setOnClickListener(mSettingButtonListener);
        mDeleteAccountButton.setOnClickListener(mAccountDeleteButtonListener);
        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);

        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);

        AsyncAssetManager manager = new AsyncAssetManager();
        manager.unpackRuntime(this.getAssets(), false);
        manager.unpackComponents(this);
        manager.unpackSingleFiles(this);

        new AsyncVersionList().getVersionList(versions -> {
            ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions);
        });

        mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;
        if(requestCode == Tools.RUN_MOD_INSTALLER && data != null){
            Tools.launchModInstaller(this, data);
            return;
        }
        if(requestCode == MultiRTConfigDialog.MULTIRT_PICK_RUNTIME && data != null){
            Tools.installRuntimeFromUri(this, data.getData());
        }
    }

    /** Custom implementation to feel more natural when a backstack isn't present */
    @Override
    public void onBackPressed() {
        if(isFragmentVisible(MicrosoftLoginFragment.TAG)){
            MicrosoftLoginFragment fragment = (MicrosoftLoginFragment) getSupportFragmentManager().findFragmentByTag(MicrosoftLoginFragment.TAG);
            if(fragment.canGoBack()){
                fragment.goBack();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public void onAttachedToWindow() {
        if (Build.VERSION.SDK_INT < P) return;

        try {
            Rect notchRect = getWindow().getDecorView().getRootWindowInsets().getDisplayCutout().getBoundingRects().get(0);
            // Math min is to handle all rotations
            LauncherPreferences.PREF_NOTCH_SIZE = Math.min(notchRect.width(), notchRect.height());
        }catch (Exception e){
            Log.i("NOTCH DETECTION", "No notch detected, or the device if in split screen mode");
            LauncherPreferences.PREF_NOTCH_SIZE = -1;
        }
        Tools.updateWindowSize(this);
    }

    private boolean isFragmentVisible(String tag){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isVisible();
    }

    private boolean isFragmentVisible(int id){
        Fragment fragment = getSupportFragmentManager().findFragmentById(id);
        return fragment != null && fragment.isVisible();
    }

    private void askForStoragePermission(){
        int revokeCount = 0;
        while (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29 && !isStorageAllowed()) { //Do not ask for storage at all on Android 10+
            try {
                revokeCount++;
                if (revokeCount >= 3) {
                    Toast.makeText(this, R.string.toast_permission_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
                requestStoragePermission();

                synchronized (mLockStoragePerm) {
                    mLockStoragePerm.wait();
                }
            } catch (InterruptedException e) {
                Log.e("LauncherActivity", e.toString());
            }
        }
    }

    private boolean isStorageAllowed() {
        //Getting the permission status
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        //If permission is granted returning true
        return result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_STORAGE_REQUEST_CODE){
            synchronized (mLockStoragePerm) {
                mLockStoragePerm.notifyAll();
            }
        }
    }

    /** Stuff all the view boilerplate here */
    private void bindViews(){
        mFragmentView = findViewById(R.id.container_fragment);
        mSettingsButton = findViewById(R.id.setting_button);
        mDeleteAccountButton = findViewById(R.id.delete_account_button);
        mAccountSpinner = findViewById(R.id.account_spinner);
        mProgressLayout = findViewById(R.id.progress_layout);
    }




}
