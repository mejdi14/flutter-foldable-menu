/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on lun., 30 déc. 2019 18:24:59 +0100
 * @copyright  Copyright (c) 2019 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2019 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 30 déc. 2019 15:44:47 +0100
 */

package com.stw.protorype.ui.activity;

import com.streamwide.smartms.lib.core.api.account.STWAccountError;
import com.streamwide.smartms.lib.core.api.account.STWAccountManager;
import com.streamwide.smartms.lib.core.api.account.STWAccountSessionCallback;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.account.AccountActivity;
import com.stw.protorype.ui.activity.calls.VoipCallServicesActivity;
import com.stw.protorype.ui.activity.contact.ContactListActivity;
import com.stw.protorype.ui.activity.geolocation.GeolocationActivity;
import com.stw.protorype.ui.activity.loneworker.LoneWorkerActivity;
import com.stw.protorype.ui.activity.messaging.ConversationListActivity;
import com.stw.protorype.ui.activity.mybusiness.MyBusinessActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String CLASS_NAME = "MainActivity";
    private Button mConnectionState;
    private Button mEmergencyButton;

    private Button mMessagingButton;

    @Override
    protected void onResume()
    {
        STWAccountManager.getInstance().registerToAccountSessionState(mSTWAccountSessionCallback);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * register to service connection state
         */
        mConnectionState = findViewById(R.id.connection_state);

        /**
         * Contacts
         */
        Button contactsButton = findViewById(R.id.action_contacts_button);
        contactsButton.setOnClickListener(this);

        /**
         * Go to Account activity
         */
        Button accountButton = findViewById(R.id.action_account_button);
        accountButton.setOnClickListener(this);

        /**
         * go to Lone worker
         */
        Button loneWorkerButton = findViewById(R.id.action_lone_worker_button);
        loneWorkerButton.setOnClickListener(this);

        /**
         * go to VoIP Calls
         */
        Button voipCallsButton = findViewById(R.id.action_voip_calls_button);
        voipCallsButton.setOnClickListener(this);

        /**
         * go to Geolocation
         */
        Button geolocationButton = findViewById(R.id.action_geolocation_button);
        geolocationButton.setOnClickListener(this);

        /**
         * Go to Emergency activity
         */
        mEmergencyButton = findViewById(R.id.action_emergency_button);

        /**
         * go to My Business
         */
        Button myBusinessButton = findViewById(R.id.action_my_business_button);
        myBusinessButton.setOnClickListener(this);

        initEvent();

    }

    private void initEvent()
    {

        mEmergencyButton.setOnClickListener(this);


        /**
         * Messaging
         */
        mMessagingButton = findViewById(R.id.action_messaging_button);
        mMessagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ConversationListActivity.class);
                startActivity(i);
            }
        });

    }

    /**
     * Monitor Account session state
     */
    private STWAccountSessionCallback mSTWAccountSessionCallback = new STWAccountSessionCallback() {
        @Override
        public void onConnecting() {
            mConnectionState.setText("Connecting ....");
        }

        @Override
        public void onConnected() {
            mConnectionState.setText("Connected");
        }

        @Override
        public void onSessionEnded() {
            mConnectionState.setText("disconnected");
        }

        @Override
        public void onError(STWAccountError stwAccountError) {

        }
    };

    @Override
    public void onClick(View v)
    {
        if (R.id.action_contacts_button == v.getId()) {
            Intent intent = new Intent(this, ContactListActivity.class);
            startActivity(intent);
        } else if (R.id.action_account_button == v.getId()) {
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
        } else if (R.id.action_lone_worker_button == v.getId()) {
            startActivity(new Intent(MainActivity.this, LoneWorkerActivity.class));
        } else if (R.id.action_emergency_button == v.getId()) {
            Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
            startActivity(intent);
        } else if (R.id.action_voip_calls_button == v.getId()) {
            startActivity(new Intent(MainActivity.this, VoipCallServicesActivity.class));
        } else if (R.id.action_geolocation_button == v.getId()) {
            startActivity(new Intent(MainActivity.this, GeolocationActivity.class));
        }else if (R.id.action_my_business_button == v.getId()){
            startActivity(new Intent(MainActivity.this, MyBusinessActivity.class));
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }


}
