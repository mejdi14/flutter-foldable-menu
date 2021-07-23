/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 10 Jan 2020 16:42:25 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 10 Jan 2020 16:40:43 +0100
 */

package com.stw.protorype;


import android.util.Log;
import android.widget.Toast;
import com.streamwide.smartms.lib.core.api.STWApplicationStateListener;
import com.streamwide.smartms.lib.core.api.call.IncomingSessionsListener;
import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.data.item.VoipSessionItem;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;
import com.stw.protorype.configuration.ConfigurationManager;
import com.stw.protorype.ui.activity.calls.VoipCallNotification;

import androidx.multidex.MultiDexApplication;

public class MainApplication extends MultiDexApplication implements STWApplicationStateListener {

    private static MainApplication mInstance;
    private boolean mIsCallEventsRegistered;

    /**
     * Listener to incoming sessions events (Incoming call, Missed call)
     */
    /**
     * Listener for incoming VoIP sessions
     */
    private IncomingSessionsListener mIncomingSessionsListener = new IncomingSessionsListener() {
        @Override
        public void onReceiveIncomingCall(STWVCall call) {

            /**
             * Show notification when receiving a call
             */
            VoipCallNotification voipCallNotification = new VoipCallNotification(MainApplication.this, call.getSessionIdentifier());
            voipCallNotification.showNotification(getBaseContext());
        }

        @Override
        public void onReceiveMissedCall(VoipSessionItem voipSessionItem) {

            if (voipSessionItem == null) {
                Log.i("MainApplication", "onReceiveMissedCall : voipSessionItem is NULL !!!");

                return;
            }
            String callTypeLabel = "";
            int callType = voipSessionItem.getSessionType();
            int mediaCallType = voipSessionItem.getSessionMediaType();

            if (callType == VoipSessionItem.SESSION_TYPE_POC) {

                callTypeLabel = "PTT Call";

            } else if (callType == VoipSessionItem.SESSION_TYPE_LIVE
                    && mediaCallType == VoipSessionItem.SESSION_MEDIA_AUDIO) {

                callTypeLabel = "Live Call";

            } else if (callType == VoipSessionItem.SESSION_TYPE_LIVE
                    && mediaCallType == VoipSessionItem.SESSION_MEDIA_AUDIO_VIDEO) {

                callTypeLabel = "Video Call";

            } else if (callType == VoipSessionItem.SESSION_TYPE_LIVE_STREAM) {

                callTypeLabel = "Streaming";
            }

            else if (voipSessionItem.getSwTo().isEmpty() && !voipSessionItem.getSwOut().isEmpty()) {

                callTypeLabel = "Call Out";
            }

            /**
             * show a simple Toast to indicate that you have missed a call
             */
            Toast.makeText(MainApplication.this, "You missed "+callTypeLabel+" from "+ voipSessionItem.getCaller(), Toast.LENGTH_SHORT).show();
        }
    };
    @Override
    public void onCreate() {

        mInstance = this;
        ConfigurationManager.getInstance().initSDKConfigurations(this, this);

        super.onCreate();

    }

    public static MainApplication getInstance()
    {
        return mInstance;
    }

    @Override
    public void onApplicationStarted() {
        registerForVoipCallEvents();
    }

    @Override
    public void onApplicationStopped() {
        unregisterFromVoipCall();
    }

    public void reeinitConfiguration(){
        ConfigurationManager.getInstance().initSDKConfigurations(this, this);
    }

    /**
     * register for VoIP call events
     */
    protected void registerForVoipCallEvents()
    {
        if (mIsCallEventsRegistered) {
            /**
             * if already registered to the session listeners, must unregister first
             */
            STWCallManager.getInstance().unregisterForCallEvents(mIncomingSessionsListener, null,
                    null, null);
        }


        STWCallManager.getInstance().registerForCallEvents(mIncomingSessionsListener, null,
                null, null, null);


        mIsCallEventsRegistered = true;
    }

    /**
     * unregister from VoIP call events
     */
    protected void unregisterFromVoipCall()
    {
        if (mIsCallEventsRegistered) {
            STWCallManager.getInstance().unregisterForCallEvents(mIncomingSessionsListener, null,
                    null, null);
            mIsCallEventsRegistered = false;
        }
    }
}
