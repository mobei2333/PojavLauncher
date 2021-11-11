package net.kdt.pojavlaunch.authenticator.mojang;

import android.util.Log;

import net.kdt.pojavlaunch.authenticator.mojang.yggdrasil.*;

import java.util.*;
import net.kdt.pojavlaunch.value.*;

/** Class invalidating tokens for the mojang accounts */
public class InvalidateTokenTask {
    private final YggdrasilAuthenticator authenticator = new YggdrasilAuthenticator();
    private final String accountName;

    public InvalidateTokenTask(String accountName){
        this.accountName = accountName;
    }

    /** Invalidates the token associated to accounts */
    public void execute() {
        new Thread(() -> {
            MinecraftAccount account = MinecraftAccount.load(accountName);
            // Delete the account file now, if the invalidation fails the token can just expire
            account.deleteSaveFile();

            if (account.accessToken.equals("0")) return; // Cracked account, no real token
            try {
                authenticator.invalidate(account.accessToken,
                        UUID.fromString(account.isMicrosoft ? account.profileId : account.clientToken));
            }catch (Throwable e){
                Log.e("Invalidate Token Task", e.toString());
            }
        }).start();
    }
}

