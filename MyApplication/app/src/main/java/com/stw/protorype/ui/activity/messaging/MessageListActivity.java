/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 10 Jan 2020 16:59:24 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 10 Jan 2020 16:52:59 +0100
 */

package com.stw.protorype.ui.activity.messaging;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.messaging.CompletionCallback;
import com.streamwide.smartms.lib.core.api.messaging.IAttachmentLoaderCallback;
import com.streamwide.smartms.lib.core.api.messaging.IConversationObservable;
import com.streamwide.smartms.lib.core.api.messaging.ILoadMessages;
import com.streamwide.smartms.lib.core.api.messaging.MessagingError;
import com.streamwide.smartms.lib.core.api.messaging.STWAttachmentManager;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.api.messaging.item.DeliveryReport;
import com.streamwide.smartms.lib.core.data.item.BaseAttachment;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.adapter.MessageAdapter;
import com.stw.protorype.ui.activity.messaging.utils.ConversationUtils;
import com.stw.protorype.ui.activity.messaging.utils.MessageUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


//this activity will implement IConversationObservable to listen to all actions related to this conversation
public class MessageListActivity extends AppCompatActivity implements IConversationObservable {

    private MessageAdapter adapter;
    private RecyclerView recyclerView;
    private EditText messageText;
    private Toolbar toolbar;

    //current conversation Id
    private String currentConversationId;

    //The offset of the first message to load from database
    private int offset = 0;
    // The max number of messages to be loaded from database starting from offset
    private int limit = 5;
    //The number of all messages in local database
    private int messagesCount;


    //in case of a new conversation
    private String[] recipients;
    private String conversationName;
    private int conversationType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        initView();
        initData();
        initScrollListener();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister to the register conversation callback before destroying the activity
        STWMessagingManager.getInstance().unregisterToConversation(currentConversationId, this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if new message exists, send mark as read request.
        int numberOfUnReadMessage = STWMessagingManager.getInstance().getNumberOfUnreadMessageInConversation(this, currentConversationId);
        if (numberOfUnReadMessage > 0) {
            STWMessagingManager.getInstance().readAllMessagesOfConversation(this, currentConversationId, null);
        }
    }

    private void initData() {


        if (getIntent().hasExtra(ConversationUtils.EXTRA_THREAD_ID)) {
            //The conversation already exists
            currentConversationId = getIntent().getStringExtra(ConversationUtils.EXTRA_THREAD_ID);
            // Register to all events related to currentConversationId
            STWMessagingManager.getInstance().registerToConversation(currentConversationId, this);

        }


        conversationType = getIntent().getExtras().getInt(ConversationUtils.EXTRA_CONVERSATION_TYPE);

        if (getIntent().hasExtra(ConversationUtils.EXTRA_CONVERSATION_NAME)) {
            //the conversation name
            conversationName = getIntent().getStringExtra(ConversationUtils.EXTRA_CONVERSATION_NAME);

        }

        if (getIntent().hasExtra(ConversationUtils.EXTRA_RECIPIENT)) {
            recipients = getIntent().getStringArrayExtra(ConversationUtils.EXTRA_RECIPIENT);
        }


        // Retrieve conversation Name
        getConversationName();


        initListMessage();


        /*
         * This method called only on the first opening of message Activity.
         * to synchronize oldest messages of conversation.
         */
        STWMessagingManager.getInstance().loadMessages(this, currentConversationId, new ILoadMessages() {
            @Override
            public void onStart() {
                //Start loading view
            }

            @Override
            public void onSuccess() {
                //Retrieve list of messages
            }

            @Override
            public void onError(String errorMessage) {
                //Add error view

            }
        });


    }

    private void getConversationName() {
        if (currentConversationId == null) {
            //it is a new conversation
            switch (conversationType) {
                case ThreadItem.THREAD_TYPE_ONE_TO_ONE: {
                    //if is One to One conversation
                    //display the name of the recipient
                    PhoneItem phone = STWContactManager.getInstance().getPhoneByNumber(this, recipients[0]);
                    String dislayName = STWContactManager.getInstance().getDisplayName(this, phone);
                    toolbar.setTitle(dislayName == null ? "" : dislayName);
                    break;
                }
                case ThreadItem.THREAD_TYPE_ONE_TO_MANY:
                case ThreadItem.THREAD_TYPE_GROUP: {
                    //if is group conversation
                    //display the name of the conversation
                    toolbar.setTitle(conversationName);

                }
                break;
            }

            return;
        }


        //it  is an existing conversation
        ThreadItem conversation = STWMessagingManager.getInstance().getConversationById(this, currentConversationId);
        if (conversation.getThreadType() == ThreadItem.THREAD_TYPE_GROUP || conversation.getThreadType() == ThreadItem.THREAD_TYPE_ONE_TO_MANY) {
            //if is group conversation
            //display the conversation name
            toolbar.setTitle(conversation.getGroupName());
        } else {
            //otherwise, display recipient name
            toolbar.setTitle(conversation.getThreadParticipantName());
        }
    }

    private void initListMessage() {

        if (currentConversationId == null)
            //it is a new conversation
            //No message to be displayed
            return;

        //Get number of message in local dataBase
        messagesCount =
                STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, currentConversationId);

        //set offset
        if (messagesCount >= limit) {
            offset = messagesCount - limit;
        } else {
            limit = messagesCount;
            offset = 0;
        }

        //Retrieve 5(limit) messages starting from offset
        List<BaseMessage> messages = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, currentConversationId, offset, limit);
        adapter.refreshData(messages);
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new MessageAdapter(MessageListActivity.this, null);
        recyclerView.setAdapter(adapter);

        Button sendButton = (Button) findViewById(R.id.sendButton);
        messageText = (EditText) findViewById(R.id.messageText);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageToSend = messageText.getText().toString();
                if (!messageToSend.isEmpty()) {
                    //if the text is not empty send the new message
                    sendMessage(messageToSend, null);
                }
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onclick on toolbar show list of recipient
                showListRecipient();
            }
        });

        Button pickImageButton = (Button) findViewById(R.id.pickImageButton);
        pickImageButton.setOnClickListener(v -> MessageUtils.selectImage(MessageListActivity.this));

    }


    private void showListRecipient() {

        if (currentConversationId == null || currentConversationId.isEmpty()) {
            //it is a new conversation
            //No recipient to show
            return;
        }

        ThreadItem threadItem = STWMessagingManager.getInstance().getConversationById(this, currentConversationId);
        if (threadItem.getThreadType() == ThreadItem.THREAD_TYPE_ONE_TO_ONE) {
            //if is one to one conversation
            //No need to display a list
            return;
        }


        //create a popup containing list of recipients
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("List of recipient");

        //Retrieve list of single contact
        List<ContactItem> contactItems = STWMessagingManager.getInstance().getGroupsInConversation(this, currentConversationId);
        List<PhoneItem> phoneItems = STWMessagingManager.getInstance().getUsersInConversation(this, currentConversationId);
        if (phoneItems != null) {
            for (PhoneItem phone : phoneItems) {
                ContactItem contactByPhone = STWContactManager.getInstance().getSingleContactByPhoneItem(this, phone);
                if (contactItems == null) {
                    contactItems = new ArrayList<>();
                }
                contactItems.add(contactByPhone);
            }
        }


        List<String> contactsNames = new ArrayList<>();

        Iterator<ContactItem> contactIterator = contactItems.iterator();

        while (contactIterator.hasNext()) {
            ContactItem contact = contactIterator.next();

            if (contact == null)
                contactIterator.remove();
            else {
                contactsNames.add(contact.getContactName());
            }
        }

        String[] contactArray = contactsNames.toArray(new String[contactsNames.size()]);

        //Show popup with names of contacts
        builder.setItems(contactArray, null);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Invite Contact", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MessageUtils.inviteUser(MessageListActivity.this, currentConversationId);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void sendMessage(String messageToSend, BaseAttachment attachment) {
        if (currentConversationId != null) {
            //it is an existing conversation
            sendMessageInExistingConversation(messageToSend, attachment);

        } else {
            //it is a new conversation

            if (recipients == null) {
                //No recipient found
                return;
            }

            sendMessageInNewConversation(messageToSend, attachment);

        }
    }


    private void sendMessageInNewConversation(String messageToSend, BaseAttachment attachment) {
        //Sending a new message and creating a new conversation by defining
        // list of recipient,
        // the type f the conversation,
        // the name of the conversation(in case of a group conversation)
        //..
        STWMessagingManager.getInstance().sendMessage(messageToSend, attachment, recipients, conversationType, conversationName, false, false, BaseMessage.BEARER_DEFAULT, new CompletionCallback() {
                    @Override
                    public void onError(MessagingError a) {

                    }

                    @Override
                    public void onCompletion(ThreadItem threadItem, BaseMessage message) {
                        //The message has been successfully sent
                        //Update the new conversation id
                        //Register to IConversationObservable
                        // an event will be invoked when the message is successfully sent

                        if (currentConversationId == null) {
                            //assign the currentConversationId
                            currentConversationId = threadItem.getId();
                            //register to IConversationObservable
                            STWMessagingManager.getInstance().registerToConversation(currentConversationId, MessageListActivity.this);
                        }

                    }


                }
        );

    }

    private void sendMessageInExistingConversation(String messageToSend, BaseAttachment attachment) {
        //Sending a new message to existing conversation
        STWMessagingManager.getInstance().sendMessage(messageToSend, attachment, currentConversationId, false, BaseMessage.BEARER_DEFAULT, new CompletionCallback() {
            @Override
            public void onError(MessagingError a) {

            }

            @Override
            public void onCompletion(ThreadItem threadItem, BaseMessage message) {
                //The message has been successfully sent
                //Update UI

            }
        });
    }

    private void initScrollListener() {
        //Load more messages
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (linearLayoutManager == null) {
                        return;
                    }

                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItemPosition <= 0) {
                        loadMore();
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });


    }

    private void loadMore() {
        //Check if the current thread has more messages to be loaded from server or not
        boolean hasMoreMessage = STWMessagingManager.getInstance().hasMoreMessages(MessageListActivity.this, currentConversationId);
        if (!hasMoreMessage) {
            //There is no more messages t be loaded from server
            //configure offset and limit
            if (offset > 0) {
                offset = offset - limit;
                if (offset < 0) {
                    offset = 0;
                    limit = messagesCount - adapter.getItemCount() - 1;
                }

                //retrieve (limit) list of messages starting from offset
                List<BaseMessage> moreMessages = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, currentConversationId, offset, limit);
                //update UI
                if (moreMessages != null)
                    insertElements(moreMessages, 0);

            }
        } else {

            //There is more messages to be loaded from server
            STWMessagingManager.getInstance().loadMoreMessages(this, currentConversationId, new ILoadMessages() {
                @Override
                public void onStart() {
                    //UI
                    Log.e("load more", "on start");
                    adapter.isLoading(true);

                }

                @Override
                public void onSuccess() {

                    Log.e("load more", "on onSuccess");

                    if (adapter.isLoading()) {
                        adapter.isLoading(false);
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {

                            messagesCount = STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, currentConversationId);

                            if (offset > 0) {
                                offset = offset - limit;
                                if (offset < 0) {
                                    offset = 0;
                                    limit = messagesCount - adapter.getItemCount() - 1;
                                }

                                List<BaseMessage> moreMessages = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, currentConversationId, offset, limit);
                                if (moreMessages != null)
                                    insertElements(moreMessages, 0);

                            }

                        }
                    });

                }

                @Override
                public void onError(String errorMessage) {

                    Log.e("load more", "on onError" + errorMessage);
                    if (adapter.isLoading()) {
                        adapter.isLoading(false);
                    }
                }
            });
        }
    }


    public void insertElements(List<BaseMessage> messageList, int positionStart) {
        adapter.insertData(messageList, positionStart);
    }

    public void insertElement(BaseMessage message, int positionStart) {
        adapter.insertData(message, positionStart);
    }

    public void removeElement(int positionStart) {
        adapter.removeItem(positionStart);
    }

    @Override
    public void onMessageSendingInProgress(String conversationId, String messageId) {
        //This event is invoked when a new message is sent.

        Log.e("test ", "test");
        //Update UI
        BaseMessage message = STWMessagingManager.getInstance().getMessageById(this, messageId);
        messageText.setText("");
        insertElement(message, adapter.getItemCount());
        getConversationName();
    }

    @Override
    public void onMessageDelivered(String conversationId, String messageId) {

        //This event is invoked when the message is successfully delivered to the backend
        //Update UI

    }

    @Override
    public void onMessageFail(String conversationId, String messageId, int error) {

        //This event is invoked when a delivery failure message has been received from the backend
        //Update UI

    }

    @Override
    public void onDeliveryReport(String conversationId, String messageId, DeliveryReport deliveryReport) {
        //This event is invoked when a delivery message report is received from the recepient
        //Update UI
    }

    @Override
    public void onMessageReadReceipt(String internationalPhoneNumber, List<String> messageIdList) {

        //This event is invoked when a read receipt is received  for a display message
        //Update UI
    }

    @Override
    public void onMessageReadReceived(List<String> messageIdList, List<String> voipSessionIdList) {
        //This event is invoked when the display message has been read from webchat
        //Update UI
    }

    @Override
    public void onAckMessageReceived(String internationalPhoneNumber, List<String> messageIdList) {

    }

    @Override
    public void onMessageDeleted(String conversationId, BaseMessage deletedMessage, int deletedMessagePosition) {
        //this event is invoked when a message belongs to this conversation is deleted locally or from the backend
        //update UI
        removeElement(deletedMessagePosition);
    }

    @Override
    public void onMessageUpdated(@NonNull String s, @NonNull String s1) {

    }

    @Override
    public void onNewMessageReceived(String conversationId, BaseMessage message, String internationalNumber, boolean isNoNotification) {
        //this event is invoked when a new message belongs to this conversation is received
        //update UI
        insertElement(message, adapter.getItemCount());
    }

    @Override
    public void onInvitationReceived(String conversationId, List<String> userIdList, List<Long> groupIdList, String internationalNumberSender) {
        //this event is invoked when a user is invited to this conversation
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the last message
                messagesCount = STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, conversationId);
                List<BaseMessage> message = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, conversationId, messagesCount - 1, 1);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //update UI
                        insertElement(message.get(0), adapter.getItemCount());

                    }
                });
            }
        }).start();
    }

    @Override
    public void onInvitationCompleted(String conversationId, List<String> phoneList, List<Long> groupIdList) {

        //Get the last message
        messagesCount = STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, conversationId);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                List<BaseMessage> message = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, conversationId, messagesCount - 1, 1);
                //update UI
                insertElement(message.get(0), adapter.getItemCount());

            }
        });

    }

    @Override
    public void onInvitationFailed(String conversationId) {

    }

    @Override
    public void onLeaveConversationReceived(String conversationId, String internationalNumber) {
        //this event is invoked when a user left this conversation

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the last message
                messagesCount = STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, conversationId);
                List<BaseMessage> message = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, conversationId, messagesCount - 1, 1);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //update UI
                        insertElement(message.get(0), adapter.getItemCount());

                    }
                });
            }
        }).start();

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
    public void onNewAdministratorsAssigned(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list, @NonNull @org.jetbrains.annotations.NotNull String s1) {

    }

    @Override
    public void onAssignRegularAdminsFailed(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list) {

    }

    @Override
    public void onAssignRegularAdminsCompleted(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list) {

    }

    @Override
    public void onAdministratorsRemoved(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list, @NonNull @org.jetbrains.annotations.NotNull String s1) {

    }

    @Override
    public void onRemoveRegularAdminsCompleted(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list) {

    }

    @Override
    public void onRemoveRegularAdminsFailed(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull List<String> list) {

    }

    @Override
    public void onCloseConversationReceived(@NonNull @org.jetbrains.annotations.NotNull String s, @NonNull @org.jetbrains.annotations.NotNull String s1) {

    }

    @Override
    public void onCloseConversationCompleted(@NonNull @org.jetbrains.annotations.NotNull String s) {

    }

    @Override
    public void onCloseConversationFailed(@NonNull @org.jetbrains.annotations.NotNull String s) {

    }

    @Override
    public void onDeleteConversationReceived(String conversationId) {

    }

    @Override
    public void onConversationNameChanged(String conversationId, String oldConversationName, String newConversationName) {
        //this event is invoked when name of this group conversation has been changed

        if (newConversationName == null)
            return;

        if (oldConversationName != null && oldConversationName.equals(newConversationName)) {
            return;
        }

        //Retrieve last message id using getLastMessageId
        String messageId = STWMessagingManager.getInstance().getLastMessageId(this, conversationId);
        BaseMessage message = STWMessagingManager.getInstance().getMessageById(this, messageId);
        //UpdateUI
        insertElement(message, adapter.getItemCount());


        //update toolbar title
        toolbar.setTitle(newConversationName);
    }

    @Override
    public void onConversationListReceived() {

    }

    @Override
    public void onMessageListReceived(String conversationId) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get the last message
                messagesCount = STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, conversationId);
                limit = messagesCount;
                offset = 0;
                List<BaseMessage> messages = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, currentConversationId, offset, limit);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //update UI
                        adapter.refreshData(messages);
                    }
                });
            }
        }).start();

    }

    @Override
    public void onVoiceMessagePlayedReceived(List<String> messageIdList) {

    }

    @Override
    public void onMessageDeletionPeriodicallyTaskFinished() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                //Get number of message in local dataBase
                messagesCount =
                        STWMessagingManager.getInstance().getNumberOfMessagesInConversation(MessageListActivity.this, currentConversationId);

                //set offset
                if (messagesCount >= limit) {
                    offset = messagesCount - limit;
                } else {
                    limit = messagesCount;
                    offset = 0;
                }

                //Retrieve 5(limit) messages starting from offset
                List<BaseMessage> messages = STWMessagingManager.getInstance().getMessages(MessageListActivity.this, currentConversationId, offset, limit);
                adapter.refreshData(messages);
            }
        });

    }

    @Override
    public void onRemoveFromConversationReceived(String s, String s1, List<String> list, @Nullable @org.jetbrains.annotations.Nullable List<String> list1) {

    }


    //get images from gallery or camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MessageUtils.PICK_IMAGE_CAMERA || requestCode == MessageUtils.PICK_IMAGE_GALLERY) {
            try {
                // load the image
                STWAttachmentManager.getInstance().loadImageAttachment(this, data, null, false, new IAttachmentLoaderCallback() {
                    @Override
                    public void onError(MessagingError messagingError) {

                    }

                    @Override
                    public void onComplete(BaseAttachment attachment) {
                        //Send the attachment
                        sendMessage("", attachment);
                    }


                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == MessageUtils.REQUEST_CAMERA_CODE) {
            MessageUtils.selectImage(MessageListActivity.this);
        }
    }
}
