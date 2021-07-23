/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on lun., 6 janv. 2020 11:41:23 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 6 janv. 2020 11:41:23 +0100
 */

package com.stw.protorype.ui.activity.calls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.streamwide.smartms.lib.core.api.call.CallError;
import com.streamwide.smartms.lib.core.api.call.CompletionCallback;
import com.streamwide.smartms.lib.core.api.call.EndCallStatus;
import com.streamwide.smartms.lib.core.api.call.FloorGrantedPercentProgressObserver;
import com.streamwide.smartms.lib.core.api.call.PocFloorControlEventListener;
import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.api.call.STWFloorDenyReason;
import com.streamwide.smartms.lib.core.api.call.STWFloorRevokeReason;
import com.streamwide.smartms.lib.core.api.call.STWParticipantState;
import com.streamwide.smartms.lib.core.api.call.SessionParticipantEventListener;
import com.streamwide.smartms.lib.core.api.call.SessionStateListener;
import com.streamwide.smartms.lib.core.api.call.VoIPTimerObserver;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.VoipSessionItem;
import com.streamwide.smartms.lib.core.network.voip.STWFreeCall;
import com.streamwide.smartms.lib.core.network.voip.STWLiveStreamCall;
import com.streamwide.smartms.lib.core.network.voip.STWPTTCall;
import com.streamwide.smartms.lib.core.network.voip.STWSurfaceView;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;
import com.stw.protorype.R;

import java.util.ArrayList;

public class VoipCallScreenActivity extends AppCompatActivity implements View.OnClickListener, VoIPTimerObserver, FloorGrantedPercentProgressObserver {

    private static final String CLASS_NAME = "VoipCallScreenActivity";

    private String mCallId;
    private STWVCall mVCall;

    private AppCompatTextView mVoipCallTitle;
    private AppCompatTextView mVoipCallInfo;
    private AppCompatTextView mVoipCallTimer;
    private AppCompatButton mStopVoipCallButton;

    private RelativeLayout mVideoPreviewcontainer;
    private STWSurfaceView mFeedbackSurfaceView;
    private STWSurfaceView mRemoteSurfaceView;

    private RelativeLayout mPttOptionsContainer;
    private AppCompatTextView mPocFloorStatus;
    private AppCompatButton mRequestFloorButton;
    private ProgressBar mGrantedFloorProgress;

    private SessionStateListener mSessionStateListener;
    private SessionParticipantEventListener mParticipantEventListener;
    private PocFloorControlEventListener mPocFloorEventListener;
    private boolean mIsCallEventsRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voip_call_screen);

        initResolver();

        if (TextUtils.isEmpty(mCallId)) {
            Toast.makeText(this, "Unknown VoIP call", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initView();

        initData();

        initEvent();

    }

    private void initResolver() {

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        if (intent.getExtras() == null) {
            return;
        }

        mCallId = intent.getStringExtra(VoipCallServicesActivity.EXTRA_CALL_ID);
    }

    private void initView() {
        mVoipCallTitle = findViewById(R.id.voip_call_title);
        mVoipCallInfo = findViewById(R.id.voip_call_info);
        mVoipCallTimer = findViewById(R.id.voip_call_timer);
        mStopVoipCallButton = findViewById(R.id.stop_voip_call_button);

        mVideoPreviewcontainer = findViewById(R.id.video_preview_container);
        mFeedbackSurfaceView = findViewById(R.id.feedback_surface_view);
        mRemoteSurfaceView = findViewById(R.id.remote_surface_view);

        mPttOptionsContainer = findViewById(R.id.ptt_options_container);
        mPocFloorStatus = findViewById(R.id.poc_floor_status);
        mRequestFloorButton = findViewById(R.id.request_floor_button);
        mGrantedFloorProgress = findViewById(R.id.granted_floor_progress);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        STWLoggerHelper.LOGGER.i(null, CLASS_NAME, "onNewIntent");

        String newCallId = intent.getStringExtra("extra_call_id");
        if (TextUtils.isEmpty(newCallId) || newCallId.equals(mCallId)) {
            return;
        }

        unregisterFromVoipCall();

        resetViews();

        mCallId = newCallId;

        initData();
        initEvent();

    }

    private void resetViews() {

        mVoipCallTitle.setText("");
        mVoipCallTimer.setText("");
        mVoipCallInfo.setText("");

        mVideoPreviewcontainer.setVisibility(View.GONE);
        mFeedbackSurfaceView.setVisibility(View.GONE);
        mRemoteSurfaceView.setVisibility(View.GONE);


    }

    private void initData() {

        mVCall = STWCallManager.getInstance().getCallWithID(mCallId);

        if (mVCall == null) {
            Toast.makeText(this, "Unknown VoIP call", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mVoipCallTitle.setText(mVCall.getParticipantNames());

        int callType = mVCall.getCurrentCallType();
        int callStatus = mVCall.getCallStatus();

        switch (callStatus) {
            case STWVCall.STWVCallConnectionStatus.STWVCallRingingStatus: {
                /**
                 * Not supported use case :
                 * call screen is displayed after session has been accepted in case of incoming voIP call
                 */
            }
            break;
            case STWVCall.STWVCallConnectionStatus.STWVCallCallingStatus: {
                if(mVCall.isSessionOutgoing()) {
                    if(mVCall.isParticipantReachedReceived()) {
                        mVoipCallInfo.setText("ringing ...");
                    } else {
                        mVoipCallInfo.setText("calling ...");
                    }
                }

                if (callType == STWVCall.STWVCallType.STWVCallLiveVideoCallType) {
                    displayVideoCallPreviews();
                } else if (callType == STWVCall.STWVCallType.STWVCallLiveStreamCallType) {
                    displayLiveStreamingPreview();
                } else if (callType == STWVCall.STWVCallType.STWVCallPTTCallType) {
                    displayPttCallPreview();
                }
            }
            break;
            case STWVCall.STWVCallConnectionStatus.STWVCallStartedStatus: {

                initTimerEvent();

                if (callType == STWVCall.STWVCallType.STWVCallLiveVideoCallType) {
                    displayVideoCallPreviews();
                } else if (callType == STWVCall.STWVCallType.STWVCallLiveStreamCallType) {
                    displayLiveStreamingPreview();
                } else if (callType == STWVCall.STWVCallType.STWVCallPTTCallType) {
                    displayPttCallPreview();
                }
            }
            break;
            case STWVCall.STWVCallConnectionStatus.STWVCallEndedStatus: {

                mStopVoipCallButton.setEnabled(false);
            }
            break;
        }

    }


    private void initEvent() {

        mStopVoipCallButton.setOnClickListener(this);

        mRequestFloorButton.setOnTouchListener((v, event) -> {
            int eventaction = event.getAction();
            switch (eventaction) {
                case MotionEvent.ACTION_DOWN:
                    /**
                     * Kicks-off a request to take the floor
                     */
                    ((STWPTTCall) mVCall).requestMediaBurst(new CompletionCallback() {
                        @Override
                        public void onError(CallError error) {
                            Log.d(CLASS_NAME, "Error to request the floor : "+error.getMessage());
                        }

                        @Override
                        public void onCompletion(STWVCall vCall) {
                            Log.d(CLASS_NAME, "You are talking");
                            mRequestFloorButton.setPressed(true);
                        }
                    });

                    return true;
                case MotionEvent.ACTION_UP:

                    Log.d(CLASS_NAME, "Release the floor");

                    /**
                     * Kicks-off a request to release the floor
                     */
                    ((STWPTTCall) mVCall).releaseMediaBurst((new CompletionCallback() {
                        @Override
                        public void onCompletion(STWVCall vCall) {
                            /**
                             * some code here...
                             *
                             * Note that : vCall is the instance of the VoIP call related to the media burst release
                             */
                            Log.d(CLASS_NAME, "onCompletion");

                        }

                        @Override
                        public void onError(CallError error) {
                            /**
                             * some code here...
                             *
                             * Note that : error is the reason why the VoIP call failed to execute the media burst release
                             * (See : {@link CallError})
                             */
                            Log.e(CLASS_NAME, "onError = "+error);

                        }
                    }));
                    mRequestFloorButton.setPressed(false);
                    break;
            }
            return false;
        });

        initTimerEvent();

        /**
         * Listener for session state (CALLING,IN_CALL, IDLE, connecting...)
         */
        mSessionStateListener = new SessionStateListener() {

            @Override
            public void calling(STWVCall vCall) {

                Log.d(CLASS_NAME, "SessionStateListener : calling");
                if(vCall.isSessionOutgoing()) {
                    mVoipCallInfo.setText("calling ...");
                }
            }

            @Override
            public void onSessionStarted(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : onSessionStarted");
                if(sessionHolder.isSessionOutgoing()) {
                    mVoipCallInfo.setText("calling ...");
                }
            }

            @Override
            public void inCall(STWVCall vCall) {
                Log.d(CLASS_NAME, " SessionStateListener : inCall");
                mVoipCallInfo.setText("");

                initTimerEvent();

                if (vCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallLiveVideoCallType) {
                    displayVideoCallPreviews();
                } else if (vCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallLiveStreamCallType) {
                    displayLiveStreamingPreview();
                }  else if (vCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallPTTCallType) {
                    displayPttCallPreview();
                }
            }

            @Override
            public void onSessionConnected(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : onSessionConnected");
                mVoipCallInfo.setText("");
            }

            @Override
            public void onSessionReconnecting(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : onSessionReconnecting");
                mVoipCallInfo.setText("reconnecting...");
            }

            @Override
            public void onAllSessionsReconnecting() {
                Log.d(CLASS_NAME, "SessionStateListener : onAllSessionsReconnecting");
                mVoipCallInfo.setText("reconnecting...");
            }

            @Override
            public void onSessionOnHold(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, " SessionStateListener : onSessionOnHold");
                mVoipCallInfo.setText("On Hold");
            }

            @Override
            public void onSessionResumed(STWVCall sessionHolder) {

                Log.d(CLASS_NAME, "SessionStateListener : onSessionResumed");
                mVoipCallInfo.setText("");
            }

            @Override
            public void onQosChanged(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : onQosChanged");
                Log.i(CLASS_NAME, "new qos value :"+sessionHolder.getQosIndicator());
            }

            @Override
            public void onAudioPlayVolumeChanged( STWVCall stwvCall) {

            }

            @Override
            public void onAudioCaptureVolumeChanged( STWVCall stwvCall) {

            }

            @Override
            public void onSessionStopped(@NonNull VoipSessionItem voipSessionItem) {

                int clientReason = EndCallStatus.DEFAULT_END_CALL_STATUS;
                if (voipSessionItem != null) {
                    clientReason = voipSessionItem.getEndCallStatus();
                }
                Log.i(CLASS_NAME, "SessionStateListener : onSessionStopped - clientReason = " + clientReason);
                mVoipCallInfo.setText("Call ended");

                mStopVoipCallButton.setEnabled(false);
                mRequestFloorButton.setEnabled(false);
                int stoppedCallType = voipSessionItem.getSessionType();
                int mediaCallType = voipSessionItem.getSessionMediaType();

                if ((stoppedCallType ==VoipSessionItem.SESSION_TYPE_LIVE
                        && mediaCallType == VoipSessionItem.SESSION_MEDIA_AUDIO_VIDEO)
                        || stoppedCallType == VoipSessionItem.SESSION_TYPE_LIVE_STREAM ) {
                    mFeedbackSurfaceView.setVisibility(View.GONE);
                    mRemoteSurfaceView.setVisibility(View.GONE);
                } else if (stoppedCallType == VoipSessionItem.SESSION_TYPE_POC){
                    mPocFloorStatus.setText("");
                }

                handleStopSessionReason(clientReason);
            }

            @Override
            public void onVoipSessionClosed(@NonNull VoipSessionItem voipSessionItem) {
                Log.d(CLASS_NAME, "SessionStateListener : Voip session closed");

            }

            @Override
            public void onSessionAnsweredFromOtherDevice(String sessionIdentifier) {
                Log.d(CLASS_NAME, "SessionStateListener : onSessionAnsweredFromOtherDevice");
                finish();
            }

            @Override
            public void onAudioOutputChanged() {

            }
        };

        /**
         * Listener for all events related to sessions participants
         */
        mParticipantEventListener = new SessionParticipantEventListener() {

            @Override
            public void onParticipantJoined(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantJoined");
                Log.i(CLASS_NAME, participant +" has joined the session");
            }

            @Override
            public void onParticipantLeft(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantLeft");
                Log.i(CLASS_NAME, participant +" has left the session");
            }

            @Override
            public void onParticipantRefused(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType, @STWParticipantState int participantState)
            {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantRefused");
                Log.i(CLASS_NAME, participant+" has refused the session");
            }

            @Override
            public void onParticipantReached(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantReached");
                mVoipCallInfo.setText("ringing ...");

            }

            @Override
            public void onParticipantOnHold(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantOnHold");
                Log.i(CLASS_NAME, participant +" is now on hold");
            }

            @Override
            public void onParticipantResumed(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantResumed");
                Log.i(CLASS_NAME, participant +" has resumed the session");
            }

            @Override
            public void onParticipantConnected(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantConnected");
                Log.i(CLASS_NAME, participant +" is now connected");
            }

            @Override
            public void onParticipantReconnecting(@NonNull STWVCall sessionHolder, @NonNull String participant, @STWVCall.STWVCallParticipantType int participantType) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantReconnecting");
                Log.i(CLASS_NAME, participant +" is reconnecting");
            }

            @Override
            public void onParticipantInvited(String oldSessionId, SparseArray<ArrayList<PhoneItem>> participantMap,
                                             STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "ParticipantEventListener : onParticipantInvited");
                /**
                 * when PTT Call goes from 1 to 1 participant to PTT group call, local session Id will change
                 */
                if(TextUtils.isEmpty(oldSessionId)  && oldSessionId.equals(mCallId)) {

                    unregisterFromVoipCall();

                    mCallId = sessionHolder.getSessionIdentifier();

                    registerForVoipCallEvents();
                }

                mVoipCallTitle.setText(sessionHolder.getParticipantNames());
            }
        };


        /**
         * Listener for all events related to PTT session
         */
        if (mVCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallPTTCallType) {
            mPocFloorEventListener = new PocFloorControlEventListener() {

                @Override
                public void onNewFloorControlStatus(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, "onNewFloorControlStatus pushButtonStatus = "+pushButtonStatus);
                    refreshPushButtonState(pushButtonStatus);
                }

                @Override
                public void onFloorControlRequest(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, "onFloorControlRequest pushButtonStatus = "+pushButtonStatus);
                    refreshPushButtonState(pushButtonStatus);
                }

                @Override
                public void onFloorControlRelease(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, "onFloorControlRelease pushButtonStatus = "+pushButtonStatus);
                    refreshPushButtonState(pushButtonStatus);
                }

                @Override
                public void onFloorControlGranted(STWVCall sessionHolder, int pushButtonStatus, boolean isRevokedFloor) {
                    Log.i(CLASS_NAME, "onFloorControlGranted pushButtonStatus = "+pushButtonStatus);
                    mPocFloorStatus.setText("you are talking");
                    refreshPushButtonState(pushButtonStatus);
                    mGrantedFloorProgress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFloorControlDeny(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, "onFloorControlDeny pushButtonStatus = "+pushButtonStatus);
                    refreshPushButtonState(pushButtonStatus);

                    @STWFloorDenyReason int denyReason = ((STWPTTCall)sessionHolder).getFloorDenyReasonValue();
                    Log.d(CLASS_NAME, "denyReason = "+denyReason);
                }

                @Override
                public void onFloorControlIdle(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, " onFloorControlIdle pushButtonStatus = "+pushButtonStatus);
                    mPocFloorStatus.setText("");
                    refreshPushButtonState(pushButtonStatus);

                }

                @Override
                public void onFloorControlTaken(@NonNull STWVCall sessionHolder, @STWPTTCall.STWVCallFloorState int pushButtonStatus,
                                                boolean isRevokedFloor, @NonNull String floorOwner, @STWVCall.STWVCallParticipantType int floorOwnerType) {
                    Log.i(CLASS_NAME, "onFloorControlTaken pushButtonStatus = "+pushButtonStatus);
                    mPocFloorStatus.setText(floorOwner + " is talking");
                    refreshPushButtonState(pushButtonStatus);
                }

                @Override
                public void onFloorControlRevoke(STWVCall sessionHolder, int pushButtonStatus) {
                    Log.i(CLASS_NAME, "onFloorControlRevoke pushButtonStatus = "+pushButtonStatus);
                    refreshPushButtonState(pushButtonStatus);
                    @STWFloorRevokeReason int revokeReason = ((STWPTTCall)sessionHolder).getFloorRevokeReasonValue();
                    Log.d(CLASS_NAME, "revokeReason = "+revokeReason);

                }

                @Override
                public void onFloorControlUpdated(@NonNull STWVCall sessionHolder, @Nullable String updatedFloorOwner) {
                    Log.d(CLASS_NAME, "onFloorControlUpdated");
                }

                @Override
                public void onNotifyUserTalkingInOtherChannel() {
                    Log.d(CLASS_NAME, "onNotifyUserTalkingInOtherChannel");
                }

                @Override
                public void onActivePTTCallChanged() {
                    Log.d(CLASS_NAME, "onActivePTTCallChanged");
                }
            };
        }

        /**
         * regsiter for voip call events
         */
        registerForVoipCallEvents();
    }


    private void refreshPushButtonState(@STWPTTCall.STWVCallFloorState int pushButtonState) {

        switch (pushButtonState) {
            case STWPTTCall.STWVCallFloorState.STATE_ENABLE : {
                mRequestFloorButton.setEnabled(true);
                mRequestFloorButton.setText("PRESS TO REQUEST");
                mRequestFloorButton.setPressed(false);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_PUSHED : {
                mRequestFloorButton.setEnabled(true);
                mRequestFloorButton.setText("DROP TO RELEASE");
                mRequestFloorButton.setPressed(true);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_LISTENING : {
                mRequestFloorButton.setEnabled(false);
                mRequestFloorButton.setText("( LISTENING )");
                mRequestFloorButton.setPressed(false);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_PENDING : {
                mRequestFloorButton.setEnabled(true);
                mRequestFloorButton.setText("( REQUESTING )");
                mRequestFloorButton.setPressed(true);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_CALLING : {
                mRequestFloorButton.setEnabled(false);
                mRequestFloorButton.setText("( CALLING )");
                mRequestFloorButton.setPressed(false);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_RINGING : {
                mRequestFloorButton.setEnabled(false);
                mRequestFloorButton.setText("( RINGING )");
                mRequestFloorButton.setPressed(false);
            }
            break;
            case STWPTTCall.STWVCallFloorState.STATE_DISABLE : {
                mRequestFloorButton.setEnabled(false);
                mRequestFloorButton.setText("( DISABLE )");
                mRequestFloorButton.setPressed(false);
            }
            break;
        }
    }

    private void displayVideoCallPreviews() {

        mVideoPreviewcontainer.setVisibility(View.VISIBLE);

        mFeedbackSurfaceView.setVisibility(View.VISIBLE);
        ((STWFreeCall) mVCall).setPreviewDisplay(mFeedbackSurfaceView);

        if(mVCall.getCallStatus() == STWVCall.STWVCallConnectionStatus.STWVCallStartedStatus) {
            mRemoteSurfaceView.setVisibility(View.VISIBLE);
            ((STWFreeCall) mVCall).setDisplay(mRemoteSurfaceView);
        }

    }

    private void displayLiveStreamingPreview() {

        mVideoPreviewcontainer.setVisibility(View.VISIBLE);

        if(mVCall.isSessionOutgoing()) {
            mFeedbackSurfaceView.setVisibility(View.VISIBLE);
            ((STWLiveStreamCall) mVCall).setPreviewDisplay(mFeedbackSurfaceView);
        } else {
            if (mVCall.getCallStatus() == STWVCall.STWVCallConnectionStatus.STWVCallStartedStatus) {
                mRemoteSurfaceView.setVisibility(View.VISIBLE);
                ((STWLiveStreamCall) mVCall).setDisplay(mRemoteSurfaceView);
            }
        }

    }

    private void displayPttCallPreview() {
        mPttOptionsContainer.setVisibility(View.VISIBLE);

        refreshPushButtonState(((STWPTTCall)mVCall).getPushButtonState());
    }

    /**
     * register to call timer events
     */
    private void initTimerEvent() {
        if (mVCall == null) {
            return;
        }

        /**
         * release the VoIP timer before register to it
         */
        releaseVoIPTimer();

        if (mVCall.getCallStatus() != STWVCall.STWVCallConnectionStatus.STWVCallStartedStatus) {
            return;
        }

        mVCall.registerForVoipCallTimerListener(this);

        if (mVCall != null && mVCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallPTTCallType) {
            ((STWPTTCall) mVCall).registerForFloorGrantedPercentProgressListener(this);
        }

    }

    /**
     * release call timer
     */
    public void releaseVoIPTimer() {
        mVCall.unregisterForVoipCallTimerListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(CLASS_NAME, "onDestroy");
        unregisterFromVoipCall();
    }

    /**
     * register for VoIP call events
     */
    protected void registerForVoipCallEvents() {
        if (TextUtils.isEmpty(mCallId)) {
            return;
        }

        if (mIsCallEventsRegistered) {
            STWCallManager.getInstance().unregisterForSessionEvents(null, mSessionStateListener, mParticipantEventListener,
                    mPocFloorEventListener);
        }
        Log.d(CLASS_NAME, "register For VoIP Call events");

        STWCallManager.getInstance().registerForSessionEvents(null, mSessionStateListener, mParticipantEventListener,
                mPocFloorEventListener, mCallId);

        mIsCallEventsRegistered = true;
    }

    /**
     * unregister from VoIP call events
     */
    protected void unregisterFromVoipCall() {
        if (mIsCallEventsRegistered) {
            STWCallManager.getInstance().unregisterForSessionEvents(null, mSessionStateListener, mParticipantEventListener,
                    mPocFloorEventListener);
            mIsCallEventsRegistered = false;
        }
    }


    /**
     * handle stop session reasons
     *
     * @param error the error value. <p>see {@link EndCallStatus} </p>
     */
    private void handleStopSessionReason(int error) {
        switch (error) {
            case EndCallStatus.OUTGOING_CALL_UNAUTHORIZED_DU_TO_NO_NETWORK:
                Toast.makeText(VoipCallScreenActivity.this, "No network connectivity", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.UNKNOWN_SUBSCRIBER:
                Toast.makeText(VoipCallScreenActivity.this, "Call ended due to unknown subscriber error", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.DECLINED:
                Toast.makeText(VoipCallScreenActivity.this, "Callee is not available", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.BUSY:
                Toast.makeText(VoipCallScreenActivity.this, "Callee already on a call", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.INTERNAL_ERROR:
                Toast.makeText(VoipCallScreenActivity.this, "Call ended with unknown internal error", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.FEATURE_NOT_ALLOWED:
                Toast.makeText(VoipCallScreenActivity.this, "Feature not allowed", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.CUG_DENIED:
                Toast.makeText(VoipCallScreenActivity.this, "Not reachable through call", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.NO_NETWORK:
                Toast.makeText(VoipCallScreenActivity.this, "Call ended due to bad internet connection", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.TOO_MANY_PARTICIPANTS:
                Toast.makeText(VoipCallScreenActivity.this, "Too many participants", Toast.LENGTH_SHORT).show();
                break;
            case EndCallStatus.UNANSWERING:
                Toast.makeText(VoipCallScreenActivity.this, "Unavailable", Toast.LENGTH_SHORT).show();


        }
    }

    @Override
    public void onClick(View view) {

        if (mStopVoipCallButton == view) {
            Log.d(CLASS_NAME, "Stop VoIP Call");
            mStopVoipCallButton.setEnabled(false);
            STWCallManager.getInstance().stopCall(VoipCallScreenActivity.this, mVCall);
        }
    }

    /**
     * callback triggered when timer is started
     */
    @Override
    public void onCallTimeStart() {
        Log.d(CLASS_NAME, "Call timer started");
    }

    /**
     * callback triggered when timer is stopped
     */
    @Override
    public void onCallTimeStop() {
        Log.d(CLASS_NAME, "Call timer stopped");
    }

    @Override
    public void onTick(String formattedTime) {
        mVoipCallTimer.setText(formattedTime);
    }


    @Override
    public void onPocFloorTimerGrantedStopped() {
        mGrantedFloorProgress.setProgress(0);
        mGrantedFloorProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onTick(int i) {
        mGrantedFloorProgress.setProgress(i);
    }
}