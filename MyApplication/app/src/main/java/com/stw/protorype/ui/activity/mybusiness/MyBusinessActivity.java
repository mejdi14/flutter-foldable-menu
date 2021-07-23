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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessManager;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessTab;
import com.streamwide.smartms.lib.core.data.item.MBProcessCategoryModel;
import com.streamwide.smartms.lib.core.data.item.ProcessCategoryItem;
import com.streamwide.smartms.lib.core.data.item.ProcessItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.adapter.ProcessTabAdapter;

import java.util.ArrayList;
import java.util.List;


public class MyBusinessActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private ProcessTabAdapter mAdapter;

    private List<MBProcessCategoryModel> mProcessCategoryModelList;

    ProcessTabAdapter.SortFilterCallback mSortFilterCallback = new ProcessTabAdapter.SortFilterCallback(){
        @Override
        public void onSortOrFilterApplied() {
            mProcessCategoryModelList = getCategoryProcessModelList(MyBusinessActivity.this);
            mAdapter.setData(mProcessCategoryModelList);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_business);

        setTitle("My Business");

        initView();
        initData();
    }

    private void initView() {

        mRecyclerView = findViewById(R.id.process_recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initData() {


        mProcessCategoryModelList = getCategoryProcessModelList(this);

        mAdapter = new ProcessTabAdapter(this, mProcessCategoryModelList, mSortFilterCallback);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mybusiness, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_my_business_new) {
            startActivity(new Intent(this, TemplateActivity.class));
        }else if (item.getItemId() == R.id.menu_my_business_sort_filter) {
            startActivity(new Intent(this, AddFilterActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private List<MBProcessCategoryModel> getCategoryProcessModelList(@NonNull Context context)
    {
        /*
         * get list of process categories.
         */
        List<ProcessCategoryItem> processCategoryItemList = new ArrayList<>();


        processCategoryItemList.add(new ProcessCategoryItem ("-5", 0, STWProcessTab.NEW ));
        processCategoryItemList.add(new ProcessCategoryItem ("-7", 0, STWProcessTab.DRAFT ));
        processCategoryItemList.add(new ProcessCategoryItem ("-1", 0, STWProcessTab.IN_PROGRESS ));
        processCategoryItemList.add(new ProcessCategoryItem ("-2", 0, STWProcessTab.SUBMITTED ));
        processCategoryItemList.add(new ProcessCategoryItem ("-3", 0, STWProcessTab.COMPLETED ));
        processCategoryItemList.add(new ProcessCategoryItem ("-4", 0, STWProcessTab.CANCELED ));


        List<MBProcessCategoryModel> processCategoryMapList = new ArrayList<>();

        for (ProcessCategoryItem categoryItem : processCategoryItemList) {

            /*
             * get the list of process corresponding to current category.
             */
            //List<ProcessItem> processList = STWProcessManager.getInstance().getProcessListByTab(context, categoryItem.getTab(), null);

            List<ProcessItem> processList = STWProcessManager.getInstance().getProcessListByTab(context, categoryItem.getTab(), true, true,null);


            MBProcessCategoryModel categoryModel = new MBProcessCategoryModel(categoryItem, processList);

            processCategoryMapList.add(categoryModel);

        }

        return processCategoryMapList;
    }
}
