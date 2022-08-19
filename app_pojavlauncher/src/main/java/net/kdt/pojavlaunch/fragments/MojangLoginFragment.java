package net.kdt.pojavlaunch.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.authenticator.mojang.MojangBackgroundLogin;
import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import java.io.File;

public class MojangLoginFragment extends Fragment {
    public static final String TAG = "MOJANG_LOGIN_FRAGMENT";

    private EditText mUsernameEditText, mPasswordEditText;
    private Button mLoginButton;

    public MojangLoginFragment(){
        super(R.layout.fragment_mojang_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindViews(view);

        mLoginButton.setOnClickListener(v -> {
            if(!checkEditText(true)){
                // TODO show a toast or something ?
                return;
            }

            ExtraCore.setValue(ExtraConstants.MOJANG_LOGIN_TODO, new String[]{
                    mUsernameEditText.getText().toString(),
                    mPasswordEditText.getText().toString()
            });

            Tools.removeCurrentFragment(requireActivity());

        });
    }

    /** @return Whether the mail (and password) text are eligible to make an auth request  */
    private boolean checkEditText(boolean online){
        new File(Tools.DIR_ACCOUNT_OLD).mkdir();

        String text = mUsernameEditText.getText().toString();
        String passwordText = mPasswordEditText.getText().toString();
        return !(text.isEmpty()
                || text.length() < 3
                || text.length() > 16
                || !text.matches("\\w+")
                || new File(Tools.DIR_ACCOUNT_NEW + "/" + text + ".json").exists()
                // Online part, password "verification"
                || (online && (
                        passwordText.isEmpty()
                        || passwordText.length() < 3
                ))
        );
    }

    private void bindViews(@NonNull View view){
        mUsernameEditText = view.findViewById(R.id.login_edit_email);
        mPasswordEditText = view.findViewById(R.id.login_edit_password);
        mLoginButton = view.findViewById(R.id.login_button);
    }
}
