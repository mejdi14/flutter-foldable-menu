/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on lun., 6 janv. 2020 11:47:30 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 6 janv. 2020 11:47:30 +0100
 */

package com.stw.protorype.ui.activity.calls;


import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.api.call.SessionStateListener;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.VoipSessionItem;
import com.streamwide.smartms.lib.core.network.voip.STWPTTCall;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;
import com.stw.protorype.R;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class VoipCallNotification {
    private static final String CLASS_NAME = "VoipCallNotification";
    private static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private static final String NOTIFICATION_RINGING_CHANNEL_ID = "10002" ;
    private static final int NOTIFICATION_ID = ( int ) System. currentTimeMillis () ;

    /**
     * VoIP session id
     */
    private String mCallId;

    /**
     * VoIP session object
     */
    private STWVCall mVCall;

    private SessionStateListener mSessionStateListener;
    private boolean mIsCallEventsRegistered;


    public VoipCallNotification(Context context, String callId) {
        mCallId = callId;

        initData();

        initEvent(context);

    }

    private void initEvent(Context context) {

        /**
         * Listener for session state (CALLING,IN_CALL, IDLE, connecting...)
         */
        mSessionStateListener = new SessionStateListener() {
            @Override
            public void calling(STWVCall vCall) {
                Log.d(CLASS_NAME, "SessionStateListener : calling");
                if(!mVCall.isSessionOutgoing()) {
                    cancelNotification(context);
                }
            }

            @Override
            public void onSessionStarted(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : calling");
            }

            @Override
            public void inCall(STWVCall vCall) {
                Log.d(CLASS_NAME, "SessionStateListener : inCall");
            }

            @Override
            public void onSessionConnected(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : connected");
            }

            @Override
            public void onSessionReconnecting(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : reconnecting");
            }

            @Override
            public void onAllSessionsReconnecting() {
                Log.d(CLASS_NAME, "SessionStateListener : reconnecting");
            }

            @Override
            public void onSessionOnHold(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : onhold");
            }

            @Override
            public void onSessionResumed(STWVCall sessionHolder) {
                Log.d(CLASS_NAME, "SessionStateListener : resumed");
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
            public void onSessionStopped(VoipSessionItem voipSessionItem) {
                Log.d(CLASS_NAME, "SessionStateListener : stopped");
                cancelNotification(context);
            }

            @Override
            public void onVoipSessionClosed(VoipSessionItem voipSessionItem) {
                Log.d(CLASS_NAME, "SessionStateListener : Voip session closed");
                cancelNotification(context);
            }

            @Override
            public void onSessionAnsweredFromOtherDevice(String sessionIdentifier) {
                Log.d(CLASS_NAME, "SessionStateListener : Session Answered From Other Device");
                cancelNotification(context);
            }

            @Override
            public void onAudioOutputChanged() {

            }
        };

        registerForVoipCallEvents();
    }

    /**
     * cancel notification
     * @param context
     */
    private void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE ) ;
        notificationManager.cancel(mCallId, NOTIFICATION_ID);

        unregisterFromVoipCall();

    }

    private void initData() {
        mVCall = STWCallManager.getInstance().getCallWithID(mCallId);
    }

    /**
     *
     * @param context
     */
    public void showNotification(Context context) {

        /**
         * call type info
         */
        String callTypeLabel = "";
        switch (mVCall.getCurrentCallType()) {
            case STWVCall.STWVCallType.STWVCallLiveCallType : {
                callTypeLabel = "Live Call";
            }
            break;
            case STWVCall.STWVCallType.STWVCallCallOutCallType : {
                callTypeLabel = "Call Out";
            }
            break;
            case STWVCall.STWVCallType.STWVCallLiveVideoCallType : {
                callTypeLabel = "Video Call";
            }
            break;
            case STWVCall.STWVCallType.STWVCallPTTCallType : {
                if (((STWPTTCall)mVCall).isChannel()) {
                    callTypeLabel = "Channel";
                } else {
                    callTypeLabel = "PTT Call";
                }
            }
            break;
            case STWVCall.STWVCallType.STWVCallLiveStreamCallType : {
                callTypeLabel = "Streaming";
            }
            break;
        }

        boolean isRingingSession = !mVCall.isSessionOutgoing() && mVCall.getCallStatus() == STWVCall.STWVCallConnectionStatus.STWVCallRingingStatus;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context , isRingingSession ? NOTIFICATION_RINGING_CHANNEL_ID : NOTIFICATION_CHANNEL_ID);
        /**
         * make notification not cancelable
         */
        mBuilder.setAutoCancel(false);
        mBuilder.setSmallIcon(R.drawable.ic_call_notification);

        mBuilder.setUsesChronometer(true);
        mBuilder.setWhen(mVCall.getStartCallTime());


        Log.d(CLASS_NAME, "showNotification : call type = "+callTypeLabel+" - is outgoing session = "+mVCall.isSessionOutgoing() + " - need play ringtone = "+isRingingSession);

        /**
         * incoming session in ringing state
         */
        if(isRingingSession) {
            mBuilder.setContentTitle("Incoming " +callTypeLabel + " from "+getCallerName(context, mVCall.getCallerPhoneItem())) ;

            /**
             * make notification fullscreen
             */
            mBuilder.setFullScreenIntent(null, true);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

            /**
             * add ACCEPT and REFUSE actions
             */
            mBuilder.addAction(-1 , "Accept" , getActionPendingIntent(context, NotificationBroadcastReceiver.ACCEPT_ACTION)) ;
            mBuilder.addAction(-1 , "Refuse" , getActionPendingIntent(context, NotificationBroadcastReceiver.REFUSE_ACTION)) ;

            if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_RINGING_CHANNEL_ID , "RINGING CALL" , NotificationManager.IMPORTANCE_HIGH) ;

                AudioAttributes att = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
                notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), att);


                mBuilder.setChannelId( NOTIFICATION_RINGING_CHANNEL_ID ) ;
                notificationManager.createNotificationChannel(notificationChannel) ;
            } else {
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), AudioManager.STREAM_RING);
            }

        }
        /**
         * incoming started session or outgoing session
         */
        else {
            mBuilder.setContentTitle(callTypeLabel + " in progress") ;


            /**
             * participant info
             */
            if(mVCall.getCurrentCallType() == STWVCall.STWVCallType.STWVCallPTTCallType && ((STWPTTCall)mVCall).isChannel()) {
                String groupID = mVCall.getSessionIdentifier().replace("group:", "");
                ContactItem contactItem = STWContactManager.getInstance().getContactByGroupId(context, groupID);
                String channelName = STWContactManager.getInstance().getDisplayNameForContactItem(context, contactItem);
                mBuilder.setContentText(channelName);
            } else {
                mBuilder.setContentText(mVCall.getParticipantNames());
            }

            mBuilder.setPriority(NotificationCompat.PRIORITY_LOW);

            /**
             * add STOP action
             */
            mBuilder.addAction(-1 , "Stop" , getActionPendingIntent(context, NotificationBroadcastReceiver.STOP_ACTION)) ;

            /**
             * redirect user to Call screen activity
             */
            mBuilder.setContentIntent(getPendingIntent(context));


            if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
                NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "ONGOING CALL" , NotificationManager.IMPORTANCE_HIGH) ;
                notificationChannel.setSound(null, null);

                mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
                notificationManager.createNotificationChannel(notificationChannel) ;
            } else {
                mBuilder.setSound(null);
            }

        }

        notificationManager.notify(mCallId,NOTIFICATION_ID,  mBuilder.build());
    }

    /**
     * Retrieve the caller name from the phoneItem parameter
     *
     * @param context
     * @param phoneItem the phoneItem of the caller
     * @return The caller name
     */
    private String getCallerName(Context context, PhoneItem phoneItem) {
        String contactName = STWContactManager.getInstance().getDisplayName(context, phoneItem);

        if (TextUtils.isEmpty(contactName)) {
            contactName = phoneItem.getDisplayNumber();
        }

        return contactName;
    }

    /**
     *
     * @param context
     * @param action
     *          can be {@link NotificationBroadcastReceiver#ACCEPT_ACTION}
     *          {@link NotificationBroadcastReceiver#REFUSE_ACTION}
     *          {@link NotificationBroadcastReceiver#STOP_ACTION}
     * @return pending intent for action button notification
     */
    private PendingIntent getActionPendingIntent(Context context, String action) {
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction(action);
        Bundle bundle = new Bundle();
        bundle.putString(VoipCallServicesActivity.EXTRA_CALL_ID, mCallId);
        intent.putExtras(bundle);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, VoipCallScreenActivity.class);
        intent.putExtra(VoipCallServicesActivity.EXTRA_CALL_ID, mCallId);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }



    /**
     * register to VoIP call events
     */
    private void registerForVoipCallEvents()
    {
        if(TextUtils.isEmpty(mCallId)) {
            return;
        }

        if (mIsCallEventsRegistered) {
            STWCallManager.getInstance().unregisterForCallEvents(null, mSessionStateListener,
                    null, null);
        }

        STWCallManager.getInstance().registerForCallEvents(null, mSessionStateListener,
                null, null, mCallId);

        mIsCallEventsRegistered = true;
    }

    /**
     * unregister from VoIP call events
     */
    private void unregisterFromVoipCall()
    {
        if (mIsCallEventsRegistered) {
            STWCallManager.getInstance().unregisterForCallEvents(null, mSessionStateListener,
                    null, null);
            mIsCallEventsRegistered = false;
        }
    }

}
