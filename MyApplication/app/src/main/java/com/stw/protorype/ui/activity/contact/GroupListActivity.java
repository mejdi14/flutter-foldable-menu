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
 * @lastModifiedOn jeu., 26 déc. 2019 15:10:48 +0100
 */

package com.stw.protorype.ui.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.streamwide.smartms.lib.core.api.contact.ContactsError;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.contact.STWLoadContactsCallback;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.stw.protorype.R;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupListActivity extends AppCompatActivity {

    private static final String TAG = "GroupListActivity";

    private RecyclerView mContactRecyclerView;
    private ProgressBar mProgress;
    private TextView mEmptyView;

    private ContactAdapter mDemoContactsAdapter;
    private ContactItem mContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_detail);

        initResolver(getIntent());
        initView();
        initData();
    }

    private void initResolver(Intent intent)
    {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mContact = extras.getParcelable("contact_item");
        }
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
    private void initData()
    {
        mProgress.setVisibility(View.VISIBLE);
        STWContactManager.getInstance().getGroupsOfContact(this, mContact, new STWLoadContactsCallback() {
            @Override
            public void onComplete(List<ContactItem> list) {
                mDemoContactsAdapter = new ContactAdapter(GroupListActivity.this, list, null);
                mContactRecyclerView.setAdapter(mDemoContactsAdapter);
                mProgress.setVisibility(View.GONE);
            }

            @Override
            public void onError(ContactsError error) {

            }
        });
    }

}
