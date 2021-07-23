/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 24 janv. 2020 12:20:05 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 24 janv. 2020 12:11:22 +0100
 */

package com.stw.protorype.ui.activity.messaging;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.adapter.ContactAdapter;
import com.stw.protorype.ui.activity.messaging.utils.ConversationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.stw.protorype.MainConstant.GEOLOCATION;
import static com.stw.protorype.MainConstant.MESSAGING;

public class MessagingContactListActivity extends AppCompatActivity {


    private int conversationType;
    List<ContactItem> contactItems = new ArrayList<>();
    ListView list ;
    FloatingActionButton validateButton;
    private String conversationName;
    private String comesFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_contact_list);

        //inti view
        list = findViewById(R.id.list);
        validateButton = findViewById(R.id.validateButton);

        conversationType =  getIntent().getIntExtra(ConversationUtils.EXTRA_CONVERSATION_TYPE, 0);
        conversationName =  getIntent().getExtras().getString(ConversationUtils.EXTRA_CONVERSATION_NAME);
        comesFrom =  getIntent().getExtras().getString(ConversationUtils.EXTRA_COMES_FROM);


        //retrieve list contacts according to the conversation type
        switch (conversationType) {
            case ThreadItem.THREAD_TYPE_ONE_TO_ONE:
                contactItems = STWContactManager.getInstance().getCompanyContacts(this, STWContactFilter.SINGLE);
                break;
            case ThreadItem.THREAD_TYPE_GROUP:
            case ThreadItem.THREAD_TYPE_ONE_TO_MANY:
                contactItems = STWContactManager.getInstance().getCompanyContacts(this, STWContactFilter.ALL);
                break;
        }


        ContactAdapter contactAdapter = new ContactAdapter(this, contactItems, conversationType);
        list.setAdapter(contactAdapter);

        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    //One To many or group conversation
                List<ContactItem> selectedContactItems = contactAdapter.getSelectedContactList();

                if(conversationType != ThreadItem.THREAD_TYPE_ONE_TO_ONE && selectedContactItems.size() == 0){
                    return;
                }
                if (comesFrom.equals(MESSAGING)) {
                    if(conversationType == ThreadItem.THREAD_TYPE_ONE_TO_ONE){
                        ContactItem selectedContact = contactAdapter.getSelectedContact();

                        if(selectedContact == null){
                            return;
                        }

                        ConversationUtils.sendOneToOneConversation(MessagingContactListActivity.this, selectedContact);
                        finish();
                        return;
                    }

                    if(selectedContactItems.size() == 1 && !selectedContactItems.get(0).isGroup()){
                        conversationType = ThreadItem.THREAD_TYPE_ONE_TO_ONE;
                        ConversationUtils.sendOneToOneConversation(MessagingContactListActivity.this, selectedContactItems.get(0));
                        finish();
                        return;
                    }

                    ConversationUtils.sendOneToManyOrGroupConversation(MessagingContactListActivity.this,conversationName, selectedContactItems, conversationType);
                    finish();
                } else if (comesFrom.equals(GEOLOCATION)){
                    ConversationUtils.sendSelectedContactsToGeoloc(MessagingContactListActivity.this, selectedContactItems);
                    finish();
                }


            }
        });

    }
}
