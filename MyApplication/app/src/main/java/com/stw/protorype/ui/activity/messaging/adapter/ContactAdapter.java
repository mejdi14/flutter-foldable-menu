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

package com.stw.protorype.ui.activity.messaging.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<ContactItem> {

    private List<ContactItem> contactItems;
    private int conversationType;

    //In case of one t many or group conversation
    private List<ContactItem> selectedContactItems;

    //In case of one t one conversation
    private CheckBox lastCheckBox;
    private ContactItem selectedContactItem;

    private Context mContext;


    public ContactAdapter(Context context, List<ContactItem> data, int conversationType) {
        super(context, R.layout.item_contact, data);
        this.contactItems = data;
        this.selectedContactItems = new ArrayList<>();
        this.mContext = context;
        this.conversationType = conversationType;
    }


    @Nullable
    @Override
    public ContactItem getItem(int position) {
        return contactItems.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ContactItem contactItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_contact, parent, false);
        TextView txtName = (TextView) convertView.findViewById(R.id.nameTxt);
        TextView txtPhone = (TextView) convertView.findViewById(R.id.phoneTxt);
        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);


        String name = STWContactManager.getInstance().getDisplayNameForContactItem(mContext, contactItem);
        txtName.setText(name);

        String number = "";
        if (!contactItem.isGroup()) {
            PhoneItem phoneItem = STWContactManager.getInstance().getBusinessPhone(mContext, contactItem);
            if (phoneItem != null) {
                number = phoneItem.getInternationalNumber();
            }
        }
        txtPhone.setText(number);

        if (conversationType != ThreadItem.THREAD_TYPE_ONE_TO_ONE) {
            checkBox.setChecked(selectedContactItems.contains(contactItem));
        } else {
            checkBox.setChecked(selectedContactItem != null && selectedContactItem.equals(contactItem));
        }


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //Contact checked
                if (isChecked) {

                    if (conversationType == ThreadItem.THREAD_TYPE_ONE_TO_ONE) {
                        //is one to one conversation
                        if (lastCheckBox != null) {
                            //uncheck previous contact
                            lastCheckBox.setChecked(false);
                        }
                        //set new selected contact
                        lastCheckBox = checkBox;
                        selectedContactItem = contactItem;
                    } else {

                        // is a group conversation or a one to many conversation
                        // add contact t the selected contact list
                        selectedContactItems.add(contactItem);
                    }

                } else {
                    //Contact unchecked

                    if (conversationType == ThreadItem.THREAD_TYPE_ONE_TO_ONE) {
                        // is one to one conversation
                        // delete last selected contact
                        selectedContactItem = null;
                        lastCheckBox = null;
                    } else {

                        // is a group conversation or a one to many conversation
                        // remove contact from selected contact list
                        selectedContactItems.remove(contactItem);
                    }
                }
            }

        });


        // Return the completed view to render on screen
        return convertView;
    }

    //in case of one to many or group conversation
    public List<ContactItem> getSelectedContactList() {
        return selectedContactItems;
    }

    //in case of one to one conversation
    public ContactItem getSelectedContact() {
        return selectedContactItem;
    }
}
