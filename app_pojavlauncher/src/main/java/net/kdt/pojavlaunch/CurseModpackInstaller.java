package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;

import com.kdt.mcgui.ProgressLayout;

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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class dedicated to downloading and installing a curseforge modpack
 */
public class CurseModpackInstaller {
    private CurseModpackInstaller(){}

    private static final String CURSE_API_KEY = "$2a$10$lPRxrrWdNTLrwteqR4hC3uCKx5h/DUoOVjKx2XA6WRxhwqY5wtYg.";
    private static final String CURSE_FQDN = "https://api.curseforge.com/";
    private static final String GET_MODS_ENDPOINT = CURSE_FQDN + "v1/mods";

    public static void installModpack(Context context, String mainFileUrl, String originalUserAgent){
        sExecutorService.execute(new Runnable() {

            private final WebView mWebview = new WebView(context);
            private List<String> mUrlAdresses;
            private int mUrlIndex = 0;
            private final String mOutputFolder = Tools.DIR_DATA + "/modpack_data";
            private final String mOutputModFolder = mOutputFolder + "/mods";
            private final String mOutputTexturesFolder = mOutputFolder + "/textures_packs";
            private String mModpackName = "mod_name";
            private String mOverrideFolder = "overrides";
            private JSONObject mManifest = null;

            private final DownloadListener modDownloadListener = (url, userAgent, contentDisposition, mimetype, contentLength) -> {
                sExecutorService.execute(() -> {
                    try {
                        File outputFile = new File(mUrlAdresses.get(mUrlIndex).contains("/texture-packs/")
                                ? mOutputTexturesFolder + "/" + URLUtil.guessFileName(url, contentDisposition, mimetype)
                                : mOutputModFolder + "/" + URLUtil.guessFileName(url, contentDisposition, mimetype));

                        // Download the file
                        DownloadUtils.downloadFileMonitoredWithHeaders(url, outputFile, null, (curr, max) -> {
                            ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, (int) Math.max((float)curr/max*100,0), R.string.mcl_launch_downloading, outputFile.getName());
                        }, userAgent, CookieManager.getInstance().getCookie(url));

                        mUrlIndex++;
                        if(mUrlIndex < mUrlAdresses.size())
                            mWebview.post(() -> mWebview.loadUrl(mUrlAdresses.get(mUrlIndex)));
                        else{
                            createModpackInstance(mManifest.getJSONObject("minecraft").getJSONArray("modLoaders").getJSONObject(0).getString("id"),
                                    mModpackName, mOutputModFolder, mOutputTexturesFolder, mOutputFolder + "/" + mOverrideFolder);
                        }

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                });
            };

            @Override
            public void run() {
                try {
                    mUrlIndex = 0;

                    // First remove any old traces
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(mOutputFolder));
                    new File(Tools.DIR_DATA + "/modpack_data.zip").delete();
                    new File(mOutputFolder).mkdir();

                    //Then download the main modpack file
                    DownloadUtils.downloadFileMonitored(mainFileUrl, Tools.DIR_DATA + "/modpack_data.zip", null,
                            (curr, max) -> ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK,  (int) Math.max((float)curr/max*100,0), R.string.mcl_launch_downloading,"main modpack file"));

                    // unzip the modpack file
                    ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.global_unpacking, "modpackdata.zip");
                    FileUtils.unzip(Tools.DIR_DATA + "/modpack_data.zip", mOutputFolder);
                    //TODO report unzipping progress

                    // Then parse the json and load the credits.
                    //String creditFileContent = org.apache.commons.io.FileUtils.readFileToString(new File(mOutputFolder + "/modlist.html"));
                    mManifest = new JSONObject(org.apache.commons.io.FileUtils.readFileToString(new File(mOutputFolder + "/manifest.json")));

                    mModpackName = mManifest.getString("name");
                    mOverrideFolder = mManifest.getString("overrides");

                    //mUrlAdresses = generateUrlList(creditFileContent, rootObject);
                    mUrlAdresses = generateUrlListAPI(mManifest.getJSONArray("files"));
                    mWebview.post(() -> {
                        mWebview.setDownloadListener(modDownloadListener);
                        mWebview.loadUrl(mUrlAdresses.get(mUrlIndex));
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Build a list of urls for the webview from the manifest data and the curseforge API
     * @param ids The json list containing all project ids and
     */
    private static List<String> generateUrlListAPI(JSONArray ids) throws JSONException {

        int[] projectIds = new int[ids.length()];
        int[] fileIds = new int[ids.length()];
        for (int i=0; i<ids.length(); ++i){
            projectIds[i] = ids.getJSONObject(i).getInt("projectID");
            fileIds[i] = ids.getJSONObject(i).getInt("fileID");
        }

        ArrayList<String> urls = new ArrayList<>();
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(GET_MODS_ENDPOINT).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("x-api-key", CURSE_API_KEY);
            con.setDoOutput(true);
            con.setDoInput(true);

            byte[] input = ("{ \"modIds\":" + Arrays.toString(projectIds) + "}").getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", String.valueOf(input.length));

            try(OutputStream os = con.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            //int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            con.disconnect();

            // Once we got the request data, we need to build urls
            //Note: using regex might be faster than dealing with JSONObject
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray modList = jsonResponse.getJSONArray("data");
            for(int i=0; i<modList.length(); ++i){
                urls.add(modList.getJSONObject(i).getJSONObject("links").getString("websiteUrl")
                        + "/download/" + fileIds[i] + "/file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }

    /** Move all files folders recursively
     * @param directory The directory to move
     * @param parentFolderDest The parent target directory, in which the directory will be moved
     */
    private static void moveFolderContent(File directory, File parentFolderDest){
        File[] files = directory.listFiles();
        if(files == null) return;

        File destFolder = new File(parentFolderDest, directory.getName());
        destFolder.mkdirs();

        for(File file : files){
            if(file.isDirectory()){
                moveFolderContent(file, destFolder);
            }else
                file.renameTo(new File(destFolder, file.getName()));
        }
    }

    /** Create an instance will all the mods and the correct version set, ready to play */
    private static void createModpackInstance(String versionID, String modpackName, String outputModFolder, String outputTextureFolder, String overrideFolder){
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 100, "Creating profile instance");

        // Create a mc instance
        MinecraftProfile profile = new MinecraftProfile();
        profile.gameDir = "/custom_instances/" + modpackName;
        profile.name = modpackName;
        profile.lastVersionId = versionID;
        LauncherProfiles.update();
        LauncherProfiles.mainProfileJson.profiles.put(modpackName, profile);
        LauncherProfiles.update();

        // Move files to the instance
        new File(Tools.DIR_GAME_NEW + profile.gameDir).mkdirs();
        new File(Tools.DIR_GAME_NEW + profile.gameDir + "/mods").mkdirs();
        new File(Tools.DIR_GAME_NEW + profile.gameDir + "/resourcepacks").mkdirs();

        File[] modFiles = new File(outputModFolder).listFiles();
        if(modFiles != null)
            for(File modFile : modFiles){
                modFile.renameTo(new File(Tools.DIR_GAME_NEW + profile.gameDir + "/mods/", modFile.getName()));
            }

        File[] textureFiles = new File(outputTextureFolder).listFiles();
        if(textureFiles != null)
            for(File textureFile : textureFiles){
                textureFile.renameTo(new File(Tools.DIR_GAME_NEW + profile.gameDir + "/resourcepacks/", textureFile.getName()));
            }

        File targetFolder = new File(Tools.DIR_GAME_NEW + profile.gameDir);
        File[] overrideFiles = new File(overrideFolder).listFiles();
        if(overrideFiles != null)
            for(File overrideFile : overrideFiles){
                if(overrideFile.isDirectory())
                    moveFolderContent(overrideFile, targetFolder);
                else
                    overrideFile.renameTo(new File(targetFolder, overrideFile.getName()));
            }

        ProgressLayout.clearProgress(ProgressLayout.INSTALL_MODPACK);
    }

}
