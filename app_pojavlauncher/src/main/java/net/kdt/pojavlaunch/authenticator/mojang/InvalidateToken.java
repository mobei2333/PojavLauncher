package net.kdt.pojavlaunch.authenticator.mojang;

import static net.kdt.pojavlaunch.PojavApplication.sExecutorService;

import android.util.Log;

import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.YggdrasilAuthenticator;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;
import java.util.UUID;

public class InvalidateToken {
    private final YggdrasilAuthenticator mAuthenticator = new YggdrasilAuthenticator();

    public void invalidate(String filename){
        MinecraftAccount account = MinecraftAccount.load(filename);
        invalidate(account);
    }

    public void invalidate(MinecraftAccount account){
        sExecutorService.execute(() -> {
            if(account == null) return;
            new File(Tools.DIR_ACCOUNT_NEW + "/" + account.username + ".json").delete();

            if(!account.accessToken.equals("0")){ // pointless to invalidate local
                try {
                    mAuthenticator.invalidate(account.accessToken,
                            UUID.fromString(account.isMicrosoft ? account.profileId : account.clientToken /* should be? */));
                } catch (Throwable e) {
                    Log.e("InvalidateToken", e.toString());
                }
            }
        });
    }

}
