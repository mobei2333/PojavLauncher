package net.kdt.pojavlaunch.prefs.screens.settings;

import android.os.Bundle;

import net.kdt.pojavlaunch.R;

public class LauncherPreferenceExperimentalFragment extends LauncherPreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle b, String str) {
        addPreferencesFromResource(R.xml.pref_experimental);
    }
}
