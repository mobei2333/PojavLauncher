package net.kdt.pojavlaunch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.kdt.mcgui.mcAccountSpinner;
import com.kdt.mcgui.mcVersionSpinner;

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

import java.io.File;
import java.util.List;

public class LauncherActivity extends BaseActivity {
    public static final String SETTING_FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    private mcVersionSpinner mVersionSpinner;
    private mcAccountSpinner mAccountSpinner;
    private FragmentContainerView mFragmentView;
    private ImageButton mSettingsButton, mDeleteAccountButton;
    private Button mPlayButton;


    /* Listener for the back button in settings */
    private final ExtraListener<String> mBackPreferenceListener = (key, value) -> {
        if(value.equals("true")) onBackPressed();
        return false;
    };


    private final ExtraListener<String> mRefreshVersion = (key, value) -> {
        mVersionSpinner.getProfileAdapter().notifyDataSetChanged();
        mVersionSpinner.setSelection(mVersionSpinner.getProfileAdapter().resolveProfileIndex(value));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);
        getWindow().setBackgroundDrawable(null);
        bindViews();

        mSettingsButton.setOnClickListener(mSettingButtonListener);
        mDeleteAccountButton.setOnClickListener(mAccountDeleteButtonListener);
        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.addExtraListener(ExtraConstants.REFRESH_VERSION_SPINNER, mRefreshVersion);

        AsyncAssetManager manager = new AsyncAssetManager();
        manager.unpackRuntime(this.getAssets(), false);
        manager.unpackComponents(this);
        manager.unpackSingleFiles(this);

        new AsyncVersionList().getVersionList(versions -> {
            ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions);
        });


        mPlayButton.setOnClickListener(v -> {
            String selectedProfile = LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE,"");
            if (LauncherProfiles.mainProfileJson == null
                    || LauncherProfiles.mainProfileJson.profiles == null
                    || !LauncherProfiles.mainProfileJson.profiles.containsKey(selectedProfile)) return;
            MinecraftProfile prof = LauncherProfiles.mainProfileJson.profiles.get(selectedProfile);
            if (prof == null || prof.lastVersionId == null) return;

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
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.BACK_PREFERENCE, mBackPreferenceListener);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethod);
        ExtraCore.removeExtraListenerFromValue(ExtraConstants.REFRESH_VERSION_SPINNER, mRefreshVersion);
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

    private boolean isFragmentVisible(String tag){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        return fragment != null && fragment.isVisible();
    }

    private boolean isFragmentVisible(int id){
        Fragment fragment = getSupportFragmentManager().findFragmentById(id);
        return fragment != null && fragment.isVisible();
    }

    /** Stuff all the view boilerplate here */
    private void bindViews(){
        mVersionSpinner = findViewById(R.id.mc_version_spinner);
        mFragmentView = findViewById(R.id.container_fragment);
        mSettingsButton = findViewById(R.id.setting_button);
        mDeleteAccountButton = findViewById(R.id.delete_account_button);
        mAccountSpinner = findViewById(R.id.account_spinner);
        mPlayButton = findViewById(R.id.play_button);
    }




}
