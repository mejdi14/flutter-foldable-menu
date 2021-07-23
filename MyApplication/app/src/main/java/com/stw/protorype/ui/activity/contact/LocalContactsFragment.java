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

import java.util.List;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.contact.STWContactUpdateListener;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.stw.protorype.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LocalContactsFragment extends Fragment implements SearchView.OnQueryTextListener, ContactListActivity.IOnBackPressed, STWContactUpdateListener {

    private static final String TAG = "LocalContactsFragment";

    private static final int READ_CONTACT_REQUEST_CODE = 0x100;

    private RecyclerView mContactRecyclerView;

    private ProgressBar mProgress;

    private TextView mEmptyView;

    private SearchView mSearchView;
    private LocalContactAdapter mContactsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");

        setHasOptionsMenu(true);
        View view = View.inflate(getActivity(), R.layout.fragment_contact_list, null);
        mContactRecyclerView = view.findViewById(R.id.fragment_contact_recycler);
        mProgress = view.findViewById(R.id.fragment_contact_progress);
        mEmptyView = view.findViewById(R.id.fragment_contact_empty_view);

        // Configuring the recyclerView properties.
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mContactsAdapter = new LocalContactAdapter(getActivity(), null);
        mContactRecyclerView.setAdapter(mContactsAdapter);

        /**
         * READ_CONTACTS Permission is required to retrieve local contacts
         */
        boolean readContactPermissionGranted =
                ContactUtils.checkPermissions(getContext(), Manifest.permission.READ_CONTACTS);

        if (readContactPermissionGranted) {
            mProgress.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
            loadContactList();
        }else{
            Log.d(TAG,"Request read contact permission for marshmallow device");
            requestPermissions(new String[] { Manifest.permission.READ_CONTACTS },
                    READ_CONTACT_REQUEST_CODE);
            mProgress.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }

        refreshDisplayNameOrder();

        STWContactManager.getInstance().registerContactListUpdatesListener(this);
        return view;
    }
    private void loadContactList() {

        List<ContactItem> contactList = STWContactManager.getInstance().getAllLocalContacts(getContext());
        mContactsAdapter.changeContactList(contactList);
        mEmptyView.setVisibility(contactList.isEmpty() ? View.VISIBLE :  View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_search_contact, menu);
        MenuItem mSearch = menu.findItem(R.id.demo_menu_search_contact);
        mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search Contact");
        mSearchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onQueryTextSubmit(String s) {
        mContactsAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mContactsAdapter.getFilter().filter(s);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        // close search view on back button pressed
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(READ_CONTACT_REQUEST_CODE == requestCode){
            if(PackageManager.PERMISSION_GRANTED == grantResults[0]){
                mProgress.setVisibility(View.VISIBLE);
                loadContactList();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onGroupListUpdated() {
        //Nothing to do here As local contact cannot be group
    }

    @Override
    public void onSubscriberListUpdated() {
        //Nothing to do here As current screen concern only local contacts
    }

    @Override
    public void onSingleContactsUpdated(String phoneNumber) {
        //Nothing to do here As current screen concern only local contacts
    }

    @Override
    public void onLocalContactUpdated() {
        //Reload the contact list to get updated contacts
        loadContactList();
    }

    @Override
    public void onSubscribersDeletedFromOrganisation( List<String> list) {

    }

    @Override
    public void onDestroy() {
        STWContactManager.getInstance().unregisterContactListUpdatesListener(this);
        super.onDestroy();
    }

    private void refreshDisplayNameOrder(){
        ((ContactListActivity)getActivity()).setFragmentRefreshListener(new ContactListActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                loadContactList();
            }
        });
    }
}
