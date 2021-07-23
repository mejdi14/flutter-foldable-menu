/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 3 janv. 2020 09:14:10 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 31 déc. 2019 13:00:20 +0100
 */

package com.stw.protorype.ui.activity.messaging.adapter;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.messaging.STWAttachmentManager;
import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.BaseAttachment;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.MgmMessage;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.MessageListActivity;
import com.stw.protorype.ui.activity.messaging.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<BaseMessage> messages;
    private MessageListActivity context;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_IMAGE = 2;

    private boolean isLoading;

    public MessageAdapter(MessageListActivity context, List<BaseMessage> messageList) {
        this.context = context;
        messages = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View messageAdapter = layoutInflater.inflate(R.layout.message_adapter, parent, false);
            return new ItemViewHolder(messageAdapter);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View imageAdapter = layoutInflater.inflate(R.layout.image_adapter, parent, false);
            return new ImageViewHolder(imageAdapter);
        }
    }

    @Override
    public int getItemViewType(int position) {

        BaseMessage message = messages.get(position);
        if (message == null) {
            return VIEW_TYPE_LOADING;
        }

        if (message.getMessageType() == BaseMessage.MESSAGE_TYPE_MGM) {
            if (((MgmMessage) message).getAttachmentId() != null && !((MgmMessage) message).getAttachmentId().isEmpty()) {
                return VIEW_TYPE_IMAGE;
            }
        }
        return VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            final BaseMessage message = messages.get(position);

            itemViewHolder.body.setText(getMessageBody(message));

            itemViewHolder.sender.setText(getNameSender(message));

            itemViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessageUtils.createDialogMessageOption(context, message, position);
                }
            });

        } else if (viewHolder instanceof ImageViewHolder) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) viewHolder;
            final MgmMessage message = (MgmMessage) messages.get(position);

            setImageThumbnail(message.getAttachmentId(), imageViewHolder.image);

            imageViewHolder.sender.setText(getNameSender(message));

            imageViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessageUtils.createDialogMessageOption(context, message, position);
                }
            });
        } else {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    private void setImageThumbnail(String attachmentId, ImageView imageView) {
        //Retrieve the attachment model related to the message
        BaseAttachment attachment = STWAttachmentManager.getInstance().getAttachmentById(context, attachmentId);
        //Display the thumbnail of the attachment
        imageView.setImageBitmap(BitmapFactory.decodeFile(attachment.getThumbnailPath()));
    }


    private String getNameSender(BaseMessage message) {

        //Retrieve the number of the sender of the current message
        String senderInternationalNumber = STWMessagingManager.getInstance().getMessageSender(context, message.getId());

        //Get the phoneItem model by sender number
        PhoneItem phoneItem = STWContactManager.getInstance().getPhoneByNumber(context, senderInternationalNumber);

        //Get name related to the phoneItem
        String dislayName = STWContactManager.getInstance().getDisplayName(context, phoneItem);

        return dislayName == null ? "" : dislayName;
    }


    private String getMessageBody(BaseMessage message) {
        //handle body text by type of message
        switch (message.getMessageType()) {
            case BaseMessage.MESSAGE_TYPE_MGM:
                //mgm message
                return ((MgmMessage) message).getBody();
            case BaseMessage.MESSAGE_TYPE_SERVICE_GROUP_ADD:
            case BaseMessage.MESSAGE_TYPE_SERVICE_SUBSCRIBER_ADDED_USER_TO_GROUP:
                //service message
                //an invitation to the group
                return "joins the conversation";
            case BaseMessage.MESSAGE_TYPE_SERVICE_GROUP_LEAVE:
                //service message
                //leave group
                return "leaves the conversation";
            case BaseMessage.MESSAGE_TYPE_SERVICE_SUBSCRIBER_NAME_ADDED_ME_TO_CONVERSATION:
                //service message
                //invite me to the group
                return "added me to conversation";
            case BaseMessage.MESSAGE_TYPE_SERVICE_GROUP_CONVERSATION_NAME_UPDATE:
                //service message
                //group name updated
                return "conversation name Updated";
            case BaseMessage.MESSAGE_TYPE_VOIP:
                //voip message
                return "voip call";

            default:
                return String.valueOf(message.getMessageType());

        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    public void refreshData(List<BaseMessage> messageList) {
        if (messages == null)
            messages = new ArrayList<>();
        messages = messageList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        messages.remove(position);
        notifyItemRemoved(position);
    }

    public void insertData(List<BaseMessage> messageList, int startPosition) {
        if (messages == null)
            messages = new ArrayList<>();
        messages.addAll(startPosition, messageList);
        notifyItemRangeInserted(startPosition, messageList.size());
    }

    public void insertData(BaseMessage message, int startPosition) {
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(startPosition, message);
        notifyItemRangeInserted(startPosition, 1);
    }


    public void isLoading(boolean isLoading) {
        //show loader when loading more messages

        Log.e("isLoading", isLoading+"");
        this.isLoading = isLoading;

        if (isLoading) {
            if (messages == null) {
                messages = new ArrayList<>();
            }
            messages.add(0, null);
            notifyItemInserted(0);
        } else {
            messages.remove(0);
            notifyItemRemoved(0);
        }

        notifyDataSetChanged();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView body, sender;
        LinearLayout linearLayout;

        ItemViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            body = (TextView) itemView.findViewById(R.id.body);
            sender = (TextView) itemView.findViewById(R.id.sender);

        }
    }


    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView sender;
        LinearLayout linearLayout;
        ImageView image;

        ImageViewHolder(View itemView) {
            super(itemView);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            image = (ImageView) itemView.findViewById(R.id.image);
            sender = (TextView) itemView.findViewById(R.id.sender);

        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }


}