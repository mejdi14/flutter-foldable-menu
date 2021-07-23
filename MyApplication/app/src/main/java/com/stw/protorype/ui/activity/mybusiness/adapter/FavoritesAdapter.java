/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:00:58 +0100
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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.api.mybusiness.TemplateIconType;
import com.streamwide.smartms.lib.core.data.item.TemplateItem;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.mybusiness.ItemMoveCallback;

import java.util.Collections;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    private Context mContext;
    private List<TemplateItem> mTemplateItems;

    public FavoritesAdapter(Context context, List<TemplateItem> items) {
        mContext = context;
        this.mTemplateItems = items;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_item_view, parent, false);
        return new  ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemplateItem templateItem = mTemplateItems.get(position);

        Bitmap favoriteIcon = STWTemplateManager.getInstance().geTemplateIcon(templateItem.getTemplateUUID(), TemplateIconType.BIG);

        holder.bind(templateItem.getLabel(), favoriteIcon);
    }


    @Override
    public int getItemCount() {
        return mTemplateItems.size();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mTemplateItems, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mTemplateItems, i, i - 1);
            }
        }
        STWTemplateManager.getInstance().changeTemplateFavoriteListOrder(mContext, mTemplateItems, null);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ViewHolder holder) {
        holder.itemRow.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(ViewHolder holder) {
        holder.itemRow.setBackgroundColor(Color.TRANSPARENT);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mItemName;
        private AppCompatImageView mItemImage;
        private AppCompatImageView mItemFavorite;
        LinearLayout itemRow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemRow = itemView.findViewById(R.id.template_item_row);
            mItemName = itemView.findViewById(R.id.template_item_title);
            mItemImage = itemView.findViewById(R.id.template_item_image);
            mItemFavorite = itemView.findViewById(R.id.template_item_favorite);
        }

        void bind(@Nullable String item, @Nullable Bitmap icon) {

            mItemFavorite.setVisibility(View.GONE);
            mItemName.setText(item);

            if (icon != null) {
                mItemImage.setImageBitmap(icon);
            }
        }
    }
}
