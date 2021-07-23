/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 10 avr. 2020 11:06:25 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 10 avr. 2020 11:06:17 +0100
 */

package com.stw.protorype.ui.activity.mybusiness;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateListUpdateListener;
import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.data.item.TemplateItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.adapter.TemplateAdapter;

import java.util.List;

public class RecentFragment extends Fragment {

    private static final String TAG = "RecentFragment";
    private RecyclerView mRecyclerView;

    STWTemplateListUpdateListener mSTWTemplateListUpdateListener = this::initData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = View.inflate(getActivity(), R.layout.fragment_template_list, null);
        initView(view);
        initData();

        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        STWTemplateManager.getInstance().registerTemplateListListener(mSTWTemplateListUpdateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        STWTemplateManager.getInstance().unregisterTemplateListListener(mSTWTemplateListUpdateListener);
    }

    private void initView(View view) {

        mRecyclerView = view.findViewById(R.id.template_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initData() {

        Log.d(TAG, "initData");

        List<TemplateItem> recentTemplateList = STWTemplateManager.getInstance().getRecentUsedTemplates(getContext());


        final TemplateAdapter adapter = new TemplateAdapter(getContext(), recentTemplateList, true);
        mRecyclerView.setAdapter(adapter);
    }
}
