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
 * @lastModifiedOn lun., 30 déc. 2019 17:27:57 +0100
 */

package com.stw.protorype.ui.activity.messaging.adapter;

import com.streamwide.smartms.lib.core.api.messaging.STWMessagingManager;
import com.streamwide.smartms.lib.core.data.item.BaseMessage;
import com.streamwide.smartms.lib.core.data.item.MgmMessage;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.ConversationListActivity;
import com.stw.protorype.ui.activity.messaging.utils.ConversationUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ThreadItem> conversations;
    private ConversationListActivity context;

    public ConversationAdapter(ConversationListActivity context, List<ThreadItem> conversations) {
        this.context = context;
        this.conversations = conversations;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.conversation_adapter, parent, false);
        ItemViewHolder viewHolder;
        viewHolder = new ItemViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        final ThreadItem conversation = conversations.get(position);


        if (conversation.getThreadType() == ThreadItem.THREAD_TYPE_GROUP) {
            itemViewHolder.title.setText(conversation.getGroupName());
        } else if (conversation.getThreadType() == ThreadItem.THREAD_TYPE_ONE_TO_ONE) {
            itemViewHolder.title.setText(conversation.getThreadParticipantName());
        } else {
            itemViewHolder.title.setText("ONE to MANY conversation");

        }

        itemViewHolder.lastMessage.setText(getLastMessage(conversation.getId()));

        //holder.imageView.setImageResource(threadItem.get);

        if (conversation.getUnreadMgmCount() == 0) {
            itemViewHolder.unreadCount.setVisibility(View.GONE);
        } else {
            itemViewHolder.unreadCount.setVisibility(View.VISIBLE);
            itemViewHolder.unreadCount.setText(String.valueOf(conversation.getUnreadMgmCount()));
        }
        itemViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversationUtils.createDialogConversationOption(context, conversation, position);
            }
        });

    }

    private String getLastMessage(String id) {
        String messageId = STWMessagingManager.getInstance().getLastMessageId(context, id);
        BaseMessage message = STWMessagingManager.getInstance().getMessageById(context, messageId);
        if (message == null) {
            return "";
        }
        if (message.getMessageType() == BaseMessage.MESSAGE_TYPE_MGM) {
            return ((MgmMessage) message).getBody();
        }

        return "";
    }

    public int getItemPosition(ThreadItem conversation) {
        if (conversations.contains(conversation)) {
            return conversations.indexOf(conversation);
        } else
            return -1;
    }

    @Override
    public int getItemCount() {
        if (conversations == null)
            return 0;

        return conversations.size();
    }

    public void refreshData(List<ThreadItem> threadItems) {
        conversations = threadItems;
        notifyDataSetChanged();
    }

    public void itemChanged(ThreadItem threadItem, int pos) {
        conversations.remove(pos);
        conversations.add(pos, threadItem);
        notifyItemChanged(pos);
    }

    public void removeData(int pos) {
        conversations.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, conversations.size());

    }

    public void insertData(ThreadItem conversation, int startPosition) {
        if (conversations == null)
            conversations = new ArrayList<>();
        conversations.add(startPosition, conversation);
        notifyItemRangeInserted(startPosition, 1);
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title, lastMessage, unreadCount;
        RelativeLayout relativeLayout;

        ItemViewHolder(View itemView) {
            super(itemView);
            this.imageView = (ImageView) itemView.findViewById(R.id.imageView);
            this.unreadCount = (TextView) itemView.findViewById(R.id.unreadCount);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            this.title = (TextView) itemView.findViewById(R.id.title);
            this.lastMessage = (TextView) itemView.findViewById(R.id.lastMessage);

        }
    }

}