/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on jeu., 26 déc. 2019 12:45:20 +0100
 * @copyright  Copyright (c) 2019 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2019 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn jeu., 26 déc. 2019 12:45:19 +0100
 */

package com.stw.protorype.ui.activity;

import com.streamwide.smartms.lib.core.api.account.STWAccountManager;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.stw.protorype.MainConstant;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.login.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private String CLASS_NAME = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Check if user already authenticated
        boolean isUserAuthenticated = STWAccountManager.getInstance().isUserAuthenticated(this);

        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "onCreate"), MainConstant.LOGIN,
                "isUserAuthenticated = " + isUserAuthenticated);

        //If user already authenticated redirect to MainActivity, otherwise redirect to Login screen
        if (isUserAuthenticated) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }else{
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }

        finish();
    }
}
