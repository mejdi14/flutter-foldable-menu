/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on lun., 30 déc. 2019 10:30:04 +0100
 * @copyright  Copyright (c) 2019 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2019 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 30 déc. 2019 10:28:42 +0100
 */

package com.stw.protorype.ui.activity.messaging;

import java.util.List;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.streamwide.smartms.lib.core.api.messaging.IConversationObservable;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.api.messaging.item.DeliveryReport;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.adapter.ConversationAdapter;
import com.stw.protorype.ui.activity.messaging.utils.ConversationUtils;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ConversationListActivity extends AppCompatActivity implements IConversationObservable {

    private RecyclerView recyclerView;
    private ConversationAdapter adapter;

    private static final int ALL_CONVERSATION = 1;
    private static final int EXTERNAL_CONVERSATION = 2;
    private static final int COMPANY_CONVERSATION = 3;
    private int conversationType = ALL_CONVERSATION;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);

        // Register to all events coming from any conversation.
        STWMessagingManager.getInstance().registerToConversation(this);
        initView();
        initData();
        initFloatingButton();
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        //Refresh view
        List<ThreadItem> conversations = STWMessagingManager.getInstance().getAllConversations(this);
        refreshAllElements(conversations);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister to the previous register conversation callback before destroying the activity
        STWMessagingManager.getInstance().unregisterToConversation(this);

    }

    private void initData() {
        // Retrieve list of all conversations
        List<ThreadItem> conversations = STWMessagingManager.getInstance().getAllConversations(this);
        adapter = new ConversationAdapter(this, conversations);
        recyclerView.setAdapter(adapter);

        setToolbarTitle();

    }

    void setToolbarTitle() {
        switch (conversationType) {
            case ALL_CONVERSATION:
                toolbar.setTitle("All conversationss");
                break;
            case EXTERNAL_CONVERSATION:
                toolbar.setTitle("External conversations");
                break;
            case COMPANY_CONVERSATION:
                toolbar.setTitle("Company conversations");
                break;
        }
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }


    public void removeElement(int positionStart) {
        adapter.removeData(positionStart);
    }

    public void refreshAllElements(List<ThreadItem> threadItems) {
        adapter.refreshData(threadItems);

    }

    public void refreshElement(ThreadItem threadItem, int positionStart) {
        adapter.itemChanged(threadItem, positionStart);
    }


    /*============================================
     * List of events on all the conversations
     *============================================
     */

    @Override
    public void onMessageSendingInProgress(String conversationId, String messageId) {

        //This event is invoked when a new message is sent.
        //Update UI
        ThreadItem conversation = STWMessagingManager.getInstance().getConversationById(this, conversationId);
        int pos = adapter.getItemPosition(conversation);

        if (pos >= 0) {
            refreshElement(conversation, pos);
        }

    }

    @Override
    public void onMessageDelivered(String conversationId, String messageId) {

    }

    @Override
    public void onMessageFail(String conversationId, String messageId, int error) {

    }

    @Override
    public void onDeliveryReport(String conversationId, String messageId, DeliveryReport deliveryReport) {

    }

    @Override
    public void onMessageReadReceipt(String internationalPhoneNumber, List<String> messageIdList) {

    }

    @Override
    public void onMessageReadReceived(List<String> messageIdList, List<String> voipSessionIdList) {

    }

    @Override
    public void onAckMessageReceived(String internationalPhoneNumber, List<String> messageIdList) {

    }

    @Override
    public void onMessageDeleted(String conversationId, BaseMessage deletedMessage, int deletedMessagePosition) {

    }

    @Override
    public void onMessageUpdated(@NonNull String s, @NonNull String s1) {

    }

    @Override
    public void onNewMessageReceived(String conversationId, BaseMessage message, String internationalNumber, boolean isNoNotification) {
        //This event is invoked when a new message is received
        //if the conversation already exists, update UI
        //Otherwise, add new item to the list

        ThreadItem conversation = STWMessagingManager.getInstance().getConversationById(this, conversationId);
        int pos = adapter.getItemPosition(conversation);

        if (pos >= 0) {
            //Conversation exists
            refreshElement(conversation, pos);
        } else if (conversationType == ALL_CONVERSATION || conversationType == COMPANY_CONVERSATION) {
            //Conversation does not exist
            adapter.insertData(conversation, 0);
        }


    }

    @Override
    public void onInvitationReceived(String conversationId, List<String> userIdList, List<Long> groupIdList, String internationalNumberSender) {

    }

    @Override
    public void onInvitationCompleted(String conversationId, List<String> phoneList, List<Long> groupIdList) {

    }

    @Override
    public void onInvitationFailed(String conversationId) {

    }

    @Override
    public void onLeaveConversationReceived(String conversationId, String internationalNumber) {

    }


    @Override
    public void onLeaveConversationCompleted(String conversationId) {

    }

    @Override
    public void onLeaveConversationFailed(String conversationId) {

    }

    @Override
    public void onRemoveFromConversationFailed(@NonNull String s) {

    }

    @Override
    public void onRemoveFromConversationCompleted(@NonNull String s) {

    }

    @Override
    public void onNewAdministratorsAssigned( String s,  List<String> list,  String s1) {

    }

    @Override
    public void onAssignRegularAdminsFailed( String s,  List<String> list) {

    }

    @Override
    public void onAssignRegularAdminsCompleted( String s,  List<String> list) {

    }

    @Override
    public void onAdministratorsRemoved( String s,  List<String> list,  String s1) {

    }

    @Override
    public void onRemoveRegularAdminsCompleted( String s,  List<String> list) {

    }

    @Override
    public void onRemoveRegularAdminsFailed( String s,  List<String> list) {

    }

    @Override
    public void onCloseConversationReceived( String s,  String s1) {

    }

    @Override
    public void onCloseConversationCompleted( String s) {

    }

    @Override
    public void onCloseConversationFailed( String s) {

    }

    @Override
    public void onDeleteConversationReceived(String conversationId) {
        //this event is invoked when a conversation is deleted from the webchat
        //update UI


        ThreadItem conversation = STWMessagingManager.getInstance().getConversationById(this, conversationId);
        int pos = adapter.getItemPosition(conversation);

        if (pos >= 0) {
            removeElement(pos);
        }

    }

    @Override
    public void onConversationNameChanged(String conversationId, String oldConversationName, String newConversationName) {

        //This event is invoked when the name of group conversation has been changed
        // update UI
        ThreadItem conversation = STWMessagingManager.getInstance().getConversationById(this, conversationId);
        int pos = adapter.getItemPosition(conversation);
        if (pos >= 0) {
            refreshElement(conversation, pos);
        }

    }

    @Override
    public void onConversationListReceived() {

    }

    @Override
    public void onMessageListReceived(String conversationId) {
        //Do nothing
    }

    @Override
    public void onVoiceMessagePlayedReceived(List<String> messageIdList) {

    }

    @Override
    public void onMessageDeletionPeriodicallyTaskFinished() {

    }

    @Override
    public void onRemoveFromConversationReceived( String s,  String s1,  List<String> list,  List<String> list1) {
        
    }




    //initialise floating button options
    boolean isOpen = false;

    private void initFloatingButton() {
        FloatingActionButton fab_main = findViewById(R.id.fab);
        FloatingActionButton fab_send = findViewById(R.id.sendFab);
        FloatingActionButton fab_comp_conv = findViewById(R.id.compConvFab);
        FloatingActionButton fab_ext_comp = findViewById(R.id.extConvFab);
        FloatingActionButton fab_all_comp = findViewById(R.id.allConvFab);

        Animation fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        Animation fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        Animation fab_clock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_clock);
        Animation fab_anticlock = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_rotate_anticlock);

        TextView textview_send = (TextView) findViewById(R.id.sendText);
        TextView textview_comp_conv = (TextView) findViewById(R.id.compConvText);
        TextView textview_ext_conv = (TextView) findViewById(R.id.extConvText);
        TextView textview_all_conv = (TextView) findViewById(R.id.allConvText);

        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isOpen) {

                    textview_send.setVisibility(View.INVISIBLE);
                    textview_comp_conv.setVisibility(View.INVISIBLE);
                    textview_ext_conv.setVisibility(View.INVISIBLE);
                    textview_all_conv.setVisibility(View.INVISIBLE);

                    fab_send.startAnimation(fab_close);
                    fab_comp_conv.startAnimation(fab_close);
                    fab_ext_comp.startAnimation(fab_close);
                    fab_all_comp.startAnimation(fab_close);

                    fab_main.startAnimation(fab_anticlock);

                    fab_send.setClickable(false);
                    fab_comp_conv.setClickable(false);
                    fab_ext_comp.setClickable(false);
                    fab_all_comp.setClickable(false);

                    isOpen = false;
                } else {
                    textview_send.setVisibility(View.VISIBLE);
                    textview_comp_conv.setVisibility(View.VISIBLE);
                    textview_ext_conv.setVisibility(View.VISIBLE);
                    textview_all_conv.setVisibility(View.VISIBLE);

                    fab_send.startAnimation(fab_open);
                    fab_comp_conv.startAnimation(fab_open);
                    fab_ext_comp.startAnimation(fab_open);
                    fab_all_comp.startAnimation(fab_open);

                    fab_main.startAnimation(fab_clock);

                    fab_send.setClickable(true);
                    fab_comp_conv.setClickable(true);
                    fab_ext_comp.setClickable(true);
                    fab_all_comp.setClickable(true);

                    isOpen = true;
                }

            }
        });


        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversationUtils.chooseMessageType(ConversationListActivity.this);

                //send new Message

            }
        });

        fab_comp_conv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conversationType = COMPANY_CONVERSATION;
                ConversationUtils.getCompanyConversation(ConversationListActivity.this);
                setToolbarTitle();


            }
        });

        fab_ext_comp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conversationType = EXTERNAL_CONVERSATION;
                ConversationUtils.getExternalConversation(ConversationListActivity.this);
                setToolbarTitle();

            }
        });

        fab_all_comp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conversationType = ALL_CONVERSATION;
                ConversationUtils.getAllConversation(ConversationListActivity.this);
                setToolbarTitle();


            }
        });


    }
}
