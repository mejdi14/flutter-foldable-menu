/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 24 Jan 2020 12:11:00 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 24 Jan 2020 11:49:29 +0100
 */

package com.stw.protorype.ui.activity.login;

import java.net.MalformedURLException;

import com.streamwide.smartms.lib.core.api.account.STWAccountError;
import com.streamwide.smartms.lib.core.api.account.STWAccountManager;
import com.streamwide.smartms.lib.core.api.account.login.CompletionCallback;
import com.streamwide.smartms.lib.core.api.account.login.RegisterInfo;
import com.streamwide.smartms.lib.core.api.account.login.RegistrationCallback;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.stw.protorype.MainConstant;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.MainActivity;
import com.stw.protorype.util.Utils;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private static final String CLASS_NAME = "LoginActivity";
    private static final String TAG = "Authentication";

    // login input
    private EditText mCompanyId;
    private EditText mPhoneNumber;
    private LinearLayout mLoginContainer;
    // Error
    private RelativeLayout mErrorContainer;
    private ProgressBar mProgressBar;
    // activation code
    private LinearLayout mActivationCodeContainer;
    private EditText mActivationCodeInput;
    private Button mActivateAccount;

    // login type
    private int mLoginType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

            mLoginContainer = findViewById(R.id.login_ll_input);
            mCompanyId = findViewById(R.id.login_company_id);
            mPhoneNumber = findViewById(R.id.login_phone_number);

            Button login = findViewById(R.id.login_button);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String companyId = mCompanyId.getText().toString();
                    String phoneNumber = mPhoneNumber.getText().toString();
                    if (!TextUtils.isEmpty(companyId) && !TextUtils.isEmpty(phoneNumber)) {
                        changeProgressVisibility(View.VISIBLE);
                        STWAccountManager.getInstance().register(LoginActivity.this, phoneNumber, companyId, mRegistrationCallback);

                    }
                }
            });


            mErrorContainer = findViewById(R.id.error_container);
            mErrorContainer.setVisibility(View.GONE);
            mProgressBar = findViewById(R.id.progress);

            mActivationCodeContainer = findViewById(R.id.activation_code_layout);
            mActivationCodeInput = findViewById(R.id.activation_code);
            mActivateAccount = findViewById(R.id.activate_button);


    }


    private void displayActivationCodeView() {
        mLoginContainer.setVisibility(View.GONE);
        mActivationCodeContainer.setVisibility(View.VISIBLE);
        mActivateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activationCode = mActivationCodeInput.getText().toString();
                if (!TextUtils.isEmpty(activationCode)) {
                    changeProgressVisibility(View.VISIBLE);

                    try {
                        STWAccountManager.getInstance().login(LoginActivity.this, mLoginType, activationCode,  mCompletionCallback);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    private CompletionCallback mCompletionCallback = new CompletionCallback() {
        @Override
        public void onError(STWAccountError stwAccountError) {
            STWLoggerHelper.LOGGER.w(Pair.create(CLASS_NAME, "onError"), TAG,
                    "completion callback error received :"+stwAccountError.getMessage());
            handleError(stwAccountError);
        }

        @Override
        public void onSuccess() {

        }

        @Override
        public void onSynchronizationStarted() {
            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onSynchronizationStarted"), TAG,
                    "synchronization started");
        }

        @Override
        public void onSynchronizationFinished() {
            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onSynchronizationFinished"), TAG,
                    "onSynchronizationFinished");

            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onSuccess"), TAG,
                    "onSuccess");

            changeProgressVisibility(View.GONE);

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    };
    RegistrationCallback mRegistrationCallback = new RegistrationCallback() {
        @Override
        public void onSuccess(int loginType, @NonNull RegisterInfo registerInfo) {

            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mRegistrationCallback"), MainConstant.LOGIN,
                    "LoginType = "+loginType );

            changeProgressVisibility(View.GONE);

            mLoginType = loginType;

            Utils.hideSoftInput(LoginActivity.this);

            displayActivationCodeView();
        }

        @Override
        public void onServiceConfigurationSuccess() {
            // Wait
            changeProgressVisibility(View.VISIBLE);
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mRegistrationCallback"), MainConstant.LOGIN,
                    "onServiceConfigurationSuccess");
        }

        @Override
        public void onError(STWAccountError error) {
           //Handle error
            handleError(error);
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mRegistrationCallback"), MainConstant.LOGIN,
                    "onServiceConfigurationSuccess");
        }
    };



    private void handleError(@NonNull STWAccountError error)
    {
        int errorCode = error.getCode();
        changeProgressVisibility(View.GONE);
        switch (errorCode)
        {
            case STWAccountError.AccountErrorCode.UNKNOWN_ERROR:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "UNKNOWN_ERROR");

                Toast.makeText(LoginActivity.this, "login authentication failed", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.SUBSCRIBER_NOT_FOUND:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "SUBSCRIBER_NOT_FOUND");
                Toast.makeText(LoginActivity.this, "SUBSCRIBER_NOT_FOUND", Toast.LENGTH_SHORT).show();
            }
            break;

            case STWAccountError.AccountErrorCode.SUBSCRIBER_BELONGS_TO_DIFFERENT_COMPANY:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "SUBSCRIBER_BELONGS_TO_DIFFERENT_COMPANY");

                Toast.makeText(LoginActivity.this, "SUBSCRIBER BELONGS TO DIFFERENTCOMPANY", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.SUBSCRIBER_BLOCKED:
            case STWAccountError.AccountErrorCode.NUMBER_NOT_ALLOWED:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "NUMBER_NOT_ALLOWED");
                Toast.makeText(LoginActivity.this, "NUMBER_NOT_ALLOWED", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.NO_NETWORK_AVAILABLE:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "NO_NETWORK_AVAILABLE");
                Toast.makeText(LoginActivity.this, "NO_NETWORK_AVAILABLE", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.INVALID_PHONE_NUMBER:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "INVALID_PHONE_NUMBER");
                Toast.makeText(LoginActivity.this, "INVALID_PHONE_NUMBER", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.INVALID_COMPANY_ID:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "INVALID_COMPANY_ID");
                Toast.makeText(LoginActivity.this, "INVALID_COMPANY_ID", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.INCORRECT_LOGIN_TYPE:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "INCORRECT_LOGIN_TYPE");
                Toast.makeText(LoginActivity.this, "INCORRECT_LOGIN_TYPE", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.FORCE_WIPEOUT:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "FORCE_WIPEOUT");

                Toast.makeText(LoginActivity.this, "FORCE_WIPEOUT", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.FCM_TOKEN_NOT_RETRIEVED:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "FCM_TOKEN_NOT_RETRIEVED");
                Toast.makeText(LoginActivity.this, "FCM_TOKEN_NOT_RETRIEVED", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.EMPTY_PHONE_NUMBER:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "EMPTY_PHONE_NUMBER");
                Toast.makeText(LoginActivity.this, "EMPTY_PHONE_NUMBER", Toast.LENGTH_SHORT).show();

            }
            break;
            case STWAccountError.AccountErrorCode.BAD_DEVICE_ID:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "BAD_DEVICE_ID");

                handleBadDeviceIDError();
            }
            break;
            case STWAccountError.AccountErrorCode.BAD_AUTHENTICATION_TK:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "BAD_AUTHENTICATION_TOKEN");
                Toast.makeText(LoginActivity.this, "BAD_AUTHENTICATION_TOKEN", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.AUTHENTICATION_TOKEN_MISSING_OR_EXPIRED:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "AUTHENTICATION_TOKEN_MISSING_OR_EXPIRED");
                Toast.makeText(LoginActivity.this, "AUTHENTICATION_TOKEN_MISSING_OR_EXPIRED", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.AUTHENTICATION_TOKEN_LIMIT_REACHED:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "AUTHENTICATION_TOKEN_LIMIT_REACHED");
                Toast.makeText(LoginActivity.this, "AUTHENTICATION_TOKEN_LIMIT_REACHED", Toast.LENGTH_SHORT).show();
            }
            break;
            case STWAccountError.AccountErrorCode.APP_NEED_UPDATE:
            {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "handleError"), TAG,
                        "APP_NEED_UPDATE");
                Toast.makeText(LoginActivity.this, "APP_NEED_UPDATE", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    /**
     * Displaying the dialog to let user link the current account with this device
     */
    public void handleBadDeviceIDError() {

        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(getString(R.string.bad_device_id_dialog_title));
        alertDialog.setMessage(R.string.bad_device_id_dialog_content);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton(R.string.ok_label_dialog,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        STWAccountManager.getInstance().confirmLogin(LoginActivity.this, mRegistrationCallback);
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(R.string.cancel,
                null);

        // closed

        // Showing Alert Message
        alertDialog.show();
    }

    private void changeProgressVisibility(int visibility){
            mErrorContainer.setVisibility(visibility);
            mProgressBar.setVisibility(visibility);
    }
}
