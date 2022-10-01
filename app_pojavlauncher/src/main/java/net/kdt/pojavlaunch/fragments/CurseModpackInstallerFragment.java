package net.kdt.pojavlaunch.fragments;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.os.Bundle;
import android.os.HandlerThread;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.kdt.mcgui.ProgressLayout;

import net.kdt.pojavlaunch.CurseModpackInstaller;
import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.utils.DownloadUtils;
import net.kdt.pojavlaunch.utils.FileUtils;
import net.kdt.pojavlaunch.value.launcherprofiles.LauncherProfiles;
import net.kdt.pojavlaunch.value.launcherprofiles.MinecraftProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurseModpackInstallerFragment extends Fragment implements OverrideMenuInteractionInterface {

    public static final String TAG = "CurseForgeFragment";


    public CurseModpackInstallerFragment(){
        super(R.layout.fragment_curseforge_downloader);
    }

    private WebView mWebview;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mWebview = view.findViewById(R.id.webview);
        CookieManager.getInstance().removeAllCookie();  // The non deprecated method is not async
        WebSettings settings = mWebview.getSettings();
        //settings.setJavaScriptEnabled(true);
        //settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setMinimumFontSize(1);
        settings.setMinimumLogicalFontSize(1);
        mWebview.setVerticalScrollBarEnabled(true);
        mWebview.setHorizontalScrollBarEnabled(true);

        mWebview.clearHistory();
        mWebview.clearCache(true);
        mWebview.clearFormData();
        mWebview.clearHistory();

        mWebview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if(url.contains("/download?client=y")){
                    // Small button in the mod list, teleport to the files section
                    mWebview.post(() -> mWebview.loadUrl(url.replace("/download?client=y", "/files")));
                    return true;
                }

                if(url.contains("/download/") && url.contains("?client=y")){
                    // Modpack download button, teleport him to the file link
                    mWebview.post(() -> mWebview.loadUrl(url.replace("?client=y", "/file")));
                    return true;
                }

                return false;
            }
        });

        mWebview.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength)
                -> CurseModpackInstaller.installModpack(requireContext(), url, userAgent));

        //TODO either remove the ability to click on buttons or remove the window entirely
        //mWebview.setOnTouchListener((v, event) -> mInstallingMod);

        mWebview.loadUrl("https://www.curseforge.com/minecraft/modpacks");
    }





    @Override
    public boolean allowHome() {
        return true;
    }

    @Override
    public boolean allowBackButton() {
        if(mWebview.canGoBack()){
            mWebview.goBack();
            return false;
        }

        return true;
    }
}
