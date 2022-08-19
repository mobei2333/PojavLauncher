package net.kdt.pojavlaunch.authenticator.mojang;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.authenticator.listener.DoneListener;
import net.kdt.pojavlaunch.authenticator.listener.ErrorListener;
import net.kdt.pojavlaunch.authenticator.listener.ProgressListener;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.AuthenticateResponse;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.NetworkResponse;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.RefreshResponse;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.YggdrasilAuthenticator;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.IOException;
import java.util.UUID;

public class MojangBackgroundLogin {

    private final YggdrasilAuthenticator mAuthenticator = new YggdrasilAuthenticator();
    private final android.os.Handler mHandler = new Handler(Looper.getMainLooper());
    private String mUsername, mPassword;
    private MinecraftAccount mMinecraftAccount;

    public MojangBackgroundLogin(@NonNull String username, @NonNull String password){
        mUsername = username;
        mPassword = password;
    }

    public MojangBackgroundLogin(@NonNull MinecraftAccount minecraftAccount){
        mMinecraftAccount = minecraftAccount;
    }

    public void performLogin(@Nullable final ProgressListener progressListener,
                                         @Nullable final DoneListener doneListener,
                                         @Nullable final ErrorListener errorListener){
        new Thread(() -> {
            // Behavior is controlled by which constructor filled the private variables.
            // As it is unlikely that a user needs to login and refresh by starring hours at the UI
            notifyProgress(progressListener, 2);
            MinecraftAccount account;
            try {
                account = mMinecraftAccount == null ? performNewLogin() : performRefreshLogin();
            } catch (Throwable throwable) {
                Log.e("Mojang Auth", throwable.toString());

                if(errorListener != null)
                    errorListener.onLoginError(throwable);
                return;
            }

            notifyProgress(progressListener, 3);
            if(account == null) account = mMinecraftAccount;
            if(account != null) {
                try {
                    account.updateSkinFace();
                    account.save();
                } catch (IOException e) {
                    Log.e("Mojang auth", e.toString());
                }
            }

            notifyProgress(progressListener, 5);
            if(doneListener != null)
                doneListener.onLoginDone(account);



        }).start();

    }

    private MinecraftAccount performNewLogin() throws Throwable {
        AuthenticateResponse response = mAuthenticator.authenticate(mUsername, mPassword, UUID.randomUUID());

        MinecraftAccount minecraftAccount = new MinecraftAccount();
        minecraftAccount.accessToken = response.accessToken;
        minecraftAccount.clientToken = response.clientToken.toString();
        minecraftAccount.username = response.selectedProfile.name;
        minecraftAccount.profileId = response.selectedProfile.id;

        return minecraftAccount;
    }

    private MinecraftAccount performRefreshLogin() throws Throwable {
        NetworkResponse validateResponse = mAuthenticator.validate(mMinecraftAccount.accessToken);

        // Forbidden means the token expired
        if(validateResponse.statusCode == 403){
            RefreshResponse refreshResponse = mAuthenticator.refresh(mMinecraftAccount.accessToken, UUID.fromString(mMinecraftAccount.clientToken));

            if(refreshResponse != null) {  // Cannot refresh, offline ?
                mMinecraftAccount.clientToken = refreshResponse.clientToken.toString();
                mMinecraftAccount.accessToken = refreshResponse.accessToken;
                mMinecraftAccount.username = refreshResponse.selectedProfile.name;
                mMinecraftAccount.profileId = refreshResponse.selectedProfile.id;
            }
        }


        return mMinecraftAccount;
    }


    /** Wrapper to ease notifying the listener */
    private void notifyProgress(@Nullable ProgressListener listener, int step){
        if(listener != null){
            mHandler.post(() -> listener.onLoginProgress(step));
        }
    }

}
