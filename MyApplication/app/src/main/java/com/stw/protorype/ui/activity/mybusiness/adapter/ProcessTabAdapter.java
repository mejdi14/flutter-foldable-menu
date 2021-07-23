/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:01:23 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn lun., 13 avr. 2020 12:58:19 +0100
 */

package com.stw.protorype.ui.activity.mybusiness.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.api.mybusiness.MBSort;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessFilter;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessManager;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessTab;
import com.streamwide.smartms.lib.core.data.item.FilterAndSortItem;
import com.streamwide.smartms.lib.core.data.item.MBProcessCategoryModel;
import com.streamwide.smartms.lib.core.data.item.ProcessFilterItem;
import com.streamwide.smartms.lib.core.data.item.ProcessItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.Utils;

import java.util.ArrayList;
import java.util.List;

public class ProcessTabAdapter extends RecyclerView.Adapter<ProcessTabAdapter.ViewHolder> {

    private String CLASS_NAME = "ProcessTabAdapter";

    private List<MBProcessCategoryModel> mSectionList;
    private Context mContext;
    private ProcessAdapter mProcessAdapter;
    private SortFilterCallback mSortFilterCallback;

    public ProcessTabAdapter(Context context, List<MBProcessCategoryModel> sections, SortFilterCallback sortFilterCallback) {
        this.mContext = context;
        this.mSectionList = sections;
        this.mSortFilterCallback = sortFilterCallback;
    }

    public void setData(List<MBProcessCategoryModel> sections){
        this.mSectionList = sections;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProcessTabAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.process_tab_view, parent, false);

        return new ProcessTabAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProcessTabAdapter.ViewHolder holder, int position) {
        MBProcessCategoryModel section = mSectionList.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return mSectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mSectionName;
        private RecyclerView mItemRecyclerView;
        private TextView mSectionEmpty;
        private TextView mSortBtn;
        private TextView mFilterBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSectionName = itemView.findViewById(R.id.process_tab_title);
            mItemRecyclerView = itemView.findViewById(R.id.process_tab_recycler_view);
            mSectionEmpty = itemView.findViewById(R.id.process_tab_empty);
            mSortBtn = itemView.findViewById(R.id.process_tab_btn_sort);
            mFilterBtn = itemView.findViewById(R.id.process_tab_btn_filter);
        }

        public void bind(MBProcessCategoryModel section) {

            final @STWProcessTab int tab = section.getCategory().getTab();
            mSectionName.setText(getTabName(tab));

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mItemRecyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            mItemRecyclerView.addItemDecoration(dividerItemDecoration);

            mItemRecyclerView.setLayoutManager(linearLayoutManager);

            List<ProcessItem> ProcessList = section.getProcessList();

            final boolean isProcessListNotEmpty = ProcessList != null && !ProcessList.isEmpty();

            if(isProcessListNotEmpty) {
                mProcessAdapter = new ProcessAdapter(mContext, section.getProcessList());
                mItemRecyclerView.setAdapter(mProcessAdapter);

                mItemRecyclerView.setVisibility(View.VISIBLE);
                mSectionEmpty.setVisibility(View.GONE);
            }else{
                mItemRecyclerView.setVisibility(View.GONE);
                mSectionEmpty.setVisibility(View.VISIBLE);
            }

            mSortBtn.setOnClickListener(v -> {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "ViewHolder"),"bind",
                        " Sort Btn clicked " + getTabName(tab));

                if(isProcessListNotEmpty) {
                    List<FilterAndSortItem> sortItemList = getSortList(mContext, tab);

                    List<String> popupSortingList = new ArrayList<>();

                    int selectedItem = -1;
                    for (int i = 0; i < sortItemList.size(); i++) {

                        FilterAndSortItem filterAndSortItem = sortItemList.get(i);
                        popupSortingList.add(filterAndSortItem.getName());

                        if (filterAndSortItem.isSelected()) {
                            selectedItem = i;
                        }
                    }
                    final CharSequence[] items = popupSortingList.toArray(new CharSequence[popupSortingList.size()]);

                    new AlertDialog.Builder(mContext)
                            .setSingleChoiceItems(items, selectedItem, null)
                            .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                                    FilterAndSortItem selectedItem = sortItemList.get(selectedPosition);

                                    if (selectedItem != null) {
                                        STWProcessManager.getInstance().setSortType(mContext, tab, selectedItem.getSortId());
                                        if (mSortFilterCallback != null) {
                                            mSortFilterCallback.onSortOrFilterApplied();
                                        }
                                    }
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(mContext, "Empty Tab", Toast.LENGTH_SHORT).show();
                }
            });

            mFilterBtn.setOnClickListener(v -> {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "ViewHolder"),"bind",
                        " Filter Btn clicked " + getTabName(section.getCategory().getTab()));

                List<ProcessFilterItem> filterItemList = Utils.getFilterData(mContext, tab);

                if(filterItemList!= null && !filterItemList.isEmpty()) {

                    List<String> popupSortingList = new ArrayList<>();

                    final boolean[] checkedItems = new boolean[filterItemList.size()];

                    for (int i = 0; i < filterItemList.size(); i++) {

                        ProcessFilterItem filterItem = filterItemList.get(i);
                        popupSortingList.add(getFilterTypeName(filterItem.getType()) + " : " + filterItem.getData());

                        checkedItems[i] = false;

                    }
                    final CharSequence[] items = popupSortingList.toArray(new CharSequence[popupSortingList.size()]);

                    new AlertDialog.Builder(mContext)
                            .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    checkedItems[which] = isChecked;
                                }
                            })
                            .setPositiveButton("Delete Filter", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    for (int i = 0; i < checkedItems.length; i++) {
                                        boolean checked = checkedItems[i];
                                        if (checked) {
                                            ProcessFilterItem processFilterItem = filterItemList.get(i);
                                            if (processFilterItem != null) {
                                                STWProcessManager.getInstance().filter(mContext, tab, processFilterItem.getType(), null);
                                            }
                                        }
                                    }

                                    if (mSortFilterCallback != null) {
                                        mSortFilterCallback.onSortOrFilterApplied();
                                    }

                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(mContext, "Empty Filter list or Empty Tab", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getTabName(@STWProcessTab int tab) {

        switch (tab){
            case STWProcessTab.IN_PROGRESS :
                return "IN PROGRESS";
            case STWProcessTab.SUBMITTED :
                return "SUBMITTED";
            case STWProcessTab.COMPLETED :
                return "COMPLETED";
            case STWProcessTab.CANCELED :
                return "CANCELED";
            case STWProcessTab.NEW :
                return "NEW";
            case STWProcessTab.DRAFT :
                return "DRAFT";
            default :
                return "OTHER";
        }
    }

    private String getFilterTypeName(@STWProcessFilter int tab) {

        switch (tab){
            case STWProcessFilter.BY_OWNER_NAME :
                return "By owner";
            case STWProcessFilter.BY_INITIATOR_NAME :
                return "By initiator";
            case STWProcessFilter.BY_RECEIVER_NAME :
                return "By receiver";
            case STWProcessFilter.BY_START_DATE :
                return "By start date";
            case STWProcessFilter.BY_DUE_DATE :
                return "By due date";
            case STWProcessFilter.BY_PRIORITY :
                return "By priority";
            case STWProcessFilter.BY_CATEGORY :
                return "By category";
            default :
                return "OTHER";
        }
    }

    public @NonNull List<FilterAndSortItem> getSortList(Context context, @STWProcessTab int tab)
    {
        List<FilterAndSortItem> sortItems = new ArrayList<>();
        int oldSortingType = STWProcessManager.getInstance().getSortType(context, tab);
        sortItems.add(new FilterAndSortItem("Default",
                MBSort.TAG_SORT_DEFAULT, MBSort.TAG_SORT_DEFAULT == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort alphabetically (A to Z)",
                MBSort.TAG_SORT_ALPHABETICALLY, MBSort.TAG_SORT_ALPHABETICALLY == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort by priority (High to Low)",
                MBSort.TAG_SORT_BY_PRIORITY, MBSort.TAG_SORT_BY_PRIORITY == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort by start date",
                MBSort.TAG_SORT_BY_START_DATE, MBSort.TAG_SORT_BY_START_DATE == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort by due date",
                MBSort.TAG_SORT_BY_DUE_DATE, MBSort.TAG_SORT_BY_DUE_DATE == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort by reception date",
                MBSort.TAG_SORT_BY_RECEPTION_DATE, MBSort.TAG_SORT_BY_RECEPTION_DATE == oldSortingType));
        sortItems.add(new FilterAndSortItem("Sort by category",
                MBSort.TAG_SORT_BY_CATEGORY, MBSort.TAG_SORT_BY_CATEGORY == oldSortingType));
        return sortItems;
    }

    public interface SortFilterCallback{

        void onSortOrFilterApplied();
    }
}