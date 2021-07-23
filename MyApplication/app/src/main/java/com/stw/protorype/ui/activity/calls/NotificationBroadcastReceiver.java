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
 * @lastModifiedOn lun., 6 janv. 2020 11:44:24 +0100
 */

package com.stw.protorype.ui.activity.calls;

import com.streamwide.smartms.lib.core.api.call.CallError;
import com.streamwide.smartms.lib.core.api.call.CompletionCallback;
import com.streamwide.smartms.lib.core.api.call.STWCallManager;
import com.streamwide.smartms.lib.core.api.call.STWCallRefuseReason;
import com.streamwide.smartms.lib.core.network.voip.STWVCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "NotificationBroadcast";

    /**
     * Action received when accept call button is pressed
     */
    public static final String ACCEPT_ACTION = "ACCEPT";

    /**
     * Action received when refuse call button is pressed
     */
    public static final String REFUSE_ACTION = "REFUSE";

    /**
     * Action received when stop current active call button is pressed
     */
    public static final String STOP_ACTION = "STOP";


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null) {
            Log.w(CLASS_NAME, "intent is null !");
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Log.w(CLASS_NAME, "bundle is null !");
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.w(CLASS_NAME, "action is null !");
            return;
        }

        String  callId = bundle.getString("extra_call_id");
        if (TextUtils.isEmpty(callId)) {
            Log.w(CLASS_NAME, "callId is null !");
            return;
        }

        /**
         * retrieve the call object from the given id
         */
        STWVCall vCall = STWCallManager.getInstance().getCallWithID(callId);
        if (vCall == null){
            Log.w(CLASS_NAME, "call is null !");
            return;
        }

        if(ACCEPT_ACTION.equals(action)) {

            /**
             * accept incoming call
             */
            STWCallManager.getInstance().acceptCall(context, vCall, new CompletionCallback() {
                @Override
                public void onCompletion(STWVCall stwvCall) {
                    if (stwvCall == null) {
                        Toast.makeText(context, "An unknown error has occurred", Toast.LENGTH_LONG).show();

                        return;
                    }

                    /**
                     * show new notification for started VoIP call
                     */
                    VoipCallNotification voipCallNotification = new VoipCallNotification(context, callId);
                    voipCallNotification.showNotification(context);

                    /**
                     * show call screen
                     */
                    startCallScreen(context, callId);
                }

                @Override
                public void onError(CallError callError) {
                    Toast.makeText(context, "An error has occurred when starting the call. Error = "+callError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });



        } else if(REFUSE_ACTION.equals(action)) {

            /**
             * refuse incoming call
             */
            STWCallManager.getInstance().refuseVoipCall(context, vCall, STWCallRefuseReason.DECLINE);

        } else if(STOP_ACTION.equals(action)) {

            /**
             * stop current active call
             */
            STWCallManager.getInstance().stopCall(context, vCall);

        }
    }

    private void startCallScreen(Context context, String callId){
        Intent callScreenIntent = new Intent(context, VoipCallScreenActivity.class);
        callScreenIntent.putExtra(VoipCallServicesActivity.EXTRA_CALL_ID, callId);
        callScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(callScreenIntent);
    }
}
