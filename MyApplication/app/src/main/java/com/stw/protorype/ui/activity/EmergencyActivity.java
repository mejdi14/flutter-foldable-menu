/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 24 janv. 2020 16:45:34 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 24 janv. 2020 16:43:26 +0100
 */

package com.stw.protorype.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.streamwide.smartms.lib.core.api.call.CallError;
import com.streamwide.smartms.lib.core.api.call.CompletionCallback;
import com.streamwide.smartms.lib.core.api.call.FallbackCallback;
import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.api.call.STWCallPriority;
import com.streamwide.smartms.lib.core.api.emergency.EmergencyAlertCompletionCallback;
import com.streamwide.smartms.lib.core.api.emergency.EmergencyAlertError;
import com.streamwide.smartms.lib.core.api.emergency.STWEmergencyLevel;
import com.streamwide.smartms.lib.core.api.emergency.STWEmergencyListener;
import com.streamwide.smartms.lib.core.api.emergency.STWEmergencyManager;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.calls.VoipCallNotification;
import com.stw.protorype.ui.activity.calls.VoipCallScreenActivity;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * EmergencyActivity contain a demo for the Emergency feature, both Emergency
 * Alert Message and Emergency Call.
 */
public class EmergencyActivity extends AppCompatActivity implements View.OnClickListener {

    public static String EXTRA_CALL_ID = "extra_call_id";
    /**
     * Log
     */
    private static final String TAG = "EmergencyActivity";
    private TextView mLogView;
    private AppCompatButton mClearLog;
    private StringBuilder mLog;

    /**
     * ------------------ Emergency Alert Message attributes ------------
     */

    /**
     * An emergency Alert Message State goes from INIT,PENDING to OFF states.
     */
    private TextView mEmergencyMessageState;
    private TextView mEmergencyCallFeatureState;
    private AppCompatButton mStartEmergencyMessage;
    private AppCompatButton mStopEmergencyMessage;
    private AppCompatButton mStopReceivedAlert;
    private AppCompatButton mAskReceivedAlert;
    private LinearLayout mReceivedAlertContainer;
    private static final String COLOR_OFF = "#00897b";
    private static final String COLOR_ON = "#e53935";
    private static final String COLOR_PENDING = "#ef6c00";
    private static final String COLOR_NOT_ALLOWED = "#0000ff";
    private String mConversationId;
    private String mReceivedEmergencyMessageId;

    /**
     * ------------------ Emergency Call attributes ----------------
     */
    private AppCompatButton mStartEmergencyCall;
    private AppCompatButton mStopEmergencyCall;
    private STWVCall mEmergencyCall;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.emergency_activity);
        mLog = new StringBuilder();
        initView();
        initEvent();

        // Register STWEmergencyListener to be notified by any Emergency State changes
        STWEmergencyManager.getInstance().registerEmergencyListener(mEmergencyStateListener);
    }

    private void initView()
    {

        mReceivedAlertContainer = findViewById(R.id.demo_emergency_activity_received_container);
        mStopReceivedAlert = findViewById(R.id.demo_emergency_activity_btn_stop_received);
        mAskReceivedAlert = findViewById(R.id.demo_emergency_activity_btn_ask_received);

        mClearLog = findViewById(R.id.demo_emergency_activity_btn_clear_log);
        mLogView = findViewById(R.id.demo_emergency_activity_log);
        mLogView.setMovementMethod(new ScrollingMovementMethod());

        mReceivedAlertContainer.setVisibility(View.GONE);
        mStartEmergencyCall = findViewById(R.id.demo_emergency_activity_btn_start_emergency_call);
        mStopEmergencyCall = findViewById(R.id.demo_emergency_activity_btn_sop_emergency_call);
        mStopEmergencyCall.setEnabled(false);

        mEmergencyMessageState = findViewById(R.id.demo_emergency_activity_message_state);
        mEmergencyCallFeatureState = findViewById(R.id.demo_emergency_activity_emergency_call_feature_state);
        mStartEmergencyMessage = findViewById(R.id.demo_emergency_activity_btn_start_emergency_msg);
        mStopEmergencyMessage = findViewById(R.id.demo_emergency_activity_btn_sop_emergency_msg);
        updateLogText("Start Emergency demo ...");

        refreshEmergencyAlertState();
        refreshEmergencyCallState();
    }

    private void initEvent()
    {

        mStartEmergencyCall.setOnClickListener(this);
        mStopEmergencyCall.setOnClickListener(this);
        mStartEmergencyMessage.setOnClickListener(this);
        mStopEmergencyMessage.setOnClickListener(this);
        mStopReceivedAlert.setOnClickListener(this);
        mAskReceivedAlert.setOnClickListener(this);
        mClearLog.setOnClickListener(this);
    }

    /**
     * To handle Emergency events you have to record a new STWEmergencyListener. This listener
     * is the callback that had to take care of different emergency state :
     *
     *
     - EmergencyMessageReceived        : when a new emergency message has been received.

     - EmergencyAlertOffReceived       : when the emergency alert has been stopped.

     - EmergencyAlertAcknowledged      : when the emergency alert has been Acknowledged.

     - EmergencyLocationReceived       : when the alert location has been received.

     - EmergencyMessageAllowChanged    : when the administrator allow or disallow the Emergency Message feature;

     - EmergencyMessageStateChanged    : when the emergency alert state has been changed

     */
    private STWEmergencyListener mEmergencyStateListener = new STWEmergencyListener() {

        @Override
        public void onEmergencyMessageReceived(String messageId)
        {
            Log.d(TAG, "onEmergencyMessageReceived");
            mReceivedEmergencyMessageId = messageId;
            mReceivedAlertContainer.setVisibility(View.VISIBLE);
            String senderPhoneNumber =
                STWMessagingManager.getInstance().getMessageSender(EmergencyActivity.this, messageId);
            updateLogText("Emergency Message Received -> sender : " + senderPhoneNumber);
        }

        @Override
        public void onEmergencyAlertOffReceived(BaseMessage message)
        {
            Log.d(TAG, "onEmergencyAlertOffReceived");
            mReceivedAlertContainer.setVisibility(View.GONE);
            String senderPhoneNumber =
                STWMessagingManager.getInstance().getMessageSender(EmergencyActivity.this, message.getId());
            updateLogText("Emergency Alert Off Received -> sender : " + senderPhoneNumber);
        }

        @Override
        public void onEmergencyAlertAcknowledged(String threadId)
        {
            Log.d(TAG, "onEmergencyAlertAcknowledged");
            mReceivedAlertContainer.setVisibility(View.GONE);
            updateLogText("onEmergencyAlertAcknowledged");
        }

        @Override
        public void onEmergencyLocationReceived(Context context, BaseMessage message, PhoneItem sender)
        {
            Log.d(TAG, "onEmergencyLocationReceived");
            updateLogText("Emergency Location Received -> Sender : " + sender.getDisplayNumber());
        }

        @Override
        public void onEmergencyMessageAllowChanged(boolean isAllowed)
        {
            Log.d(TAG, "onEmergencyMessageAllowChanged");
            refreshEmergencyAlertState();
            updateLogText("onEmergencyMessageAllowChanged");
        }

        @Override
        public void onEmergencyCallAllowChanged(boolean isAllowed)
        {
            Log.d(TAG, "onEmergencyCallAllowChanged");
            updateLogText("onEmergencyCallAllowChanged");
            updateLogText("Emergency Message Alert feature is allowed = " + isAllowed);
            refreshEmergencyCallState();
        }

        @Override
        public void onEmergencyAmbientListeningAllowChanged(boolean b) {

        }

        @Override
        public void onEmergencyMessageStateChanged(String conversationId)
        {
            Log.d(TAG, "onEmergencyMessageStateChanged");
            updateLogText("onEmergencyMessageStateChanged");
            mConversationId = conversationId;
            refreshEmergencyAlertState();
        }

        @Override
        public void onEmergencyAmbientListeningStateChanged(@NonNull @NotNull String s) {

        }
    };


    @Override
    public void onClick(View view)
    {

        switch (view.getId()) {
        case R.id.demo_emergency_activity_btn_start_emergency_call:
            startEmergencyCall();
            break;

        case R.id.demo_emergency_activity_btn_sop_emergency_call:
            if (mEmergencyCall != null) {
                /**
                 *  Stop VoIP Incoming or outgoing emergency call
                 */
                STWCallManager.getInstance().stopCall(EmergencyActivity.this, mEmergencyCall);
                mEmergencyCall = null;
                updateLogText("Stop Emergency call");
                mStopEmergencyCall.setEnabled(false);

            }
            break;
        case R.id.demo_emergency_activity_btn_start_emergency_msg:
            startEmergencyAlert();
            break;
        case R.id.demo_emergency_activity_btn_sop_emergency_msg:
            if (mConversationId != null && !mConversationId.isEmpty()) {
                /**
                 * Stop the emergency alert corresponding to the specified conversation ID
                 */
                STWEmergencyManager.getInstance().stopEmergencyAlert(this, mConversationId,
                                new EmergencyAlertCompletionCallback() {

                                    @Override
                                    public void onSuccess()
                                    {

                                        /**
                                         * Emergency Alert stopped successfully
                                         */
                                        updateLogText("EmergencyAlertCompletionCallback, onSuccess");
                                    }

                                    @Override
                                    public void onError(EmergencyAlertError error)
                                    {
                                        /**
                                         * Error while stopping Emergency Alert
                                         */
                                        updateLogText("EmergencyAlertCompletionCallback, onError");
                                    }
                                });
            }
            break;
        case R.id.demo_emergency_activity_btn_clear_log:
            clearLog();
            break;
        case R.id.demo_emergency_activity_btn_ask_received:

            if (mReceivedEmergencyMessageId != null && !mReceivedEmergencyMessageId.isEmpty()) {
                /**
                 * Retrieve the message related to the given emergency message ID
                 */
                BaseMessage message =
                    STWMessagingManager.getInstance().getMessageById(this, mReceivedEmergencyMessageId);
                /**
                 *
                 * Acknowledge the emergency alert corresponding to the specified message
                 *
                 * Note That:
                 * - This method does not stop the emergency alarm.
                 * - The other recipients and the trigger of the alarm will not be notified.
                 */

                if (message != null)
                    STWEmergencyManager.getInstance().setEmergencyAlertAcknowledged(this, message.getId(),
                                    new EmergencyAlertCompletionCallback() {

                                        @Override
                                        public void onSuccess()
                                        {
                                            updateLogText("onSuccess sending emergency acknowledgment");
                                        }

                                        @Override
                                        public void onError(EmergencyAlertError error)
                                        {
                                            updateLogText("Error sending emergency acknowledgment : "
                                                + error.getMessage());
                                        }
                                    });
            }
            // Hide the received alert container
            mReceivedAlertContainer.setVisibility(View.GONE);
            break;
        case R.id.demo_emergency_activity_btn_stop_received:
            if (mReceivedEmergencyMessageId != null && !mReceivedEmergencyMessageId.isEmpty()) {
                // Retrieve the message related to the given emergency message ID
                BaseMessage message =
                    STWMessagingManager.getInstance().getMessageById(this, mReceivedEmergencyMessageId);
                // check if message not null and the type of the related conversation is
                // emergency
                if (message != null
                    && STWEmergencyManager.getInstance().isConversationForEmergency(this, message.getThreadId())) {
                    // Stop the emergency alert corresponding to the specified conversation ID
                    STWEmergencyManager.getInstance().stopEmergencyAlert(this, message.getThreadId(),
                                    new EmergencyAlertCompletionCallback() {

                                        @Override
                                        public void onSuccess()
                                        {
                                            updateLogText("Emergency Alert stopped successfully");
                                        }

                                        @Override
                                        public void onError(EmergencyAlertError error)
                                        {
                                            updateLogText("Error stopping Emergency Alert : " + error.getMessage());
                                        }
                                    });
                }
            }
            break;
        default:
            // do nothing...
            break;
        }
    }

    private void startEmergencyAlert()
    {

        final CharSequence[] options = new CharSequence[] { "CRITICAL", "SEVERE", "URGENT" };

        final int[] emergencyAlertLevel = new int[] { STWEmergencyLevel.EMERGENCY_ALERT_CRITICAL,
            STWEmergencyLevel.EMERGENCY_ALERT_SEVERE, STWEmergencyLevel.EMERGENCY_ALERT_URGENT };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Select your option:");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                /**
                 * The user clicked on options[which]
                 *
                 *  - Start and send an emergency alert message with selected alert level
                 *
                 * - EmergencyAlertCompletionCallback : to track the result of starting an emergency alert request
                 */

                STWEmergencyManager.getInstance().startEmergencyAlert(EmergencyActivity.this,
                                emergencyAlertLevel[which], new EmergencyAlertCompletionCallback() {

                                    @Override
                                    public void onSuccess()
                                    {
                                        updateLogText("onSuccess Starting emergency alert");
                                        /**
                                         * some code here...
                                         *
                                         */
                                    }

                                    @Override
                                    public void onError(EmergencyAlertError error)
                                    {
                                        /**
                                         * some code here...
                                         *
                                         */
                                    }
                                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Cancel operation..
            }
        });
        builder.show();
    }

    private void startEmergencyCall()
    {
        // Start a new Emergency PTT call

        /**
         * To launch an emergency PTT call you must instantiate two callbacks :
         *  - CompletionCallback : to handle call events
         *  - FallbackCallback : is performed if the emergency call cannot be started and a cellular call fallback is needed.
         */
        STWEmergencyManager.getInstance().startEmergencyCall(this, "Emergency PTT call DEMO",
                        STWCallPriority.EMERGENCY, new CompletionCallback() {

                            @Override
                            public void onError(CallError callError)
                            {
                                updateLogText("onError:startEmergencyCall : " + callError.getMessage());
                                /**
                                 * some code here...
                                 *
                                 * Note that : callError is the reason why the VoIP call failed to start
                                 *
                                 */
                            }

                            @Override
                            public void onCompletion(STWVCall vCall)
                            {
                                /**
                                 * Note that : vCall is the instance of the VoIP call started
                                 */
                                mEmergencyCall = vCall;
                                updateLogText("onCompletion:startEmergencyCall");
                                mStopEmergencyCall.setEnabled(true);

                                /**
                                 * open voip call screen activity
                                 */
                                openCallScreen(vCall.getSessionIdentifier());

                                /**
                                 * show voip call notification
                                 */
                                VoipCallNotification voipCallNotification = new VoipCallNotification(EmergencyActivity.this, vCall.getSessionIdentifier());
                                voipCallNotification.showNotification(EmergencyActivity.this);

                            }

                        }, new FallbackCallback() {

                            @Override
                            public void onFallback(String emergencyAlertNumber)
                            {
                                updateLogText("onFallback:startEmergencyCall");
                                /**
                                 * Note that : emergencyAlertNumber is the phone number that should be called
                                 * with cellular call application if emergency PTT VoIP call has not be receive
                                 * by any recipient
                                 */
                                startCellularCallActivity(EmergencyActivity.this, emergencyAlertNumber);
                            }
                        });
    }

    public void refreshEmergencyCallState()
    {
        boolean isEmergencyCallAllowed = STWEmergencyManager.getInstance().isEmergencyCallAllowed(this);
        updateLogText("Emergency Call feature is allowed = " + isEmergencyCallAllowed);
        if (!isEmergencyCallAllowed) {
            mEmergencyCallFeatureState.setVisibility(View.VISIBLE);
            mEmergencyCallFeatureState.setText("Feature is not allowed from your admin");
            mEmergencyCallFeatureState.setBackgroundColor(Color.parseColor(COLOR_NOT_ALLOWED));
            mStartEmergencyCall.setVisibility(View.GONE);
            mStopEmergencyCall.setVisibility(View.GONE);
            return;
        }
        mEmergencyCallFeatureState.setVisibility(View.GONE);
        mStartEmergencyCall.setVisibility(View.VISIBLE);
        mStopEmergencyCall.setVisibility(View.VISIBLE);
        mStartEmergencyCall.setEnabled(true);
        mStopEmergencyCall.setEnabled(false);

    }

    public void refreshEmergencyAlertState()
    {
        int emergencyAlertButtonState = STWEmergencyManager.getInstance().getEmergencyMessageState();
        mEmergencyMessageState.setText(getStateText(emergencyAlertButtonState));

        switch (emergencyAlertButtonState) {
        case 0:
            // INIT ......
            boolean isEmergencyAlertAllowed = STWEmergencyManager.getInstance().isEmergencyMessageAllowed(this);
            if (!isEmergencyAlertAllowed) {
                mStartEmergencyMessage.setVisibility(View.GONE);
                mStopEmergencyMessage.setVisibility(View.GONE);
                mEmergencyMessageState.setText("Feature is not allowed from your admin");
                mEmergencyMessageState.setBackgroundColor(Color.parseColor(COLOR_NOT_ALLOWED));
            } else {
                mEmergencyMessageState.setBackgroundColor(Color.parseColor(COLOR_OFF));
                mStartEmergencyMessage.setVisibility(View.VISIBLE);
                mStopEmergencyMessage.setVisibility(View.VISIBLE);
                mStopEmergencyMessage.setEnabled(false);
            }

            break;
        case 2:
            // ON ....
            mEmergencyMessageState.setBackgroundColor(Color.parseColor(COLOR_ON));
            mStartEmergencyMessage.setEnabled(false);
            mStopEmergencyMessage.setEnabled(true);
            break;
        default:
            mEmergencyMessageState.setBackgroundColor(Color.parseColor(COLOR_PENDING));
            mStartEmergencyMessage.setEnabled(false);
            mStopEmergencyMessage.setEnabled(false);
            break;
        }
    }

    private String getStateText(int state)
    {

        /**
         * critical alert
         * int EMERGENCY_ALERT_CRITICAL = 0x1;
         * sever alert
         * int EMERGENCY_ALERT_SEVERE = 0x2;
         * // urgent alert
         * int EMERGENCY_ALERT_URGENT = 0x3;
         * // custom alert
         * int EMERGENCY_ALERT_CUSTOM = 0x4;
         */
        switch (state) {
        case 0:
            Log.d(TAG, "State : INIT");
            return "EMERGENCY OFF";
        case 1:
            Log.d(TAG, "State : PENDING");
            return "PENDING...";
        case 2:
            Log.d(TAG, "State : ON");
            return "EMERGENCY ON";
        case 3:
            Log.d(TAG, "State : PROCESSING");
            return "PROCESSING";
        default:
            Log.d(TAG, "State : default");
            return "";
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // Unregister the STWEmergencyListener to not receive any more the Emergency
        // State changes
        if (mEmergencyStateListener != null) {
            STWEmergencyManager.getInstance().unregisterEmergencyListener(mEmergencyStateListener);
        }
    }

    private void updateLogText(String text)
    {

        STWLoggerHelper.LOGGER.d(Pair.create("Emergency", "updateLogText"), "Emergency", "Text = " + text);
        if (mLog.length() == 0)
            mLog.append("> ");

        mLog.append(getTime());
        mLog.append(" | ");
        mLog.append(text);
        mLog.append("\n> ");

        runOnUiThread(() -> mLogView.setText(mLog.toString()));
    }

    private String getTime()
    {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(Calendar.getInstance().getTime());
    }

    private void clearLog()
    {
        mLog.setLength(0);
        mLogView.setText(">");
    }

    /**
     * start the call screen
     * @param callId the VoIP session id
     */
    private void openCallScreen(String callId) {
        Intent intent = new Intent(this, VoipCallScreenActivity.class);
        intent.putExtra(EXTRA_CALL_ID, callId);
        startActivity(intent);
    }

    /**
     *
     * @param context The application context
     * @param number call fallback number
     */
    private void startCellularCallActivity(Context context, String number)
    {
        if (number == null) {
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception exception) {
            Toast.makeText(context, "No application found", Toast.LENGTH_SHORT).show();

        }
    }
}
