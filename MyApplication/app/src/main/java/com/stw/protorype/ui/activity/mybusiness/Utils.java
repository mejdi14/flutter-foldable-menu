/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:06:06 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 14 avr. 2020 15:06:04 +0100
 */

package com.stw.protorype.ui.activity.mybusiness;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessFilter;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessManager;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessTab;
import com.streamwide.smartms.lib.core.data.item.ProcessFilterItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utils {

    public static String getDate(String timestamp){
        return DateFormat.format("dd/MM/yy HH:mm", Long.valueOf(timestamp)).toString();
    }

    public static boolean isReachedDate(long date)
    {
        long currentDateTimestamp = getMillis(new Date());

        return currentDateTimestamp > date;
    }

    private static long getMillis(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.getTimeInMillis();
    }

    public static boolean isEmpty(@Nullable CharSequence value)
    {
        return (value == null || TextUtils.isEmpty(value.toString().trim()));
    }

    public  static List<ProcessFilterItem> getFilterData(Context context, @STWProcessTab int tab)
    {
        boolean hasFilters = STWProcessManager.getInstance().hasFilters(context, tab);

        List<ProcessFilterItem> FiltersList = new ArrayList<>();

        if (hasFilters) {

            ProcessFilterItem processFilterItem =
                    STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_INITIATOR_NAME);

            if (processFilterItem != null) {
                ArrayList<String> dataList =
                        new ArrayList<>(Arrays.asList(processFilterItem.getData().split(",")));
                if (dataList != null && !dataList.isEmpty()) {
                    ProcessFilterItem splitFilter;
                    for (String data : dataList) {
                        splitFilter = new ProcessFilterItem();
                        splitFilter.setId(processFilterItem.getId());
                        splitFilter.setProcessTab(processFilterItem.getProcessTab());
                        splitFilter.setType(processFilterItem.getType());
                        splitFilter.setData(data);
                        FiltersList.add(splitFilter);
                    }
                }

            }
            processFilterItem = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_OWNER_NAME);

            if (processFilterItem != null) {
                ArrayList<String> dataList =
                        new ArrayList<>(Arrays.asList(processFilterItem.getData().split(",")));
                if (dataList != null && !dataList.isEmpty()) {
                    ProcessFilterItem splitFilter;
                    for (String data : dataList) {
                        splitFilter = new ProcessFilterItem();
                        splitFilter.setId(processFilterItem.getId());
                        splitFilter.setProcessTab(processFilterItem.getProcessTab());
                        splitFilter.setType(processFilterItem.getType());
                        splitFilter.setData(data);
                        FiltersList.add(splitFilter);
                    }
                }
            }
            processFilterItem = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_RECEIVER_NAME);

            if (processFilterItem != null) {
                ArrayList<String> dataList =
                        new ArrayList<>(Arrays.asList(processFilterItem.getData().split(",")));
                if (dataList != null && !dataList.isEmpty()) {
                    ProcessFilterItem splitFilter;
                    for (String data : dataList) {
                        splitFilter = new ProcessFilterItem();
                        splitFilter.setId(processFilterItem.getId());
                        splitFilter.setProcessTab(processFilterItem.getProcessTab());
                        splitFilter.setType(processFilterItem.getType());
                        splitFilter.setData(data);
                        FiltersList.add(splitFilter);
                    }
                }
            }

            ProcessFilterItem processFilterItemByStartDate = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_START_DATE);
            ProcessFilterItem processFilterItemByDueDate = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_DUE_DATE);

            if (processFilterItemByStartDate != null) {
                FiltersList.add(processFilterItemByStartDate);
            }
            if (processFilterItemByDueDate != null) {
                FiltersList.add(processFilterItemByDueDate);
            }
            ProcessFilterItem processFilterItemByPriority = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_PRIORITY);

            if (processFilterItemByPriority != null) {
                FiltersList.add(processFilterItemByPriority);
            }
            ProcessFilterItem processFilterItemByCategory = STWProcessManager.getInstance().getProcessFilter(context, tab, STWProcessFilter.BY_CATEGORY);

            if (processFilterItemByCategory != null) {
                FiltersList.add(processFilterItemByCategory);
            }
        }

        return FiltersList;
    }
}
