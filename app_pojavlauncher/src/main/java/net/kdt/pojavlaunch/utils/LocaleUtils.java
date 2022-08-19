package net.kdt.pojavlaunch.utils;


import android.content.*;
import android.content.res.*;
import android.os.Build;

import androidx.preference.*;
import java.util.*;
import net.kdt.pojavlaunch.prefs.*;

public class LocaleUtils {

    public static Locale getLocale(){
        return Locale.getDefault();
    }
    
    public static Context setLocale(Context context) {
        if (LauncherPreferences.DEFAULT_PREF == null) {
            LauncherPreferences.DEFAULT_PREF = PreferenceManager.getDefaultSharedPreferences(context);
            LauncherPreferences.loadPreferences(context);
        }

        if(LauncherPreferences.PREF_FORCE_ENGLISH) {
            Locale CURRENT_LOCALE = Locale.ENGLISH;

            Locale.setDefault(CURRENT_LOCALE);

            Resources res = context.getResources();
            Configuration config = res.getConfiguration();

            if (Build.VERSION.SDK_INT >= 24) {
                config.setLocale(CURRENT_LOCALE);
                context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
            } else {
                config.locale = CURRENT_LOCALE;
                context.getApplicationContext().createConfigurationContext(config);
            }
        }

        return context;
    }
}
