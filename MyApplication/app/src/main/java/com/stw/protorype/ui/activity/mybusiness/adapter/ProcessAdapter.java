/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:01:13 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 14 avr. 2020 10:05:25 +0100
 */

package com.stw.protorype.ui.activity.mybusiness.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.api.mybusiness.TemplateIconType;
import com.streamwide.smartms.lib.core.data.item.ProcessItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.Utils;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ProcessAdapter extends RecyclerView.Adapter<ProcessAdapter.ViewHolder> {

    private static final String TAG = "TemplateAdapter";
    private Context mContext;
    private List<ProcessItem> mProcessItems;

    public ProcessAdapter(Context context, List<ProcessItem> items) {
        mContext = context;
        this.mProcessItems = items;
    }

    @NonNull
    @Override
    public ProcessAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.process_item_view, parent, false);
        return new ProcessAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProcessItem processItem = mProcessItems.get(position);

        holder.bind(processItem);
    }

    @Override
    public int getItemCount() {
        return mProcessItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName;
        private TextView itemStatus;
        private TextView itemOwner;
        private TextView itemDate;
        private TextView itemDueDate;
        private TextView itemEscalationDate;
        private TextView itemBadger;
        private TextView itemPriority;
        private AppCompatImageView itemImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.process_item_title);
            itemStatus = itemView.findViewById(R.id.process_item_status);
            itemOwner = itemView.findViewById(R.id.process_item_owner);
            itemDate = itemView.findViewById(R.id.process_item_date);
            itemDueDate = itemView.findViewById(R.id.process_item_due_date);
            itemEscalationDate = itemView.findViewById(R.id.process_item_escalation_date);
            itemBadger = itemView.findViewById(R.id.process_item_badger);
            itemPriority = itemView.findViewById(R.id.process_item_priority);
            itemImage = itemView.findViewById(R.id.process_item_image);
        }

        void bind(ProcessItem processItem) {

            if(processItem != null) {
                itemName.setText(processItem.getLabel());
                //itemOwner.setText(processItem.getOwnerName());
                itemDate.setText(Utils.getDate(processItem.getLastUpdateDate()));
                setStatus(processItem);
                setPriority(processItem);

                Bitmap favoriteIcon = STWTemplateManager.getInstance().geTemplateIcon(processItem.getTemplateUUID(), TemplateIconType.SMALL);

                if (favoriteIcon != null) {
                    itemImage.setImageBitmap(favoriteIcon);
                }

                if(Utils.isEmpty(processItem.getDueDate())){
                    itemDueDate.setVisibility(GONE);
                }else{
                    itemDueDate.setText("Due Date : "+Utils.getDate(processItem.getDueDate()));
                    itemDueDate.setVisibility(VISIBLE);
                }

                if(Utils.isEmpty(processItem.getNextEscalationDate())){
                    itemEscalationDate.setVisibility(GONE);
                }else{
                    itemEscalationDate.setText("Next Escalation Date : "+Utils.getDate(processItem.getDueDate()));
                    itemEscalationDate.setVisibility(VISIBLE);
                }

                // unread badger
                int notSeenDueDateNotification =
                        (processItem.isDueDateWillExpireNotified() ? 1 :0) + (processItem.isDueDateNotified() ? 1 : 0);

                int unreadProcessCount = processItem.getBadgeNumber() + notSeenDueDateNotification;
                if (unreadProcessCount == 0) {
                    itemBadger.setVisibility(GONE);
                } else {
                    itemBadger.setVisibility(View.VISIBLE);
                    itemBadger.setText(String.valueOf(unreadProcessCount));
                }
            }
        }

        private void setPriority(ProcessItem processItem) {

            int priorityLevel = processItem.getPriority();

            String processPriority = "";

            switch (priorityLevel) {
                case ProcessItem.PRIORITY_HIGH:
                    processPriority = "Priority ***";
                    break;
                case ProcessItem.PRIORITY_LOW:
                    processPriority = "Priority *";
                    break;
                case ProcessItem.PRIORITY_MEDIUM : default:
                    processPriority = "Priority **";
            }

            itemPriority.setText(processPriority);
            itemPriority.setVisibility(processItem.isPriorityUsed()? VISIBLE :  GONE);
        }

        private void setStatus(ProcessItem processItem) {
            String state = processItem.getStatus();

            // if there is no status consider it is as updated to calculate owner
            if (Utils.isEmpty(state)) {
                state = ProcessItem.STATUS_UPDATED;
            }
            String statusText;
            // init default status text
            if (processItem.getCurrentVersion() > 1) {
                statusText = "Updated by";
            } else {
                statusText = "Started by";
            }

            String senderName = processItem.getSenderName();

            switch (state) {
                case ProcessItem.STATUS_WAITING: {
                    int tab = processItem.getTab();

                    switch (tab) {

                        case ProcessItem.TAB_SUBMITTED: {
                            statusText = "Waiting to be submitted";
                            senderName = "";
                        }
                        break;

                        case ProcessItem.TAB_CANCELED: {
                            statusText = "Waiting to be canceled";
                            senderName = "";
                        }
                        break;

                        case ProcessItem.TAB_COMPLETED: {
                            statusText = "waiting to be completed";
                            senderName = "";
                        }
                        break;
                        case ProcessItem.TAB_DRAFT:
                            break;

                        case ProcessItem.TAB_IN_PROGRESS: {
                            if (processItem.getCurrentVersion() == 0 && processItem.getCurrentDraftVersion() >= 0) {
                                statusText = "Started by";

                            } else if (processItem.getCurrentVersion() > 1) {

                                if (processItem.getEscalationLevel() > 0) {
                                    statusText = "Escalated to";
                                } else {
                                    statusText = "Updated by";
                                }

                            }
                            if (processItem.getCurrentVersion() > 0) {
                                boolean imOwner = processItem.isIOwn();
                                if (imOwner) {
                                    statusText = "Owned by";
                                    senderName = "me";
                                } else if (processItem.isOwned()) {
                                    String ownerName = processItem.getOwnerName();
                                    if (!Utils.isEmpty(ownerName)) {
                                        statusText = "Owned by";
                                        senderName = ownerName;

                                    }
                                }

                                if (processItem.getEscalationLevel() > 0) {
                                    statusText = "Escalated to";
                                    senderName = imOwner ? senderName + ", " + processItem.getRecipients() : processItem.getRecipients();
                                }
                            }
                        }
                        break;
                        default:
                            break;
                    }
                }
                break;
                case ProcessItem.STATUS_COMPLETED: {
                    statusText = "Completed by";
                }
                break;
                case ProcessItem.STATUS_CANCELED: {
                    statusText = "Canceled by";
                }
                break;
                case ProcessItem.STATUS_UPDATED: {
                    if (processItem.getCurrentVersion() == 0 && processItem.getCurrentDraftVersion() >= 0) {
                        statusText = "Started by";

                    } else if (processItem.getCurrentVersion() > 1) {

                        statusText = "Updated by";

                    } else {
                        String scheduledTimeStamp = processItem.getScheduledDate();

                        boolean isScheduleNotReached =
                                processItem.isScheduled() && scheduledTimeStamp != null && !scheduledTimeStamp.isEmpty()
                                        && !Utils.isReachedDate(Long.valueOf(scheduledTimeStamp));

                        if (isScheduleNotReached) {
                            String scheduleMyBusinessDate = Utils.getDate(scheduledTimeStamp);
                            itemStatus.setText(String.format("%s %s", "Scheduled for ", scheduleMyBusinessDate));
                            itemOwner.setVisibility(INVISIBLE);
                            itemDate.setVisibility(INVISIBLE);
                            itemStatus.setVisibility(VISIBLE);
                            return;
                        } else {
                            itemDate.setVisibility(VISIBLE);
                            statusText = "Started by";
                        }
                    }

                    if (processItem.getCurrentVersion() > 0) {
                        boolean imOwner = processItem.isIOwn();
                        if (imOwner) {
                            statusText = "Owned by";
                            senderName = "me";
                        } else if (processItem.isOwned()) {
                            String ownerName = processItem.getOwnerName();
                            if (!Utils.isEmpty(ownerName)) {
                                statusText = "Owned by";
                                senderName = ownerName;

                            }
                        }

                        if (processItem.getEscalationLevel() > 0) {
                            statusText = "Escalated to";
                            senderName = imOwner ? senderName + ", " + processItem.getRecipients() : processItem.getRecipients();
                        }
                    }
                }
                break;
                default:
                    break;
            }

            itemStatus.setVisibility(Utils.isEmpty(statusText) ? INVISIBLE : VISIBLE);
            String status = statusText + " ";
            itemStatus.setText(status);

            itemOwner.setVisibility(Utils.isEmpty(senderName) ? INVISIBLE : VISIBLE);
            itemOwner.setText(senderName);

//        if (StringUtil.isEmpty(processItem.getParentProcessId())) {
//            mRepeatProcessImageView.setVisibility(GONE);
//        } else {
//            mRepeatProcessImageView.setVisibility(VISIBLE);
//        }
        }
    }



}