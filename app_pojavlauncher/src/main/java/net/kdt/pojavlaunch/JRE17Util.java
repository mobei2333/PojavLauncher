package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Architecture.archAsString;
import static net.kdt.pojavlaunch.Tools.NATIVE_LIB_DIR;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.Runtime;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import java.io.IOException;

public class JRE17Util {
    public static final String NEW_JRE_NAME = "Internal-17";
    public static boolean checkInternalNewJre(AssetManager assetManager, MultiRTUtils.RuntimeProgressReporter reporter) {
        String launcher_jre17_version;
        String installed_jre17_version = MultiRTUtils.__internal__readBinpackVersion(NEW_JRE_NAME);
        try {
            launcher_jre17_version = Tools.read(assetManager.open("components/jre-new/version"));
        }catch (IOException exc) {
            //we don't have a runtime included!
            return installed_jre17_version != null; //if we have one installed -> return true -> proceed (no updates but the current one should be functional)
            //if we don't -> return false -> Cannot find compatible Java runtime
        }
        if(!launcher_jre17_version.equals(installed_jre17_version))  // this implicitly checks for null, so it will unpack the runtime even if we don't have one installed
            return unpackJre17(assetManager, launcher_jre17_version, reporter);
        else return true;
    }
    
    private static boolean unpackJre17(AssetManager assetManager, String rt_version, MultiRTUtils.RuntimeProgressReporter feedback) {
        try {
            MultiRTUtils.installRuntimeNamedBinpack(
                    assetManager.open("components/jre-new/universal.tar.xz"),
                    assetManager.open("components/jre-new/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                    "Internal-17", rt_version, feedback);
            MultiRTUtils.postPrepare("Internal-17");
            return true;
        }catch (IOException e) {
            Log.e("JRE17Auto", "Internal JRE unpack failed", e);
            return false;
        }
    }
    public static boolean isInternalNewJRE(String s_runtime) {
        Runtime runtime = MultiRTUtils.read(s_runtime);
        if(runtime == null) return false;
        return NEW_JRE_NAME.equals(runtime.name);
    }

    /** @return true if everything is good, false otherwise.  */
    public static boolean installNewJreIfNeeded(Activity activity, JMinecraftVersionList.Version versionInfo) {
        //Now we have the reliable information to check if our runtime settings are good enough
        if (versionInfo.javaVersion == null || versionInfo.javaVersion.component.equalsIgnoreCase("jre-legacy"))
            return true;

        LauncherProfiles.update();
        MinecraftProfile minecraftProfile = LauncherProfiles.mainProfileJson.profiles.get(LauncherPreferences.DEFAULT_PREF.getString(LauncherPreferences.PREF_KEY_CURRENT_PROFILE, ""));

        String selectedRuntime = null;
        if (minecraftProfile.javaDir != null && minecraftProfile.javaDir.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX))
            selectedRuntime = minecraftProfile.javaDir.substring(Tools.LAUNCHERPROFILES_RTPREFIX.length());

        Runtime runtime = selectedRuntime != null ? MultiRTUtils.read(selectedRuntime) : MultiRTUtils.read(LauncherPreferences.PREF_DEFAULT_RUNTIME);
        if (runtime.javaVersion >= versionInfo.javaVersion.majorVersion) return true;

        String appropriateRuntime = MultiRTUtils.getNearestJreName(versionInfo.javaVersion.majorVersion);
        if (appropriateRuntime != null) {
            if (JRE17Util.isInternalNewJRE(appropriateRuntime)) {
                JRE17Util.checkInternalNewJre(activity.getAssets(), new MultiRTUtils.RuntimeProgressReporter() {
                    @Override
                    public void reportStringProgress(int resId, Object... stuff) {

                    }
                });
            }
            minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + appropriateRuntime;
            LauncherProfiles.update();
        } else {
            if (versionInfo.javaVersion.majorVersion <= 17) { // there's a chance we have an internal one for this case
                if (!JRE17Util.checkInternalNewJre(activity.getAssets(), new MultiRTUtils.RuntimeProgressReporter() {
                    @Override
                    public void reportStringProgress(int resId, Object... stuff) {

                    }})){
                    showRuntimeFail(activity, versionInfo);
                    return false;
                } else {
                    minecraftProfile.javaDir = Tools.LAUNCHERPROFILES_RTPREFIX + JRE17Util.NEW_JRE_NAME;
                    LauncherProfiles.update();
                }
            } else {
                showRuntimeFail(activity, versionInfo);
                return false;
            }
        }

        return true;
    }

    private static void showRuntimeFail(Activity activity, JMinecraftVersionList.Version verInfo) {
        Tools.dialogOnUiThread(activity, activity.getString(R.string.global_error),
                activity.getString(R.string.multirt_nocompartiblert, verInfo.javaVersion.majorVersion));
        }

}