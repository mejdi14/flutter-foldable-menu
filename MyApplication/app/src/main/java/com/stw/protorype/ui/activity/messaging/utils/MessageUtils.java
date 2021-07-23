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
 * @lastModifiedOn jeu., 2 janv. 2020 17:59:11 +0100
 */

package com.stw.protorype.ui.activity.messaging.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.streamwide.smartms.lib.core.api.Error;
import com.streamwide.smartms.lib.core.api.STWOperationCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.messaging.CompletionCallback;
import com.streamwide.smartms.lib.core.api.messaging.MessagingError;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.MessageListActivity;
import com.stw.protorype.ui.activity.messaging.adapter.DialogContactAdapter;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {

    //List of options that could be done on a message
    private static final String deleteMessageText = "Delete message";
    private static final String forwardText = "Forward message";


    public static final int PICK_IMAGE_CAMERA = 1;
    public static final int PICK_IMAGE_GALLERY = 2;
    public static final int REQUEST_CAMERA_CODE = 3;


    public static void createDialogMessageOption(MessageListActivity context, BaseMessage message, int position) {
        List<String> optionList = new ArrayList<>();
        optionList.add(deleteMessageText);
        optionList.add(forwardText);

        String[] optionArray = optionList.toArray(new String[optionList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select The Action");
        builder.setItems(optionArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (optionArray[which]) {
                    case deleteMessageText: {
                        //delete Message
                        deleteMessage(context, message.getId(), position);
                    }
                    break;
                    case forwardText: {
                        //forward message
                        forwardMessage(context, message);
                    }
                    break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + optionArray[which]);
                }
            }

        });
        builder.show();
    }

    private static void forwardMessage(MessageListActivity context, BaseMessage message) {

//        Intent intent = new Intent(context, MessagingContactListActivity.class);
//         context.startActivity(intent);

        //Retrieve list of single contact
        List<ContactItem> contactItems = STWContactManager.getInstance().getCompanyContacts(context, STWContactFilter.SINGLE);

        // show list of contacts
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_list_contact);
        dialog.setTitle("Choose a contact");
        ListView list = (ListView) dialog.findViewById(R.id.list);
        DialogContactAdapter contactAdapter = new DialogContactAdapter(context, contactItems);
        list.setAdapter(contactAdapter);
        dialog.show();

        Log.e("forward ", "forward");

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Get selected item
                ContactItem contact = contactItems.get(position);

                //Retrieve phone item related to the selected contact
                PhoneItem phone = STWContactManager.getInstance().getBusinessPhone(context, contact);

                if (phone == null) {
                    return;
                }

                ArrayList<String> messagesIdToFroward = new ArrayList<>();
                messagesIdToFroward.add(message.getId());

                //forward selected message to one contact
                // a new one to one conversation will be created
                STWMessagingManager.getInstance().forwardMessages(context, messagesIdToFroward, new String[]{phone.getInternationalNumber()}, ThreadItem.THREAD_TYPE_ONE_TO_ONE, null, new CompletionCallback() {
                    @Override
                    public void onError(MessagingError error) {
                        //Handle error
                    }

                    @Override
                    public void onCompletion(ThreadItem threadItem, BaseMessage message) {
                        //Open The new Conversation
                        ConversationUtils.openConversation(context, threadItem.getId(), threadItem.getThreadType());
                    }
                });
            }
        });

    }

    private static void deleteMessage(MessageListActivity context, String messageId, int position) {

        STWMessagingManager.getInstance().deleteMessage(context, messageId, new STWOperationCallback() {
            @Override
            public void onError(Error error) {

            }

            @Override
            public void onSuccess() {
                //remove loader
                //an event will be invoked when the message is successfully deleted from server
                //register to {@link IConversationObservable}
            }
        });
    }


    public static void selectImage(MessageListActivity context) {
        try {
            PackageManager pm = context.getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, context.getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            context.startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            context.startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_CODE);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static void inviteUser(Context context, String conversationId) {

        // show list of contacts
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Invite a contact");


        //Retrieve list of single contact
        List<ContactItem> contactItems = STWContactManager.getInstance().getCompanyContacts(context, STWContactFilter.SINGLE);
        DialogContactAdapter contactAdapter = new DialogContactAdapter(context, contactItems);

        builder.setAdapter(contactAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ContactItem contact = contactItems.get(which);

                //Retrieve phone item related to the selected contact
                PhoneItem phone = STWContactManager.getInstance().getBusinessPhone(context, contact);

                if (phone == null) {
                    return;
                }

                //invite users to this conversation
                STWMessagingManager.getInstance().inviteParticipants(context, new String[]{phone.getInternationalNumber()}, conversationId, new STWOperationCallback<MessagingError>() {

                    @Override
                    public void onError(MessagingError error) {

                    }

                    @Override
                    public void onSuccess() {
                        //An event "onInvitationCompleted" will be invoked when the invitation is successfully sent
                        //No need to update UI here
                    }
                });
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}

