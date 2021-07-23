/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 3 janv. 2020 09:15:12 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn jeu., 2 janv. 2020 18:01:19 +0100
 */

package com.stw.protorype.ui.activity.messaging.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;

import com.streamwide.smartms.lib.core.api.Error;
import com.streamwide.smartms.lib.core.api.STWOperationCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.messaging.MessagingError;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.ui.activity.geolocation.GeolocationActivity;
import com.stw.protorype.ui.activity.messaging.ConversationListActivity;
import com.stw.protorype.ui.activity.messaging.MessageListActivity;
import com.stw.protorype.ui.activity.messaging.MessagingContactListActivity;

import java.util.ArrayList;
import java.util.List;

import static com.stw.protorype.MainConstant.MESSAGING;


public class ConversationUtils {

    //List of options that could be done on conversations
    private static final String openConversationText = "Open conversation";
    private static final String deleteConversationText = "Delete conversation";
    private static final String changeGroupNameText = "Change group name";
    private static final String markAsReadText = "Mark as Read";

    //Extended data of intents
    public static final String EXTRA_THREAD_ID = "extra_thread_id";
    public static final String EXTRA_RECIPIENT = "extra_recipient";
    public static final String EXTRA_CONVERSATION_NAME = "extra_conversation_name";
    public static final String EXTRA_CONVERSATION_TYPE = "conversation_type";
    public static final String EXTRA_COMES_FROM = "comes_from";


    //Show list of options when clicking on conversation
    public static void createDialogConversationOption(ConversationListActivity context, ThreadItem threadItem, int position) {
        List<String> optionList = new ArrayList<>();
        optionList.add(openConversationText);
        optionList.add(deleteConversationText);
        if (threadItem.getThreadType() == ThreadItem.THREAD_TYPE_GROUP) {
            //Change conversation name in case of a group conversation.
            optionList.add(changeGroupNameText);
        }
        if (threadItem.getUnreadMgmCount() > 0) {
            //Send ReadMessage request, if the conversation has got unread messages.
            optionList.add(markAsReadText);
        }


        String[] optionArray = optionList.toArray(new String[optionList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select The Action");
        builder.setItems(optionArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                switch (optionArray[which]) {
                    case openConversationText: {
                        //open conversation
                        openConversation(context, threadItem.getId(), threadItem.getThreadType());

                    }
                    break;
                    case deleteConversationText: {
                        //delete conversation by id.
                        deleteConversation(context, threadItem, position);
                    }
                    break;
                    case changeGroupNameText: {
                        // change group name of conversation
                        renameConversation(context, threadItem, position);
                    }
                    break;
                    case markAsReadText: {
                        //Send readMessage request
                        markAsReadConversation(context, threadItem, position);
                    }
                    break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + optionArray[which]);
                }
            }

        });
        builder.show();
    }

    static void openConversation(Context context, String threadId, int conversationType) {
        Intent intent = new Intent(context, MessageListActivity.class);
        intent.putExtra(EXTRA_THREAD_ID, threadId);
        intent.putExtra(EXTRA_CONVERSATION_TYPE, conversationType);
        context.startActivity(intent);
    }

    private static void renameConversation(ConversationListActivity context, ThreadItem threadItem, int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage("Enter new group name");
        alert.setTitle("Change Group name");

        alert.setView(edittext);

        alert.setPositiveButton("Yes Option", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                String newGroupNameText = edittext.getText().toString();

                //Use changeGroupName method
                STWMessagingManager.getInstance().changeGroupName(context, threadItem.getId(), newGroupNameText, new STWOperationCallback<MessagingError>() {
                    @Override
                    public void onError(MessagingError error) {

                    }

                    @Override
                    public void onSuccess() {
                        //if the conversation name has been successfully updated
                        context.refreshElement(threadItem, position);
                    }
                });
            }
        });

        alert.setNegativeButton("No Option", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        alert.show();

    }

    private static void markAsReadConversation(ConversationListActivity context, ThreadItem threadItem, int position) {
        STWMessagingManager.getInstance().readAllMessagesOfConversation(context, threadItem.getId(), new STWOperationCallback<MessagingError>() {
            @Override
            public void onError(MessagingError error) {

            }

            @Override
            public void onSuccess() {
                //ReadMessage request is successfully sent
                //updated UI
                context.refreshElement(threadItem, position);
            }
        });
    }

    private static void deleteConversation(ConversationListActivity context, ThreadItem threadItem, int position) {
        STWMessagingManager.getInstance().deleteConversation(context, threadItem, new STWOperationCallback() {
            @Override
            public void onError(Error error) {

            }

            @Override
            public void onSuccess() {
                //The conversation is successfully deleted
                //update UI
                context.removeElement(position);
            }
        });
    }


    public static void chooseMessageType(Context context) {
        //Select the type of the new conversation
        final CharSequence[] items = {" One To One Conversation ", " Group Conversation ", " One To Many conversation "};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose message type");
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        //One to one conversation
                        chooseContactsConversation(context, null, ThreadItem.THREAD_TYPE_ONE_TO_ONE, MESSAGING);
                        break;
                    }
                    case 1: {
                        //Group conversation
                        addGroupNameToConversation(context, ThreadItem.THREAD_TYPE_GROUP);
                        break;
                    }
                    case 2: {
                        //One to many conversation
                        addGroupNameToConversation(context, ThreadItem.THREAD_TYPE_ONE_TO_MANY);

                        break;
                    }
                }
                dialog.dismiss();
            }

        });


        builder.create();
        builder.show();
    }


    public static void sendOneToOneConversation(Context context, ContactItem contactItem) {
        //Retrieve international number
        PhoneItem selectedPhoneItem = STWContactManager.getInstance().getBusinessPhone(context, contactItem);

        String internationalNumber = selectedPhoneItem.getInternationalNumber();

        //check if the conversation already exist or not
        ThreadItem conversation = STWMessagingManager.getInstance().getCompanyConversationByRecipientNumber(context, internationalNumber);

        Intent intent = new Intent(context, MessageListActivity.class);

        if (conversation == null) {

            //conversation does not exist, send the international phone number to start a new one to one conversation
            intent.putExtra(EXTRA_RECIPIENT, new String[]{internationalNumber});
        } else {
            //conversation exists, send conversation id and open the existing one to one conversation
            intent.putExtra(EXTRA_THREAD_ID, conversation.getId());
        }

        intent.putExtra(EXTRA_CONVERSATION_TYPE, ThreadItem.THREAD_TYPE_ONE_TO_ONE);
        context.startActivity(intent);

    }

    public static void sendOneToManyOrGroupConversation(Context context, String newGroupNameText, List<ContactItem> selectedContactList, int conversationType) {
        //Create a group conversation
        //Add list of users, groups and the conversation name
        Intent intent = new Intent(context, MessageListActivity.class);


        //prepare list of string of recipients from list of phone numbers and list of groups
        String[] recipients = prepareRecipientArrayString(context, selectedContactList);
        intent.putExtra(EXTRA_CONVERSATION_TYPE, conversationType);
        intent.putExtra(EXTRA_RECIPIENT, recipients);
        intent.putExtra(EXTRA_CONVERSATION_NAME, newGroupNameText);
        context.startActivity(intent);
    }



    public static void sendSelectedContactsToGeoloc(Context context, List<ContactItem> selectedContactList) {
        Intent intent = new Intent(context, GeolocationActivity.class);

        //prepare list of string of recipients from list of phone numbers and list of groups
        String[] recipients = prepareRecipientArrayString(context, selectedContactList);
        intent.putExtra(EXTRA_RECIPIENT, recipients);
        context.startActivity(intent);
    }




    private static void addGroupNameToConversation(Context context, int conversationType) {
        //Choose the name of the conversation
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        final EditText edittext = new EditText(context);
        alert.setMessage("Enter group name");
        alert.setTitle("Choose Group name");

        alert.setView(edittext);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newGroupNameText = edittext.getText().toString();
                if (!newGroupNameText.isEmpty()) {
                    //Choose list of contacts or groups to start a conversation with them
                    chooseContactsConversation(context, newGroupNameText, conversationType, MESSAGING);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

    }

    public static void chooseContactsConversation(Context context, String newGroupNameText, int conversationType, String comesFrom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose contacts");

        Intent intent = new Intent(context, MessagingContactListActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_TYPE, conversationType);
        intent.putExtra(EXTRA_CONVERSATION_NAME, newGroupNameText);
        intent.putExtra(EXTRA_COMES_FROM, comesFrom);
        context.startActivity(intent);
    }


    public static void getCompanyConversation(ConversationListActivity context) {
        //show only company conversations
        List<ThreadItem> threadItems = STWMessagingManager.getInstance().getCompanyConversations(context);
        context.refreshAllElements(threadItems);

    }

    public static void getExternalConversation(ConversationListActivity context) {
        //show only external conversations
        List<ThreadItem> threadItems = STWMessagingManager.getInstance().getExternalConversations(context);
        context.refreshAllElements(threadItems);

    }

    public static void getAllConversation(ConversationListActivity context) {
        //show all conversations
        List<ThreadItem> threadItems = STWMessagingManager.getInstance().getAllConversations(context);
        context.refreshAllElements(threadItems);

    }


    /**
     * @param context
     * @param selectedContactItem
     * @return array of string like : "[group:2,group:1,21699926249@smartms206.streamwide.com]"
     */
    private static String[] prepareRecipientArrayString(Context context, List<ContactItem> selectedContactItem) {


        int size = (selectedContactItem != null ? selectedContactItem.size() : 0);

        String[] users = new String[size];

        int n = 0;

        if (selectedContactItem != null) {
            for (ContactItem contactItem : selectedContactItem) {
                if (contactItem.isGroup()) {
                    users[n] = "group:" + contactItem.getGroupId();
                } else {
                    PhoneItem selectedPhoneItem = STWContactManager.getInstance().getBusinessPhone(context, contactItem);
                    users[n] = selectedPhoneItem.getInternationalNumber();
                }
                n++;

            }
        }

        return users;
    }

}

