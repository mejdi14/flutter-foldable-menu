/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on jeu., 26 déc. 2019 15:13:16 +0100
 * @copyright  Copyright (c) 2019 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2019 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn jeu., 26 déc. 2019 15:10:50 +0100
 */

package com.stw.protorype.ui.activity.contact;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.streamwide.smartms.lib.core.api.contact.ContactsError;
import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.contact.STWGroupDetailsCallback;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.stw.protorype.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupContactActivity extends AppCompatActivity {

    private static final String TAG = "DemoGroupDetails";

    private RecyclerView mContactRecyclerView;
    private ProgressBar mProgress;
    private TextView mEmptyView;

    private String mGroupId;

    private ContactItem mContactItem;
    private List<ContactItem> mListContactItem;

    private ContactAdapter mDemoContactsAdapter;

    private AsyncTask mAsyncTask;
    private ContactGroupTask mContactGroupTask;

    private ContactAdapter.OnItemClickListener mOnItemClickListener = new ContactAdapter.OnItemClickListener() {
        @Override
        public void onItemClicked(View v, int position, ContactItem contactItem) {

            Intent intent;
            if(contactItem.isGroup()){
                intent = new Intent(GroupContactActivity.this, GroupContactActivity.class);
                intent.putExtra("group_id", String.valueOf(contactItem.getGroupId()));
            }else {
                intent = new Intent(GroupContactActivity.this, SingleContactActivity.class);
                intent.putExtra("contact_item", contactItem);
            }
            startActivity(intent);
        }
    };

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Contact Group Details activity is started");

        setContentView(R.layout.activity_group_detail);

        initResolver(getIntent());
        initView();

        initData();
    }

    public void initView()
    {
        mContactRecyclerView = findViewById(R.id.contact_list);
        mProgress = findViewById(R.id.group_detail_progress);
        mEmptyView = findViewById(R.id.group_detail_empty_view);

        // Configuring the recyclerView properties.

        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mContactRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mEmptyView.setVisibility(View.GONE);
    }

    private void initResolver(Intent intent)
    {
        // When contact is selected from default application contact and want to
        // send message from Bzoo application
        if (intent != null) {

            Bundle extras = intent.getExtras();

            if (extras != null) {
                mGroupId = extras.getString("group_id");
                Log.i(TAG,"Intent have extra = "+mGroupId);
            }
        }
    }

    private void initData()
    {
        mContactItem = STWContactManager.getInstance().getContactByGroupId(this, mGroupId);

        if (mContactItem != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(mContactItem.getGroupName());
            }
            Log.i(TAG,"get contact group details from local db");
        } else {
            Log.i(TAG,"Group contact is null for the given groupId");
        }

        initContactGroupTask();

    }
    private void initContactGroupTask()
    {
        mContactGroupTask = new ContactGroupTask(this, mContactItem);
        mContactGroupTask.execute();
    }


    STWGroupDetailsCallback mGroupDetailsCallback = new STWGroupDetailsCallback() {
        @Override
        public void onError(ContactsError contactsError) {
            Log.i(TAG,"error : get group detail");
            mProgress.setVisibility(View.GONE);
            Toast.makeText(GroupContactActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
            if (mAsyncTask != null) {
                mAsyncTask.cancel(true);
            }
        }

        @Override
        public void onComplete(ContactItem contactItem) {

            Log.d(TAG,"success : get group detail");

            if (contactItem == null) {
                Log.w(TAG,"Contact item no longer exists");
                return;
            }

            mContactItem = contactItem;
            getSupportActionBar().setTitle(mContactItem.getGroupName());

            initContactGroupTask();
        }
    };

    @Override
    protected void onDestroy()
    {
        Log.i(TAG,"group details is destroyed");
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }

        if(mContactGroupTask != null){
            mContactGroupTask.cancel(true);
        }
        super.onDestroy();
    }

    private class ContactGroupTask extends AsyncTask<Void, Integer, List<ContactItem>> {

        ContactItem mContactItem;
        Context mContext;

        public ContactGroupTask(Context context, ContactItem contactItem) {
            this.mContactItem = contactItem;
            this.mContext = context;
        }
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG ,"Start loader to Get the group contact details");
            mProgress.setVisibility(View.VISIBLE);
        }

        protected List<ContactItem> doInBackground(Void...arg0) {
            Log.d(TAG , " getting the group contact details in progress");

            List<ContactItem> contactItemList = new ArrayList<>();

            if (mContactItem != null) {
                if (mContactItem.getGroupId() == -1) {
                    contactItemList = STWContactManager.getInstance().getCompanyContacts(mContext, STWContactFilter.SINGLE);
                } else {
                    List<PhoneItem> listPhoneByContactGroup = ContactUtils.getPhoneNumbersInGroup(mContext, mContactItem);

                    if (listPhoneByContactGroup != null && !listPhoneByContactGroup.isEmpty()) {

                        contactItemList.addAll(ContactUtils.constructContactListFromPhoneList(mContext,listPhoneByContactGroup));
                    }

                    List<ContactItem> listContactByContactGroupOfGroups = ContactUtils.getGroupsByGroupId(mContext, mContactItem);

                    if (listContactByContactGroupOfGroups != null && !listContactByContactGroupOfGroups.isEmpty()) {
                        contactItemList.addAll(listContactByContactGroupOfGroups);
                    }
                }
            }
            return contactItemList;
        }

        protected void onPostExecute(List<ContactItem> contactItems) {
            super.onPostExecute(contactItems);

            Log.i(TAG,"End contact group details task");

            mProgress.setVisibility(View.GONE);

            mListContactItem = new ArrayList<>();
            if (contactItems != null && !contactItems.isEmpty()) {

                mListContactItem.addAll(contactItems);

                mDemoContactsAdapter = new ContactAdapter(mContext, mListContactItem, mOnItemClickListener);
                mContactRecyclerView.setAdapter(mDemoContactsAdapter);
            }

            /**
             * check if need to fetch data from server
             */
            long lastLocalUpdate = 0;

            if (mContactItem != null) {
                String lastLocalUpdateString = mContactItem.getLastLocalUpdate();
                if (!ContactUtils.isEmpty(lastLocalUpdateString)) {
                    lastLocalUpdate = Long.valueOf(lastLocalUpdateString);
                }
            }

            long currentTimeStamps = System.currentTimeMillis();
            long deltaTime = currentTimeStamps - lastLocalUpdate;
            long updateContactDetailsDelay = STWContactManager.getInstance().getContactDetailCacheLifeTime(mContext);

            if (!mGroupId.equals(String.valueOf(-1))
                    && (TimeUnit.MILLISECONDS.toSeconds(deltaTime) >= updateContactDetailsDelay
                    || mContactItem.getGroupIds() == null && mContactItem.getContactIds() == null)) {

                Log.i(TAG,"get contact group details from server");

                mAsyncTask = STWContactManager.getInstance().getGroupDetails(mContext, mGroupId, mGroupDetailsCallback);

            }
        }
    }
}
