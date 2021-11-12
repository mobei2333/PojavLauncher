package net.kdt.pojavlaunch.tasks;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.*;
import androidx.appcompat.widget.*;

import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import java.io.*;
import java.util.*;
import net.kdt.pojavlaunch.*;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;
import net.kdt.pojavlaunch.multirt.RTSpinnerAdapter;
import net.kdt.pojavlaunch.prefs.*;
import net.kdt.pojavlaunch.utils.*;
import net.kdt.pojavlaunch.value.PerVersionConfig;

import androidx.appcompat.widget.PopupMenu;

/** Class refreshing the version list via ExtraCore */
public class RefreshVersionListTask {

    /** Get all versions */
    public void execute(){
        new Thread(() -> {
            ArrayList<JMinecraftVersionList.Version> versions = new ArrayList<>();
            String[] repositories = LauncherPreferences.PREF_VERSION_REPOS.split(";");
            for (String url : repositories) {
                JMinecraftVersionList list;
                Log.i("ExtVL", "Syncing to external: " + url);
                try {
                    list = Tools.GLOBAL_GSON.fromJson(DownloadUtils.downloadString(url), JMinecraftVersionList.class);
                    Log.i("ExtVL","Downloaded the version list, len="+list.versions.length);
                    Collections.addAll(versions,list.versions);
                }catch (IOException e) {e.printStackTrace();}
            }

            // Put all versions into a single version list
            JMinecraftVersionList full_list = new JMinecraftVersionList();
            full_list.versions = versions.toArray(new JMinecraftVersionList.Version[0]);

            //Add the version list to the ExtraCore
            ExtraCore.setValue("version_list_object", full_list);
            ExtraCore.setValue("version_list_string", filter(full_list.versions, new File(Tools.DIR_HOME_VERSION).listFiles()));
        }).start();
    }


    
    private ArrayList<String> filter(JMinecraftVersionList.Version[] list1, File[] list2) {
        ArrayList<String> output = new ArrayList<>();

        for (JMinecraftVersionList.Version value1: list1) {
            if ((value1.type.equals("release") && LauncherPreferences.PREF_VERTYPE_RELEASE) ||
                (value1.type.equals("snapshot") && LauncherPreferences.PREF_VERTYPE_SNAPSHOT) ||
                (value1.type.equals("old_alpha") && LauncherPreferences.PREF_VERTYPE_OLDALPHA) ||
                (value1.type.equals("old_beta") && LauncherPreferences.PREF_VERTYPE_OLDBETA) ||
                (value1.type.equals("modified"))) {
                output.add(value1.id);
            }
        }

        if(list2 != null) for (File value2: list2) {
            if (!output.contains(value2.getName())) {
                output.add(value2.getName());
            }
        }

        return output;
    }
}
