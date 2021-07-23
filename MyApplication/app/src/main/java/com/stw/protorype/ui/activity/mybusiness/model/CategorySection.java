/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:02:44 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 10 avr. 2020 11:06:53 +0100
 */

package com.stw.protorype.ui.activity.mybusiness.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.streamwide.smartms.lib.core.data.item.MBTemplateCategoryModel;
import com.streamwide.smartms.lib.core.data.item.TemplateCategoryItem;
import com.streamwide.smartms.lib.core.data.item.TemplateItem;

import java.util.ArrayList;
import java.util.List;

public class CategorySection extends MBTemplateCategoryModel {

    public CategorySection(@NonNull TemplateCategoryItem templateCategoryItem, @Nullable List<TemplateItem> templateItemList) {
        super(templateCategoryItem, templateItemList);
    }

    public String getSectionTitle()
    {
        return "hello";
    }

    public List<TemplateItem> getAllItemsInSection()
    {
        return new ArrayList<>();
    }
}
