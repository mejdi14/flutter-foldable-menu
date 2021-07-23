/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:01:34 +0100
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
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.Error;
import com.streamwide.smartms.lib.core.api.STWOperationCallback;
import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.api.mybusiness.TemplateIconType;
import com.streamwide.smartms.lib.core.data.item.TemplateItem;
import com.stw.protorype.R;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    private static final String TAG = "TemplateAdapter";
    private Context mContext;
    private List<TemplateItem> templateItems;
    private boolean mIsRecentList;

    public TemplateAdapter(Context context, List<TemplateItem> items,  boolean isRecentList) {
        mContext = context;
        this.templateItems = items;
        mIsRecentList = isRecentList;
    }

    @NonNull
    @Override
    public TemplateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_item_view, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TemplateAdapter.ViewHolder holder, int position) {
        TemplateItem templateItem = templateItems.get(position);

        holder.bind(templateItem);
    }

    @Override
    public int getItemCount() {
        return templateItems.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName;
        private AppCompatImageView itemImage;
        private AppCompatImageView itemFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.template_item_title);
            itemImage = itemView.findViewById(R.id.template_item_image);
            itemFavorite = itemView.findViewById(R.id.template_item_favorite);
        }

        void bind(TemplateItem templateItem) {

            if(templateItem != null) {

                itemName.setText(templateItem.getLabel());

                Bitmap favoriteIcon = STWTemplateManager.getInstance().geTemplateIcon(templateItem.getTemplateUUID(), TemplateIconType.BIG);

                if (favoriteIcon != null) {
                    itemImage.setImageBitmap(favoriteIcon);
                }

                itemFavorite.setVisibility(mIsRecentList? View.GONE : View.VISIBLE);

                itemFavorite.setSelected(templateItem.getFavoriteSet());

                itemFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isFavorite = !templateItem.getFavoriteSet();

                        templateItem.setFavoriteSet(isFavorite);

                        if (isFavorite) {
                            STWTemplateManager.getInstance().addTemplateToFavorite(mContext, templateItem.getTemplateUUID(), new STWOperationCallback() {
                                @Override
                                public void onError(@NonNull Error error) {
                                    Log.w(TAG, error.getMessage());
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "Template added to favorites");
                                }
                            });

                        } else {
                            STWTemplateManager.getInstance().removeTemplateFromFavorite(mContext, templateItem.getTemplateUUID(),
                                    new STWOperationCallback(){
                                        @Override
                                        public void onError(@NonNull Error error) {
                                            Log.w(TAG, error.getMessage());
                                        }

                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "Template removed from favorites");
                                        }
                                    });
                        }
                        itemFavorite.setSelected(isFavorite);
                    }
                });
            }
        }
    }

}