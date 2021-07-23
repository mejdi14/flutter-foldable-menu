/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on jeu., 26 déc. 2019 15:13:16 +0100
 * @copyright  Copyright (c) 2019 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2019 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn jeu., 26 déc. 2019 15:10:48 +0100
 */

package com.stw.protorype.ui.activity.contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.DefaultContactIconResources;
import com.stw.protorype.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LocalContactAdapter extends RecyclerView.Adapter<LocalContactAdapter.ContactViewHolder> implements Filterable {

    private static final String TAG = "ContactAdapter";

    private List<ContactItem> mContactItemList;
    private List<ContactItem> mcontactListFiltered;
    private Context mContext;

    public LocalContactAdapter(Context context, List<ContactItem> contactItemList)
    {

        this.mContext = context;
        this.mContactItemList = contactItemList;
        this.mcontactListFiltered = contactItemList;
    }

    @Override
    public LocalContactAdapter.ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        // Inflating item view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_view, parent, false);
        return new LocalContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LocalContactAdapter.ContactViewHolder holder, int position)
    {
        final ContactItem contactItem = mcontactListFiltered.get(position);

        if(contactItem!= null) {
            String name = STWContactManager.getInstance().getDisplayNameForContactItem(mContext, contactItem);

            holder.mContactName.setText(name);
            holder.mIcon.setTag(name);

            holder.mContactStatus.setVisibility(View.GONE);

            DefaultContactIconResources defaultContactIconResources = ContactUtils.getDefaultIconResources();
            STWContactManager.getInstance().loadContactPicture(mContext, holder.mIcon, contactItem, defaultContactIconResources);

        }
    }

    @Override
    public int getItemCount()
    {

        if (mcontactListFiltered == null) {
            return 0;
        }

        return mcontactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mcontactListFiltered = mContactItemList;
                } else {
                    List<ContactItem> filteredList = new ArrayList<>();
                    for (ContactItem row : mContactItemList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for first name or last name match
                        if (row.getContactName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    mcontactListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mcontactListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mcontactListFiltered = (ArrayList<ContactItem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void changeContactList(List<ContactItem> contactList) {
        mContactItemList = contactList;
        mcontactListFiltered = contactList;
        notifyDataSetChanged();
    }


    class ContactViewHolder extends RecyclerView.ViewHolder {

        private TextView mContactName;
        private TextView mContactStatus;
        private ImageView mIcon;

        private ContactViewHolder(View view)
        {
            super(view);
            mContactName = view.findViewById(R.id.item_contact_name);
            mContactStatus = view.findViewById(R.id.item_contact_status);
            mIcon = view.findViewById(R.id.item_contact_avatar);
        }
    }

}