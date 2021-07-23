/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on mar., 14 avr. 2020 15:08:53 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn mar., 14 avr. 2020 15:08:51 +0100
 */

package com.stw.protorype.ui.activity.mybusiness;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessFilter;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessManager;
import com.streamwide.smartms.lib.core.api.mybusiness.STWProcessTab;
import com.streamwide.smartms.lib.core.api.mybusiness.STWTemplateManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.TemplateCategoryItem;
import com.stw.protorype.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddFilterActivity extends AppCompatActivity {

    private String CLASS_NAME = "AddFilterActivity";
    private String TAG = "Filter";

    private Button mTabBtn;
    private Button mFilterBtn;

    private TextView mSelectedTabTv;
    private TextView mSelectedFilterTv;

    private EditText mDatePickerSelector;
    private EditText mPrioritySelector;
    private EditText mCategorySelector;
    private EditText mPhoneSelector;

    private @STWProcessTab int selectedTab;
    private @STWProcessFilter int selectedFilter;
    private String data;

    private int mSelectedTabPosition = -1;
    private int mSelectedFilterPosition = -1;


    final Calendar mCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, monthOfYear);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_filter);

        setTitle("Add filter");

        initView();
        initEvent();
    }

    private void initView() {

        mTabBtn = findViewById(R.id.add_filter_select_tab_btn);
        mSelectedTabTv = findViewById(R.id.add_filter_selected_tab);
        mFilterBtn = findViewById(R.id.add_filter_select_filter_btn);
        mSelectedFilterTv = findViewById(R.id.add_filter_selected_filter);

        mPrioritySelector = findViewById(R.id.add_filter_Priority);
        mCategorySelector = findViewById(R.id.add_filter_category);
        mPhoneSelector = findViewById(R.id.add_filter_phone);
        mDatePickerSelector = findViewById(R.id.add_filter_date_picker);

        mPrioritySelector.setVisibility(View.GONE);
        mCategorySelector.setVisibility(View.GONE);
        mPhoneSelector.setVisibility(View.GONE);
        mDatePickerSelector.setVisibility(View.GONE);
    }

    private void initEvent() {
        mDatePickerSelector.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new DatePickerDialog(AddFilterActivity.this, date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        mTabBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectTab();
            }
        });

        mFilterBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectFilter();
            }
        });

        mPrioritySelector.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectPriority();
            }
        });

        mCategorySelector.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectCategory();
            }
        });

        mPhoneSelector.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectPhones();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_filter_add) {

            if(selectedTab == 0 || selectedFilter == 0 || data == null || data.isEmpty()){
                Toast.makeText(this, "complete missing parameters", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }

            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "showStatusDialog"), TAG,
                    "selectedTab : " + selectedTab+ ", selectedFilter : "+selectedFilter+", data : "+data);

            STWProcessManager.getInstance().filter(this, selectedTab, selectedFilter, data);

            Toast.makeText(this, "Filter has been added successfully", Toast.LENGTH_SHORT).show();

            reset();
        }

        return super.onOptionsItemSelected(item);
    }

    private void reset(){

        mPrioritySelector.setVisibility(View.GONE);
        mCategorySelector.setVisibility(View.GONE);
        mPhoneSelector.setVisibility(View.GONE);
        mDatePickerSelector.setVisibility(View.GONE);

        mSelectedTabPosition = -1;
        mSelectedFilterPosition = -1;

        selectedTab = 0;
        selectedFilter = 0;

        mSelectedTabTv.setText("");
        mSelectedFilterTv.setText("");

        mPrioritySelector.setText("");
        mCategorySelector.setText("");
        mPhoneSelector.setText("");
        mDatePickerSelector.setText("");
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        mDatePickerSelector.setText(sdf.format(mCalendar.getTime()));

        data = String.valueOf(mCalendar.getTimeInMillis());
    }

    private void selectTab(){
        final CharSequence[] items = new CharSequence[]{"IN PROGRESS", "SUBMITTED", "COMPLETED", "CANCELED", "NEW", "DRAFT"};

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, mSelectedTabPosition, null)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        mSelectedTabPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        selectedTab = getSelectedTab(mSelectedTabPosition);
                        mSelectedTabTv.setText(items[mSelectedTabPosition]);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void selectFilter(){
        final CharSequence[] items = new CharSequence[]{"BY OWNER NAME", "BY INITIATOR NAME", "BY RECEIVER NAME", "BY START DATE", "BY DUE DATE", "BY PRIORITY", "BY_CATEGORY"};

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, mSelectedFilterPosition, null)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        mSelectedFilterPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        selectedFilter = getSelectedFilter(mSelectedFilterPosition);
                        mSelectedFilterTv.setText(items[mSelectedFilterPosition]);
                        displayDataSelector(mSelectedFilterPosition);

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void selectPriority(){

        final CharSequence[] items = new CharSequence[]{"Low Priority", "Medium priority", "High priority"};
        final boolean[] checkedItems = new boolean[]{
                false,
                false,
                false
        };

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mPrioritySelector.setText("");
                        data = "";

                        StringBuilder str = new StringBuilder();

                        for (int i = 0; i<checkedItems.length; i++){
                            boolean checked = checkedItems[i];
                            if (checked) {

                                if(str.length()>0) str.append(",");
                                str.append(i+1);
                            }
                        }

                        data = str.toString();
                        mPrioritySelector.setText(data);

                        dialog.dismiss();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void selectCategory(){

        List<TemplateCategoryItem> templateCategoryList = STWTemplateManager.getInstance().getTemplateCategoryList(this);

        if(templateCategoryList == null){
            return;
        }

        List<String> popupList = new ArrayList<>();

        for(TemplateCategoryItem templateCategoryItem : templateCategoryList){
            popupList.add(templateCategoryItem.getCategoryName());
        }

        final CharSequence[] items = popupList.toArray(new CharSequence[popupList.size()]);

        final boolean[] checkedItems = new boolean[items.length];

        for (int i = 0; i<checkedItems.length; i++){
            checkedItems[i] = false;
        }

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mCategorySelector.setText("");
                        data = "";

                        StringBuilder str = new StringBuilder();

                        for (int i = 0; i<checkedItems.length; i++){
                            boolean checked = checkedItems[i];
                            if (checked) {
                                if(str.length()>0) str.append(",");
                                str.append(templateCategoryList.get(i).getCategoryUUID());

                            }
                        }
                        data = str.toString();
                        mCategorySelector.setText(data);
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void selectPhones(){

        List<ContactItem> contactList = STWContactManager.getInstance().getCompanyContacts(this, STWContactFilter.SINGLE);

        if(contactList == null){
            return;
        }

        List<String> popupList = new ArrayList<>();

        for(ContactItem contactItem : contactList){
            popupList.add(STWContactManager.getInstance().getDisplayNameForContactItem(this, contactItem));
        }

        final CharSequence[] items = popupList.toArray(new CharSequence[popupList.size()]);

        final boolean[] checkedItems = new boolean[items.length];

        for (int i = 0; i<checkedItems.length; i++){
            checkedItems[i] = false;
        }

        new AlertDialog.Builder(this)
                .setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        mPhoneSelector.setText("");
                        data = "";

                        StringBuilder str = new StringBuilder();

                        for (int i = 0; i<checkedItems.length; i++){
                            boolean checked = checkedItems[i];
                            if (checked) {

                                String contactPhone = getContactPhone(contactList.get(i));

                                if(contactPhone!= null) {
                                    if (str.length()>0) str.append(",");
                                    str.append(contactPhone);
                                }

                            }
                        }

                        data = str.toString();
                        mPhoneSelector.setText(data);
                        dialog.dismiss();

                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private int getSelectedTab(int selectedPosition) {

        switch (selectedPosition){
            case 0 :
                return STWProcessTab.IN_PROGRESS;
            case 1:
                return STWProcessTab.SUBMITTED;
            case 2:
                return STWProcessTab.COMPLETED;
            case 3:
                return STWProcessTab.CANCELED;
            case 4:
                return STWProcessTab.NEW;
            case 5:
                return STWProcessTab.DRAFT;
            default :
                    return 0;
        }
    }

    private int getSelectedFilter(int selectedPosition) {

        switch (selectedPosition){
            case 0 :
                return STWProcessFilter.BY_OWNER_NAME;
            case 1:
                return STWProcessFilter.BY_INITIATOR_NAME;
            case 2:
                return STWProcessFilter.BY_RECEIVER_NAME;
            case 3:
                return STWProcessFilter.BY_START_DATE;
            case 4:
                return STWProcessFilter.BY_DUE_DATE;
            case 5:
                return STWProcessFilter.BY_PRIORITY;
            case 6:
                return STWProcessFilter.BY_CATEGORY;
            default :
                return 0;
        }
    }

    private void displayDataSelector(int selectedPosition){

        switch (selectedPosition){
            case 0 :  case 1:  case 2:
                mPrioritySelector.setVisibility(View.GONE);
                mCategorySelector.setVisibility(View.GONE);
                mPhoneSelector.setVisibility(View.VISIBLE);
                mDatePickerSelector.setVisibility(View.GONE);
                break;
            case 3: case 4:
                mPrioritySelector.setVisibility(View.GONE);
                mCategorySelector.setVisibility(View.GONE);
                mPhoneSelector.setVisibility(View.GONE);
                mDatePickerSelector.setVisibility(View.VISIBLE);
                break;
            case 5:
                mPrioritySelector.setVisibility(View.VISIBLE);
                mCategorySelector.setVisibility(View.GONE);
                mPhoneSelector.setVisibility(View.GONE);
                mDatePickerSelector.setVisibility(View.GONE);
                break;
            case 6:
                mPrioritySelector.setVisibility(View.GONE);
                mCategorySelector.setVisibility(View.VISIBLE);
                mPhoneSelector.setVisibility(View.GONE);
                mDatePickerSelector.setVisibility(View.GONE);
                break;
            default :
                mPrioritySelector.setVisibility(View.GONE);
                mCategorySelector.setVisibility(View.GONE);
                mPhoneSelector.setVisibility(View.GONE);
                mDatePickerSelector.setVisibility(View.GONE);
                break;
        }

        data = "";
    }

    private String getContactPhone(ContactItem contactItem)
    {
            List<PhoneItem> phoneList = STWContactManager.getInstance().getContactPhones(getApplicationContext(),contactItem);

            if (phoneList != null && !phoneList.isEmpty()) {
                for (PhoneItem phoneItem : phoneList) {
                    if(phoneItem!= null){
                        return phoneItem.getInternationalNumber();
                    }
                }
            }

            return null;
    }
}
