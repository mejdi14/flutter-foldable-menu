/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:00:45 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 10 avr. 2020 11:06:53 +0100
 */

package com.stw.protorype.ui.activity.mybusiness.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.model.CategorySection;

import java.util.List;

public class CategorySectionAdapter extends RecyclerView.Adapter<CategorySectionAdapter.ViewHolder> {

    private List<CategorySection> mSectionList;
    private Context mContext;
    private TemplateAdapter mTemplateAdapter;

    public CategorySectionAdapter(Context context, List<CategorySection> sections) {
        mSectionList = sections;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CategorySectionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_section, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySection section = mSectionList.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return mSectionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mSectionName;
        private RecyclerView mItemRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSectionName = itemView.findViewById(R.id.category_section_title);
            mItemRecyclerView = itemView.findViewById(R.id.category_section_recycler_view);
        }

        public void bind(CategorySection section) {

            mSectionName.setText(section.getSectionTitle());

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

            mItemRecyclerView.setLayoutManager(linearLayoutManager);

            mTemplateAdapter = new TemplateAdapter(mContext, section.getAllItemsInSection(), false);

            mItemRecyclerView.setAdapter(mTemplateAdapter);
        }
    }

}