/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on jeu., 9 janv. 2020 12:33:47 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn jeu., 9 janv. 2020 12:33:47 +0100
 */

package com.stw.protorype.ui.activity.messaging.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.stw.protorype.R;

import java.util.List;

public class DialogContactAdapter extends ArrayAdapter<ContactItem> {

    private List<ContactItem> contactItems;
    private Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtPhone;

    }

    public DialogContactAdapter(Context context, List<ContactItem> data) {
        super(context, R.layout.item_contact, data);
        this.contactItems = data;
        this.mContext = context;
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
        ViewHolder viewHolder; // view lookup cache stored in tag


        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.dialog_item_contact, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.nameTxt);
            viewHolder.txtPhone = (TextView) convertView.findViewById(R.id.phoneTxt);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.txtName.setText(contactItem.getContactName());

        String number = "";
        if (!contactItem.isGroup()) {
            PhoneItem phoneItem = STWContactManager.getInstance().getBusinessPhone(mContext, contactItem);
            if (phoneItem != null) {
                number = phoneItem.getInternationalNumber();
            }
        }
        viewHolder.txtPhone.setText(number);


        // Return the completed view to render on screen
        return convertView;
    }
}
