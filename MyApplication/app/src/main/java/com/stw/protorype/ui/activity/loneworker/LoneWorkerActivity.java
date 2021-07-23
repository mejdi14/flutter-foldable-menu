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
 * @lastModifiedOn Fri, 24 Jan 2020 11:59:29 +0100
 */

package com.stw.protorype.ui.activity.loneworker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.api.loneworker.LoneWorkerError;
import com.streamwide.smartms.lib.core.api.loneworker.LoneWorkerSettingsCallback;
import com.streamwide.smartms.lib.core.api.loneworker.LoneWorkerStartCallback;
import com.streamwide.smartms.lib.core.api.loneworker.LoneWorkerUserSettingsConfig;
import com.streamwide.smartms.lib.core.api.loneworker.STWLoneWorkerManager;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.stw.protorype.MainConstant;
import com.stw.protorype.MainPreference;
import com.stw.protorype.R;
import com.stw.protorype.service.AppForegroundService;
import com.stw.protorype.service.NotificationConstant;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.widget.ContentLoadingProgressBar;

public class LoneWorkerActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String CLASS_NAME = "Demo_LoneWorker";

    private static final String ALLOWED = "Allowed";
    private static final String DISALLOWED = "Disallowed";
    private static final String ON = "ON";
    private static final String OFF = "OFF";

    private static final String COLOR_OFF = "#e53935";
    private static final String COLOR_ON = "#00897b";

    private SwitchCompat mLoneWorkerSwitch;

    private AppCompatButton mBtnReCalibrate;
    private AppCompatButton mBtnClearLog;
    private AppCompatButton mStopDetectedAlert;
    private AppCompatButton mStopReceivedAlert;
    private AppCompatButton mAskReceivedAlert;

    private LinearLayout mReceivedAlertContainer;
    private LinearLayout mDetectedAlertContainer;
    private ContentLoadingProgressBar mCalibrationProgress;
    private TextView mLoneWorkerState;
    private TextView mManDownState;
    private TextView mPositiveSecurityState;
    private TextView mManDownEnabled;
    private TextView mLogView;
    private StringBuilder mLog;
    private String mReceivedEmergencyMessageId;
    private boolean isNotificationServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lone_worker_activity);

        initView();
        initEvent();
        mLog = new StringBuilder();
        updateLogText("Start LoneWorker demo ...");
        updateLogText("State = "+ STWLoneWorkerManager.getInstance().getCurrentManDownState());

        STWLoneWorkerManager.getInstance().registerToLoneWorkerEvent(mLoneWorkerCallback);
        STWLoneWorkerManager.getInstance().registerToPositiveSecurityEvent(mPositiveSecurityCallback);
        STWLoneWorkerManager.getInstance().registerToManDownEvent(mManDownCallback);


        /**
         * To start using Lone worker feature, you need to initialize the user's
         * settings, so you must build a LoneWorkerUserSettingsConfig that provides the
         * state of feature using a registered callback LoneWorkerSettingsCallback as mentioned
         * below :
         */
        LoneWorkerSettingsCallback loneWorkerSettingsCallback = new LoneWorkerSettingsCallback() {
            @Override
            public boolean isLoneWorkerSettingsEnabled() {
                return MainPreference.getInstance(LoneWorkerActivity.this).getBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, false);
            }
        };
        LoneWorkerUserSettingsConfig loneWorkerUserSettingsConfig = new LoneWorkerUserSettingsConfig
                .Builder()
                .loneWorkerSettingsCallback(loneWorkerSettingsCallback)
                .build();
        
        STWLoneWorkerManager.getInstance().initLoneWorkerUserSettings(loneWorkerUserSettingsConfig);
    }

    private void initView(){


        mLoneWorkerSwitch = findViewById(R.id.demo_lone_worker_switch);

        mBtnReCalibrate = findViewById(R.id.demo_lone_worker_activity_btn_re_calibrate);

        mCalibrationProgress = findViewById(R.id.demo_lone_worker_activity_calibrate_progress);

        mLoneWorkerState = findViewById(R.id.demo_lone_worker_lw_state);
        mManDownState = findViewById(R.id.demo_lone_worker_md_state);
        mPositiveSecurityState = findViewById(R.id.demo_lone_worker_ps_state);
        mManDownEnabled = findViewById(R.id.demo_lone_worker_md_state_enabled);

        mDetectedAlertContainer = findViewById(R.id.demo_lone_worker_activity_detected_container);
        mStopDetectedAlert = findViewById(R.id.demo_lone_worker_activity_btn_stop_detected);

        mReceivedAlertContainer = findViewById(R.id.demo_lone_worker_activity_received_container);
        mStopReceivedAlert = findViewById(R.id.demo_lone_worker_activity_btn_stop_received);
        mAskReceivedAlert = findViewById(R.id.demo_lone_worker_activity_btn_ask_received);

        mBtnClearLog = findViewById(R.id.demo_lone_worker_activity_btn_clear_log);
        mLogView = findViewById(R.id.demo_lone_worker_activity_log);
        mLogView.setMovementMethod(new ScrollingMovementMethod());

        mCalibrationProgress.setVisibility(View.GONE);
        mDetectedAlertContainer.setVisibility(View.GONE);
        mReceivedAlertContainer.setVisibility(View.GONE);

        refreshUi();
    }

    private void initEvent(){

        mBtnReCalibrate.setOnClickListener(this);
        mBtnClearLog.setOnClickListener(this);
        mStopReceivedAlert.setOnClickListener(this);
        mAskReceivedAlert.setOnClickListener(this);
        mStopDetectedAlert.setOnClickListener(this);

        mLoneWorkerSwitch.setOnCheckedChangeListener((compoundButton,isChecked)-> {
            if (isChecked ) {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "LoneWorker switched to on"), MainConstant.LOGIN,
                        "LoneWorker switched to on");
                MainPreference.getInstance(LoneWorkerActivity.this).putBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, true);
                STWLoneWorkerManager.getInstance().startLoneWorker(LoneWorkerActivity.this, true, new LoneWorkerStartCallback() {
                    @Override
                    public void onSuccess() {
                        /**
                         * Need to start a foreground service while Lone Worker service is running,
                         * to prevent task killing by the system.
                         */
                        updateLogText("onLoneWorkerServiceStarted");
                        startNotificationService();
                    }

                    @Override
                    public void onError(LoneWorkerError loneWorkerError) {
                        updateLogText("onError : " + loneWorkerError.getMessage());

                    }
                });
                mBtnReCalibrate.setEnabled(true);
                mCalibrationProgress.setVisibility(View.GONE);
                MainPreference.getInstance(LoneWorkerActivity.this).putBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, true);

                refreshUi();
            }else{

                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "LoneWorker switched to off"), MainConstant.LOGIN,
                        "LoneWorker switched to off");
                final CharSequence [] options = new CharSequence[] { "Always", "5 minutes", "10 minutes", "30 minutes", "1 hour"};
                final int [] minutes = new int[] {0, 5, 10, 30, 60};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setSingleChoiceItems(options, 0, null);
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        int selectedPosition = minutes[((AlertDialog)dialog).getListView().getCheckedItemPosition()];
                        // Do something useful withe the position of the selected radio button

                        if (selectedPosition == 0) {
                            STWLoneWorkerManager.getInstance().stopLoneWorker(LoneWorkerActivity.this);
                            MainPreference.getInstance(LoneWorkerActivity.this).putBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, false);

                        } else {
                            STWLoneWorkerManager.getInstance().scheduleLoneWorker(LoneWorkerActivity.this, selectedPosition, false);
                        }
                        mBtnReCalibrate.setEnabled(false);
                        mCalibrationProgress.setVisibility(View.GONE);
                        MainPreference.getInstance(LoneWorkerActivity.this).putBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, false);
                        stopNotificationService();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    //--------------------------- LoneWorker Callbacks ------------------------------------

    /**
     * To handle LoneWorker events you have to record a new
     * STWLoneWorkerManager.LoneWorkerCallback. It's the callback that had to take
     * care of different Lone worker services events :
     */

    STWLoneWorkerManager.LoneWorkerCallback mLoneWorkerCallback = new STWLoneWorkerManager.LoneWorkerCallback() {

        /**
         * Called when LoneWorker feature is changed from administrator
         * 
         * @param enabled
         */
        @Override
        public void onLoneWorkerFeatureChanged(boolean enabled) {

            MainPreference.getInstance(LoneWorkerActivity.this).putBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, enabled);

            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mLoneWorkerCallback"), MainConstant.LOGIN,
                    "onLoneWorkerFeatureChanged");

            updateLogText("onLoneWorkerFeatureChanged = " + enabled);

            if (enabled){
                STWLoneWorkerManager.getInstance().startLoneWorker(LoneWorkerActivity.this, true, new LoneWorkerStartCallback() {
                    @Override
                    public void onSuccess() {
                        /**
                         * Need to start a foreground service while Lone Worker service is running,
                         * to prevent task killing by the system.
                         */
                        updateLogText("onLoneWorkerServiceStarted");
                        startNotificationService();
                    }

                    @Override
                    public void onError(LoneWorkerError loneWorkerError) {
                        updateLogText("onError : " + loneWorkerError.getMessage());

                    }
                });
            }
            refreshUi();
        }

        /**
         * Called when LoneWorker protection feature is set to OFF manually by user from
         * the mobile app.
         * 
         * @param snoozeCallback <strong>{@link STWLoneWorkerManager.SnoozeCallback}</strong> callback with two option :
         *          *                       1- to snooze the break
         *          *                       2- to start the work
         */
        @Override
        public void loneWorkerSnooze(STWLoneWorkerManager.SnoozeCallback snoozeCallback) {

            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mLoneWorkerCallback"), MainConstant.LOGIN,
                    "loneWorkerSnooze");
            if (snoozeCallback != null) {
                mLoneWorkerSwitch.setChecked(true);
            }
            /**
             *  Due to Android background execution limitation, it's necessary to have a
             *  foreground service started while a given service is in running, to
             *  prevent kill of tasks. For this, a service is started to handle notifications of running service
             */
            startNotificationService();
            updateLogText("loneWorkerSnooze, service has been restarted");

        }

        /**
         * Called when LoneWorker service started
         */
        @Override
        public void onLoneWorkerServiceStarted() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mLoneWorkerCallback"), MainConstant.LOGIN,
                    "onLoneWorkerServiceStarted");
        }

        /**
         * Called when LoneWorker service scheduled
         * 
         * @param scheduleDate
         *            String Formatted date of next resumption of service.
         */
        @Override
        public void onLoneWorkerServiceScheduled(String scheduleDate) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mLoneWorkerCallback"), MainConstant.LOGIN,
                    "onLoneWorkerServiceScheduled");
            updateLogText("onLoneWorkerServiceScheduled");
        }
    };

    //--------------------------- END LoneWorker Callbacks -----------------------------

    //------------------------ Positive Security Callbacks -----------------------------
    /**
     * To handle Positive Security events you have to record a new
     * `STWLoneWorkerManager.PositiveSecurityCallback`. This callback is used to
     * handle the different Positive Security events.
     */
    STWLoneWorkerManager.PositiveSecurityCallback mPositiveSecurityCallback = new STWLoneWorkerManager.PositiveSecurityCallback() {

        /**
         * Called when the connection is lost
         *
         * @param timeConnectionLost connection lost date
         */
        @Override
        public void onPositiveSecurityConnectionLost(Date timeConnectionLost) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mPositiveSecurityCallback"), MainConstant.LOGIN,
                    "onPositiveSecurityConnectionLost");

            updateLogText("onPositiveSecurityConnectionLost");

        }

        /**
         * Called when the connection to the backend is back
         */
        
        @Override
        public void onPositiveSecurityConnectionBack() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mPositiveSecurityCallback"), MainConstant.LOGIN,
                    "onPositiveSecurityConnectionBack");
            updateLogText("onPositiveSecurityConnectionBack");
        }

        /**
         * Called when the alert for positive security is stopped
         */
        @Override
        public void onPositiveSecurityAlertStopped() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mPositiveSecurityCallback"), MainConstant.LOGIN,
                    "onPositiveSecurityAlertStopped");
            updateLogText("onPositiveSecurityAlertStopped");
        }
    };


    //------------------------ END Positive Security Callbacks -----------------

    //----------------------------- Man Down Callbacks -------------------------

    /**
     * To handle Man Down events you have to record a new
     * `STWLoneWorkerManager.ManDownCallback`. This callback is used to handle the
     * different Man Down events.
     */
    STWLoneWorkerManager.ManDownCallback mManDownCallback = new STWLoneWorkerManager.ManDownCallback() {
        /**
         * Called at reception of new man down message.
         *
         * @param messageId
         */
        @Override
        public void manDownMessageReceived(String messageId) {

            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownMessageReceived");

            updateLogText("manDownMessageReceived | msg id = "+messageId);
            mReceivedEmergencyMessageId = messageId;
            mReceivedAlertContainer.setVisibility(View.VISIBLE);
        }

        /**
         * Called at the reception of alert sender's location.
         *
         * @param context
         * @param message
         * @param sender
         */
        @Override
        public void onManDownLocationReceived(Context context, BaseMessage message, PhoneItem sender) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "onManDownLocationReceived");
            updateLogText("onManDownLocationReceived | sender = "+sender.getDisplayNumber());
        }

        /**
         * Called at reception of an man down off event.
         *
         * @param message
         */
        @Override
        public void onManDownAlertOffReceived(BaseMessage message) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "onManDownAlertOffReceived");
            updateLogText("onManDownAlertOffReceived");
            mReceivedAlertContainer.setVisibility(View.GONE);
        }

        /**
         * Called when man down is triggered.
         * @param angle
         */
        @Override
        public void manDownDetected(int angle) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownDetected");
            updateLogText("manDownDetected angle = "+angle);
        }

        /**
         * Called when man down alert is successfully sent.
         * 
         * @param angle
         */
        @Override
        public void manDownAlertSent(int angle) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownAlertSent");
            updateLogText("manDownAlertSent angle = "+angle);
            mDetectedAlertContainer.setVisibility(View.VISIBLE);
        }

        /**
         * Called when man down calibration is failed.
         */
        @Override
        public void manDownCalibrationFailed() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownCalibrationFailed");
            updateLogText("manDownCalibrationFailed");

            refreshCalibrationBtn(true);
        }

        /**
         * Called when man down pre-calibration is started.
         */
        @Override
        public void manDownPreCalibrationStarted() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownPreCalibrationStarted");
            updateLogText("manDownPreCalibrationStarted");
            refreshCalibrationBtn(false);
        }

        /**
         * Called when man down pre-calibration finished.
         */
        @Override
        public void manDownPreCalibrationFinished() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownPreCalibrationFinished");
            updateLogText("manDownPreCalibrationFinished");
            refreshCalibrationBtn(true);
        }

        /**
         * Called when man down calibration finished.
         */
        @Override
        public void manDownCalibrationFinished() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownCalibrationFinished");
            updateLogText("manDownCalibrationFinished");
            refreshCalibrationBtn(true);
        }

        /**
         * Called when man down calibration started.
         */
        @Override
        public void manDownCalibrationStarted() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownCalibrationStarted");
            updateLogText("manDownCalibrationStarted");
            refreshCalibrationBtn(false);
        }

        /**
         * Called when man down service is stopped.
         */
        @Override
        public void manDownServiceStopped() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownServiceStopped");
        }

        /**
         * Called at reception of an man down off event.
         */
        @Override
        public void manDownAlertOffReceived() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownAlertOffReceived");
        }

        /**
         * Called when man dow alert is canceled.
         */
        @Override
        public void manDownAlertCanceled() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "mManDownCallback"), MainConstant.LOGIN,
                    "manDownAlertCanceled");
        }

        /**
         * Called at reception of an man down acknowledged event
         * 
         * @param threadId
         */
        @Override
        public void manDownAlertAcknowledged(String threadId) {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "manDownAlertAcknowledged"), MainConstant.LOGIN,
                    "manDownAlertAcknowledged");
            updateLogText("manDownAlertAcknowledged");
            mReceivedAlertContainer.setVisibility(View.GONE);
        }
    };

    //--------------------------- END Man Down Callbacks ----------------------


    private void refreshUi()
    {

        boolean isLoneWorkerAllowed = STWLoneWorkerManager.getInstance().isLoneWorkerAllowed();
        boolean isManDownDetectionAllowed = STWLoneWorkerManager.getInstance().isManDownDetectionAllowed();
        boolean isPositiveSecurityAllowed = STWLoneWorkerManager.getInstance().isPositiveSecurityAllowed();

        boolean isManDownEnabled = STWLoneWorkerManager.getInstance().isManDownEnabled(this);

        setTextAllowed(mLoneWorkerState, isLoneWorkerAllowed);
        setTextAllowed(mManDownState, isManDownDetectionAllowed);
        setTextAllowed(mPositiveSecurityState, isPositiveSecurityAllowed);
        setTextOnOff(mManDownEnabled, isManDownEnabled);

        if(isLoneWorkerAllowed) {

            if (isManDownEnabled || isPositiveSecurityAllowed) {

                boolean isLoneWorkerSettingEnabled =  MainPreference.getInstance(LoneWorkerActivity.this).getBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, false);

                mLoneWorkerSwitch.setChecked(isLoneWorkerSettingEnabled);

                mLoneWorkerSwitch.setEnabled(true);
           }
            refreshCalibrationBtn(true);

        }else{
            mLoneWorkerSwitch.setEnabled(false);
            mLoneWorkerSwitch.setChecked(false);
            refreshCalibrationBtn(false);
        }

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.demo_lone_worker_activity_btn_clear_log:
                clearLog();
                break;
            case R.id.demo_lone_worker_activity_btn_re_calibrate:
                STWLoneWorkerManager.getInstance().recalibrate(this, new LoneWorkerStartCallback() {
                    @Override
                    public void onSuccess() {
                        refreshCalibrationBtn(false);
                    }

                    @Override
                    public void onError(LoneWorkerError loneWorkerError) {
                        updateLogText("onError : " + loneWorkerError.getMessage());
                    }
                });
                refreshCalibrationBtn(false);
                break;
            case R.id.demo_lone_worker_activity_btn_stop_detected:
                /**
                 * To stop the man down alert, triggered by current user
                 */
                STWLoneWorkerManager.getInstance().stopManDownAlert(this);
                mDetectedAlertContainer.setVisibility(View.GONE);
                break;
            case R.id.demo_lone_worker_activity_btn_stop_received :
                if(mReceivedEmergencyMessageId != null && !mReceivedEmergencyMessageId.isEmpty()) {
                    // Retrieve the message related to the given Man down message ID
                    BaseMessage message = STWMessagingManager.getInstance().getMessageById(this, mReceivedEmergencyMessageId);
                    // check if message not null and the type of the related conversation is man down
                    if(message != null && STWLoneWorkerManager.getInstance().isConversationForManDown(this,message.getThreadId())){
                        /**
                         *  Stop the received Man down alert corresponding to the specified conversation ID
                         */
                        STWLoneWorkerManager.getInstance().stopManDownAlert(this, message.getThreadId());
                    }
                }
                break;
            case R.id.demo_lone_worker_activity_btn_ask_received :
                if(mReceivedEmergencyMessageId != null && !mReceivedEmergencyMessageId.isEmpty()) {
                    // Retrieve the message related to the given Man down message ID
                    BaseMessage message = STWMessagingManager.getInstance().getMessageById(this, mReceivedEmergencyMessageId);
                    // Acknowledge the man down alert corresponding to the specified message
                    if(message != null) STWLoneWorkerManager.getInstance().setManDownAlertAcknowledged(this, message);
                }
                //Hide the received alert container
                mReceivedAlertContainer.setVisibility(View.GONE);
                break;
            default :
                // do nothing ...
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        STWLoneWorkerManager.getInstance().unregisterLoneWorkerEvent(mLoneWorkerCallback);
        STWLoneWorkerManager.getInstance().unregisterPositiveSecurityEvent(mPositiveSecurityCallback);
        STWLoneWorkerManager.getInstance().unregisterManDownEvent(mManDownCallback);
    }

    private void updateLogText(String text){

        if(mLog == null){
            return;
        }

        if(mLog.length() == 0)  mLog.append("> ");

        mLog.append(getTime());
        mLog.append(" | ");
        mLog.append(text);
        mLog.append("\n> ");

        mLogView.setText(mLog.toString());
    }

    private String getTime(){
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(Calendar.getInstance().getTime());
    }

    private void clearLog(){
        mLog.setLength(0);
        mLogView.setText(">");
    }

    private void setTextAllowed(TextView tv, boolean allowed){
        tv.setText(allowed ? ALLOWED : DISALLOWED);
        tv.setTextColor(Color.parseColor(allowed ? COLOR_ON : COLOR_OFF));
    }

    private void setTextOnOff(TextView tv, boolean isOn){

        tv.setText(isOn ? ON : OFF);
        tv.setTextColor(Color.parseColor(isOn ? COLOR_ON : COLOR_OFF));
    }

    private void refreshCalibrationBtn(boolean activate){
        boolean isLoneWorkerEnabled = MainPreference.getInstance(LoneWorkerActivity.this).getBoolean(MainPreference.LONE_WORKER_SETTING_ENABLED, false);
        mBtnReCalibrate.setEnabled(isLoneWorkerEnabled && activate);
        mCalibrationProgress.setVisibility(!isLoneWorkerEnabled || activate ? View.GONE : View.VISIBLE);
    }
    
    protected void startNotificationService()
    {
        if (!isNotificationServiceStarted) {
            updateLogText("Start notification service...");
            isNotificationServiceStarted = true;
            Intent serviceIntent = new Intent(LoneWorkerActivity.this, AppForegroundService.class);
            serviceIntent.setAction(
                            NotificationConstant.NotificationServiceAction.LONE_WORKER_NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LoneWorkerActivity.this.startForegroundService(serviceIntent);
            } else {
                LoneWorkerActivity.this.startService(serviceIntent);
            }
        }
    }

    protected void stopNotificationService()
    {
        updateLogText("Stop notification service...");
        isNotificationServiceStarted = false;
        AppForegroundService.stopNotificationService(LoneWorkerActivity.this,
                        NotificationConstant.NotificationKey.LONE_WORKER_SERVICE_NOTIFICATION_ID,
                        NotificationConstant.ChannelId.FOREGROUND_SERVICE_CHANNEL_ID);
    }
}
