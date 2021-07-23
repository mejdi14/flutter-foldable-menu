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

import java.util.List;

import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettings;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.DefaultContactIconResources;
import com.streamwide.smartms.lib.core.data.item.OperationalStatusItem;
import com.stw.protorype.R;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private static final String TAG = "ContactAdapter";

    private List<ContactItem> mContactItemList;
    private Context mContext;
    private static final int CONTACT_TYPE_SINGLE = 1;
    private static final int CONTACT_TYPE_GROUP = 2;
    /**
     * Listener used to return item click event.
     */
    private ContactAdapter.OnItemClickListener mItemListener;

    public ContactAdapter(Context context, List<ContactItem> contactItemList,
                          ContactAdapter.OnItemClickListener itemListener)
    {

        this.mContext = context;
        this.mContactItemList = contactItemList;
        this.mItemListener = itemListener;
    }

    @Override
    public ContactAdapter.ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        // Inflating item view.
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_view, parent, false);
        return new ContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactAdapter.ContactViewHolder holder, int position)
    {
        final ContactItem contactItem = mContactItemList.get(position);
        int itemViewType = getItemViewType(position);

        if(contactItem!= null) {
            String name = STWContactManager.getInstance().getDisplayNameForContactItem(mContext, contactItem);
            holder.mContactName.setText(name);


            String status = contactItem.getStatus();
            if(TextUtils.isEmpty(status)){
                holder.mContactStatus.setVisibility(View.GONE);
            }else{
                holder.mContactStatus.setText(status);
                holder.mContactStatus.setVisibility(View.VISIBLE);
            }

            //Check if operational status feature is allowed by the administrator or not
            boolean isOperationalStatusAllowed = STWAccountSettings.getInstance().isOperationalStatusAllowed(mContext);

            int operationalStatusId = contactItem.getOperationalStatus();

            if(isOperationalStatusAllowed && operationalStatusId > 0){

                OperationalStatusItem operationalStatusItem =
                        STWContactManager.getInstance().getOperationalStatusByIdentifier(mContext, operationalStatusId);

                if(operationalStatusItem == null ){
                    holder.mContactOperationalStatus.setVisibility(View.GONE);
                }else{

                    String operationalStatusToShow = operationalStatusItem.getCode()+" - "+ operationalStatusItem.getLabel();

                    holder.mContactOperationalStatus.setText(operationalStatusToShow);
                    holder.mContactOperationalStatus.setVisibility(View.VISIBLE);
                }
            }else{
                holder.mContactOperationalStatus.setVisibility(View.GONE);
            }

            if (CONTACT_TYPE_SINGLE == itemViewType) {
                DefaultContactIconResources defaultContactIconResources = ContactUtils.getDefaultIconResources();
                STWContactManager.getInstance().loadContactPicture(mContext, holder.mIcon, contactItem, defaultContactIconResources);

                    boolean allowUserStatusAvailability = STWAccountSettings.getInstance().isUserAvailabilityAllowed(mContext);

                    if (ContactItem.USER_NOT_REACHABLE == contactItem.getUserReachability()) {
                        holder.mAvailability.setVisibility(View.VISIBLE);
                        holder.mAvailability.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        if (allowUserStatusAvailability) {
                            if (ContactItem.USER_AVAILABILITY_AVAILABLE == contactItem.getUserAvailability()) {
                                holder.mAvailability.setVisibility(View.VISIBLE);
                                holder.mAvailability.setBackgroundColor(Color.GREEN);
                            } else if (ContactItem.USER_AVAILABILITY_BUSY == contactItem.getUserAvailability()) {
                                holder.mAvailability.setVisibility(View.VISIBLE);
                                holder.mAvailability.setBackgroundColor(Color.RED);
                            } else {
                                holder.mAvailability.setVisibility(View.GONE);
                            }
                        } else {
                            holder.mAvailability.setVisibility(View.GONE);
                        }
                    }

            }else{
                holder.mIcon.setImageResource(R.drawable.ic_group_avatar);
                holder.mAvailability.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mItemListener != null) {
                        mItemListener.onItemClicked(v, holder.getAdapterPosition(), contactItem);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {

        if (mContactItemList == null) {
            return 0;
        }

        return mContactItemList.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        ContactItem contactItem = mContactItemList.get(position);
        if (contactItem.isGroup()) {
            return CONTACT_TYPE_GROUP;
        } else {
            return CONTACT_TYPE_SINGLE;
        }
    }


    class ContactViewHolder extends RecyclerView.ViewHolder {

        private TextView mContactName;
        private TextView mContactStatus;
        private TextView mContactOperationalStatus;
        private AppCompatImageView mIcon;
        private View mAvailability;

        private ContactViewHolder(View view)
        {
            super(view);
            mContactName = view.findViewById(R.id.item_contact_name);
            mContactStatus = view.findViewById(R.id.item_contact_status);
            mContactOperationalStatus = view.findViewById(R.id.item_contact_operational_status);
            mIcon = view.findViewById(R.id.item_contact_avatar);
            mAvailability = view.findViewById(R.id.item_contact_availability);
        }
    }

    /**
     * Interface to handle item click.
     */
    public interface OnItemClickListener {

        /**
         * fired when user click on any item .
         *
         * @param v
         *            recycler view item.
         * @param position
         *            position of the item clicked.
         */
        void onItemClicked(View v, int position, ContactItem contactItem);
    }

}