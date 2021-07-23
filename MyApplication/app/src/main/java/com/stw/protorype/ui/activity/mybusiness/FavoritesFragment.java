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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.mybusiness.STWFavoriteListUpdateListener;
import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.data.item.TemplateItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.adapter.FavoritesAdapter;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private static final String TAG = "FavoritesFragment";
    private RecyclerView mRecyclerView;
    private STWFavoriteListUpdateListener mSTWFavoriteListUpdateListener = this::initData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = View.inflate(getActivity(), R.layout.fragment_template_list, null);
        initView(view);
        initData();

        return view;
    }

    private void initView(View view) {

        mRecyclerView = view.findViewById(R.id.template_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        STWTemplateManager.getInstance().registerFavoriteListListener(mSTWFavoriteListUpdateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        STWTemplateManager.getInstance().unregisterFavoriteListListener(mSTWFavoriteListUpdateListener);
    }

    private void initData() {

        List<TemplateItem> recentTemplateList = STWTemplateManager.getInstance().getFavoriteTemplateList(getContext());

        if(recentTemplateList!= null ) {
            final FavoritesAdapter adapter = new FavoritesAdapter(getContext(), recentTemplateList);

            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(adapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mRecyclerView);

            mRecyclerView.setAdapter(adapter);
        }
    }
}