/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on lun., 6 janv. 2020 11:41:50 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 6 janv. 2020 11:41:49 +0100
 */

package com.stw.protorype.ui.activity.calls;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.streamwide.smartms.lib.core.api.call.CallError;
import com.streamwide.smartms.lib.core.api.call.CompletionCallback;
import com.streamwide.smartms.lib.core.api.call.IncomingSessionsListener;
import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.api.call.STWCallPriority;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;
import com.stw.protorype.R;

public class VoipCallServicesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CLASS_NAME = "callServicesActivity";

    public static String EXTRA_CALL_ID = "extra_call_id";

    private AppCompatButton mLiveCalButton;
    private AppCompatButton mCallOutButton;
    private AppCompatButton mVideoCallButton;
    private AppCompatButton mPTTCallButton;
    private AppCompatButton mChannelButton;
    private AppCompatButton mStreamingButton;
    private AppCompatButton mStreamingToDispatchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voip_call_services);

        initView();

        initEvent();
    }

    private void initView() {
        mLiveCalButton = findViewById(R.id.live_call_button);
        mCallOutButton = findViewById(R.id.call_out_button);
        mVideoCallButton = findViewById(R.id.video_call_button);
        mPTTCallButton = findViewById(R.id.ptt_call_button);
        mChannelButton = findViewById(R.id.channel_button);
        mStreamingButton = findViewById(R.id.streaming_button);
        mStreamingToDispatchButton = findViewById(R.id.streaming_to_dispatch_button);
    }

    private void initEvent() {
        mLiveCalButton.setOnClickListener(this);
        mCallOutButton.setOnClickListener(this);
        mVideoCallButton.setOnClickListener(this);
        mPTTCallButton.setOnClickListener(this);
        mChannelButton.setOnClickListener(this);
        mStreamingButton.setOnClickListener(this);
        mStreamingToDispatchButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int callType = -1;
        boolean isChannel = false;
        boolean isStreamToDispatcher = false;

        if(mLiveCalButton == view) {
            callType = STWVCall.STWVCallType.STWVCallLiveCallType;
        } else if(mCallOutButton == view) {
            callType = STWVCall.STWVCallType.STWVCallCallOutCallType;
        } else if(mVideoCallButton == view) {
            callType = STWVCall.STWVCallType.STWVCallLiveVideoCallType;
        } else if(mPTTCallButton == view) {
            callType = STWVCall.STWVCallType.STWVCallPTTCallType;
        } else if(mChannelButton == view) {
            callType = STWVCall.STWVCallType.STWVCallPTTCallType;
            isChannel = true;
        } else if(mStreamingButton == view) {
            callType = STWVCall.STWVCallType.STWVCallLiveStreamCallType;
        } else if(mStreamingToDispatchButton == view) {
            callType = STWVCall.STWVCallType.STWVCallLiveStreamCallType;
            isStreamToDispatcher = true;

        }

        if (callType == STWVCall.STWVCallType.STWVCallLiveStreamCallType && isStreamToDispatcher ){
            launchVoipCall(null, callType, isChannel);
        } else {
            showStartVoipCalldialog(callType, isChannel);
        }

    }

    /**
     * Dialog to enter phone numbers or group ids
     * @param callType call type : FreeCall, CallOut, VideoCall, LiveStreamCall, PTTCall
     * @param isChannel true to connect to channel, need to put the group id in the edit text field
     */
    private void showStartVoipCalldialog(final @STWVCall.STWVCallType int callType, final boolean isChannel) {
        final AppCompatEditText editTextField = new AppCompatEditText(this);

        String message;
        if (isChannel) {
            message = "Channel group id";
            editTextField.setHint("e.g. group:10");
        } else if (callType == STWVCall.STWVCallType.STWVCallPTTCallType
                || callType == STWVCall.STWVCallType.STWVCallLiveStreamCallType) {
            message = "Phone numbers and/or group ids";
            editTextField.setHint("e.g. 33123456789,33123456788,group:10,group:11");
        } else {
            message = "Phone number";
            editTextField.setHint("e.g. 33123456789");
        }

        AlertDialog alertDialog =
                new AlertDialog.Builder(this).setTitle(mLiveCalButton.getText()).setMessage(message)
                        .setView(editTextField).setPositiveButton("OK", (dialog, which) -> {
                    String swTo = editTextField.getText().toString().trim();
                    if(TextUtils.isEmpty(swTo)) {
                        Toast.makeText(VoipCallServicesActivity.this, "Missing information", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.i(CLASS_NAME, "launch VoIP call with swTo :"+swTo+ " - type : "+callType+" - isChannel : "+isChannel);
                    launchVoipCall(swTo, callType, isChannel);

                }).setNegativeButton("Cancel", null).create();
        alertDialog.show();

    }

    /**
     * start a VoIP session
     * @param swTo the callee phone numbers
     * @param callType call type : FreeCall, CallOut, VideoCall, LiveStreamCall, PTTCall
     * @param isChannel true to connect to channel. Needs to put the group id in the edit text field
     */
    private void launchVoipCall(String swTo, int callType, boolean isChannel) {

        CompletionCallback completionCallback = new CompletionCallback() {
            @Override
            public void onError(CallError error) {
                Log.w(CLASS_NAME, " start VoIP Call : on error callback : "+error.getMessage());

                Toast.makeText(VoipCallServicesActivity.this, "fail to start VoIP call : "+error.getMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCompletion(STWVCall vCall) {
                Log.d(CLASS_NAME, "start VoIP Call : on completion callback");
                /**
                 * open voip call screen activity
                 */
                openCallScreen(vCall.getSessionIdentifier());

                /**
                 * show voip call notification
                 */
                VoipCallNotification voipCallNotification = new VoipCallNotification(VoipCallServicesActivity.this, vCall.getSessionIdentifier());
                voipCallNotification.showNotification(VoipCallServicesActivity.this);

            }
        };

        switch (callType) {
            case STWVCall.STWVCallType.STWVCallLiveCallType : {
                Log.d(CLASS_NAME, "start Live Call");
                /**
                 * start a free call
                 */
                STWCallManager.getInstance().startFreeCall(VoipCallServicesActivity.this,
                        swTo, STWCallPriority.NORMAL, completionCallback);

            }
            break;
            case STWVCall.STWVCallType.STWVCallLiveVideoCallType : {
                Log.d(CLASS_NAME, "start Live Video Call");

                /**
                 * start a video call
                 */
                STWCallManager.getInstance().startVideoCall(VoipCallServicesActivity.this,
                        swTo, STWCallPriority.NORMAL, completionCallback);
            }
            break;
            case STWVCall.STWVCallType.STWVCallLiveStreamCallType : {
                Log.d(CLASS_NAME, "start Live Stream Call");
                /**
                 * start a video streaming session
                 */
                String[] swToArray = null;
                if (swTo != null) {
                    swToArray = swTo.split(",");
                }
                STWCallManager.getInstance().startLiveStreamingCall(VoipCallServicesActivity.this,
                        swToArray, STWCallPriority.NORMAL, completionCallback);
            }
            break;
            case STWVCall.STWVCallType.STWVCallPTTCallType : {

                if(isChannel) {
                    Log.d(CLASS_NAME, "connect to channel Call");

                    /**
                     * connect to channel
                     */
                    STWCallManager.getInstance().startChannelCall(VoipCallServicesActivity.this, swTo, completionCallback);
                } else {
                    Log.d(CLASS_NAME, "start PTT Call");
                    /**
                     * start a push to talk session
                     */
                    STWCallManager.getInstance().startPTTCall(VoipCallServicesActivity.this, swTo.split(","),
                            "PTT Conversation", STWCallPriority.NORMAL, false, false, completionCallback);
                }
            }
            break;
            case STWVCall.STWVCallType.STWVCallCallOutCallType : {
                Log.d(CLASS_NAME, "start Call Out");

                /**
                 * start call out session
                 */
                STWCallManager.getInstance().startCallOut(VoipCallServicesActivity.this,
                        swTo, completionCallback);
            }
            break;
            default: {
                Toast.makeText(VoipCallServicesActivity.this, "Unknown call type", Toast.LENGTH_SHORT).show();
            }
            break;

        }
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
}
