/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 8 Jan 2020 16:13:47 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 16 Dec 2019 15:06:17 +0100
 */

package com.stw.protorype.ui.activity.geolocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.core.data.item.LocationAttachment;

import java.util.List;

public class PlacesAutoCompleteAdapter  extends ArrayAdapter<LocationAttachment> {
    private List<LocationAttachment> resultList;

    private int itemLayout;

   public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, List<LocationAttachment> storeDataLst) {
        super(context, textViewResourceId);
        resultList = storeDataLst;
        itemLayout = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(itemLayout, parent, false);
        }
        TextView strName =  convertView.findViewById(android.R.id.text1);
        LocationAttachment locationAttachment = getItem(position);
        if (locationAttachment != null) {
            strName.setText(locationAttachment.getAddress());
        }

        return convertView;
    }



    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public LocationAttachment getItem(int index) {
        return resultList.get(index);
    }


    public void setValue(List<LocationAttachment> suggestions) {
        resultList = suggestions;
        notifyDataSetChanged();
    }
}