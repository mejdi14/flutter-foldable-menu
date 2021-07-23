/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 10 Jan 2020 18:23:21 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 10 Jan 2020 18:22:36 +0100
 */

package com.stw.protorype.configuration;

import java.io.InputStream;

import com.streamwide.smartms.lib.core.api.STWApplicationStateListener;
import com.streamwide.smartms.lib.core.api.STWServiceConfig;
import com.streamwide.smartms.lib.core.api.STWServiceListener;
import com.streamwide.smartms.lib.core.api.SmartMsSDK;
import com.streamwide.smartms.lib.core.api.environment.certif.ITrustStore;
import com.streamwide.smartms.lib.core.api.environment.configuration.STWConfiguration;
import com.streamwide.smartms.lib.core.api.environment.logger.LogLevel;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.stw.protorype.BuildConfig;
import com.stw.protorype.MainApplication;
import com.stw.protorype.R;
import com.stw.protorype.service.AppForegroundService;
import com.stw.protorype.service.NotificationConstant;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ConfigurationManager {
    private static final ConfigurationManager ourInstance = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return ourInstance;
    }

    private ConfigurationManager() {
    }

    public void initSDKConfigurations(@NonNull Context context, @Nullable STWApplicationStateListener applicationListener) {

        //@formatter:off
        STWServiceConfig stwServiceConfig
                = STWServiceConfig.Builder
                .configure()
                .mainServiceListener(new STWServiceListener() {
                    @Override
                    public void startService(Context context) {

                    }

                    @Override
                    public void stopService(Context context) {

                    }
                })
                .geolocationServiceListener(new STWServiceListener() {
                    @Override
                    public void startService(Context context) {

                        startForegroundService(context, NotificationConstant.NotificationServiceAction.GEOLOCATION_NOTIFICATION_SERVICE);

                    }

                    @Override
                    public void stopService(Context context) {

                        stopForegroundService(context, NotificationConstant.NotificationKey.GEOLOCATION_SERVICE_NOTIFICATION_ID);

                    }
                })
                .beaconServiceListener(null)
                .myBusinessServiceListener(null)
                .voIPACMServiceListener(new STWServiceListener() {
                    @Override
                    public void startService(Context context) {
                        startForegroundService(context, NotificationConstant.NotificationServiceAction.VOIP_NOTIFICATION_SERVICE);

                    }

                    @Override
                    public void stopService(Context context) {
                        stopForegroundService(context, NotificationConstant.NotificationKey.VOIP_SERVICE_NOTIFICATION_ID);

                    }
                })
                .voIPServiceListener(new STWServiceListener() {
                    @Override
                    public void startService(Context context) {
                        startForegroundService(context, NotificationConstant.NotificationServiceAction.VOIP_NOTIFICATION_SERVICE);
                    }

                    @Override
                    public void stopService(Context context) {
                        stopForegroundService(context, NotificationConstant.NotificationKey.VOIP_SERVICE_NOTIFICATION_ID);
                    }
                })
                .build();
        //@formatter:on

        LogLevel applicationLogLevel = LogLevel.DEBUG;
        LogLevel sipStackLogLevel = LogLevel.DEBUG;
        LogLevel voIPLogLevel = LogLevel.DEBUG;

        STWLoggerHelper.initApplicationLogLevel(context, applicationLogLevel);
        STWLoggerHelper.initSipStackLogLevel(context, sipStackLogLevel);
        STWLoggerHelper.initVoIPLogLevel(context, voIPLogLevel);
        STWLoggerHelper.debuggableMode(context, BuildConfig.DEBUG);

        //formatter:off
        new STWConfiguration.Builder(context)
                .setDefaultConfigurationServerUrl(context.getString(R.string.config_server_url))
                .enableTlsForConfigurationService(true)
                .setDefaultSipPort(443)
                .setFallbackSipPort(5228)
                .setDefaultHttpPort(80)
                .setFallbackHttpPort(5223)
                .setDefaultVoIPPort(52228)
                .setFallbackVoIPPort(443)
                .setHostNameUrl(context.getString(R.string.host_name_url))
                .enableTlsForSip(true)
                .enableFallback(true)
                .enableUserPasswordEncryption(true)
                .setKeystoreAlias(context.getString(R.string.keystore_alias))
                .enableAlwaysConnectedMode(true)
                .enableTlsForOSM(false)
                .enableTlsForArcGIS(false)
                .setTrustStore(new CustomTrustStore())
                .enableVulnerabilityChecker(false)
                .enableSecureStorageChecker(false)
                .build();
        //formatter:on

        SmartMsSDK.getInstance().initializeApp(context, applicationListener, stwServiceConfig);
    }

    static class CustomTrustStore implements ITrustStore {

        /**
         * @return keystore type (like {@link java.security.KeyStore #getDefaultType()})
         */
        @Override
        public String getKeyStoreType()
        {
            return "BKS";
        }

        /**
         * @return an {@link InputStream} for the bks/jks file
         */
        @Override
        public InputStream getKeyStoreInputStream()
        {
//            return MainApplication.getInstance().getResources().openRawResource(
//                    R.raw.client_truststore);
            return null;
        }

        /**
         * @return the trust password
         */
        @Override
        public String getKeyStorePassword()
        {
            return "YOUR_PASSWORD_HERE";
        }
    }

    private void startForegroundService(Context context, String action) {
        Intent serviceIntent = new Intent(context, AppForegroundService.class);
        serviceIntent.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void stopForegroundService(Context context, int notificationId ) {

        AppForegroundService.stopNotificationService(context,notificationId,
                NotificationConstant.ChannelId.FOREGROUND_SERVICE_CHANNEL_ID);

    }
}
