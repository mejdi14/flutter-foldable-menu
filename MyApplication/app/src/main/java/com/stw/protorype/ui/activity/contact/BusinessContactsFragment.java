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
 * @lastModifiedOn jeu., 26 déc. 2019 15:10:52 +0100
 */

package com.stw.protorype.ui.activity.contact;

import java.util.List;

import com.streamwide.smartms.lib.core.api.contact.ContactsError;
import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.contact.STWContactSearchCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactUpdateListener;
import com.streamwide.smartms.lib.core.api.contact.STWLoadContactsCallback;
import com.streamwide.smartms.lib.core.api.contact.STWSearchController;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.stw.protorype.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class BusinessContactsFragment extends Fragment implements SearchView.OnQueryTextListener, ContactListActivity.IOnBackPressed, STWContactUpdateListener {

    private static final String TAG = "DemoBusinessContact";

    private RecyclerView mContactRecyclerView;

    private ProgressBar mProgress;

    private TextView mEmptyView;

    private SearchView mSearchView;

    private ContactAdapter mContactsAdapter;

    /**
     * store user search input
     */
    private String mSearchContent;

    private STWSearchController mSearchController;


    private ContactAdapter.OnItemClickListener mOnItemClickListener = new ContactAdapter.OnItemClickListener() {
        @Override
        public void onItemClicked(View v, int position, ContactItem contactItem) {
            Intent intent;
            if(contactItem.isGroup()){
                intent = new Intent(getActivity(), GroupContactActivity.class);
                intent.putExtra("group_id", String.valueOf(contactItem.getGroupId()));
            }else {
                intent = new Intent(getActivity(), SingleContactActivity.class);
                intent.putExtra("contact_item", contactItem);
            }
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = View.inflate(getActivity(), R.layout.fragment_contact_list, null);
        mContactRecyclerView = view.findViewById(R.id.fragment_contact_recycler);
        mProgress = view.findViewById(R.id.fragment_contact_progress);
        mEmptyView = view.findViewById(R.id.fragment_contact_empty_view);

        // Configuring the recyclerView properties.
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mEmptyView.setVisibility(View.GONE);

        initSearchController();
        loadContactList();
        refreshDisplayNameOrder();

        STWContactManager.getInstance().registerContactListUpdatesListener(this);
        return view;
    }

    private void initSearchController() {

        mSearchController = STWContactManager.getInstance().createSearchController(new STWContactSearchCallback() {
            @Override
            public void onError(ContactsError error) {

            }

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(List<ContactItem> businessContacts, List<ContactItem> smartMsContacts, String keyWord) {

                Log.d(TAG, "getSearchContactFromServer | onComplete()");

                if (businessContacts == null) {
                    Log.d(TAG,"contactList is null");

                    mContactsAdapter.notifyDataSetChanged();
                }
                displayContacts(businessContacts);
            }
        }, 50);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadContactList()
    {
        STWContactManager.getInstance().getCompanyContacts(getContext(), STWContactFilter.ALL, new STWLoadContactsCallback() {
            @Override
            public void onError(ContactsError error) {

            }

            @Override
            public void onComplete(List<ContactItem> list) {
                displayContacts(list);
                mProgress.setVisibility(View.GONE);
            }
        });
    }

    private void displayContacts(List<ContactItem> contactList)
    {

        if (contactList == null) {
            Log.w(TAG, "displayContacts : contactList is null");
            return;
        }

        mContactsAdapter = new ContactAdapter(getActivity(), contactList, mOnItemClickListener);
        mContactRecyclerView.setAdapter(mContactsAdapter);

        mEmptyView.setVisibility(contactList.isEmpty() ? View.VISIBLE :  View.GONE);
    }

    /**
     * search contacts data by user's input and change cursor,refresh UI
     */
    private void searchData(String data)
    {
        Log.i(TAG, "search contact data");

        mSearchContent = data;

        // search contact from local if searchContent is empty
        if(TextUtils.isEmpty(mSearchContent)){
            loadContactList();
            mSearchController.cancelSearch();
            return;
        }
        mSearchController.search(getActivity(), mSearchContent);

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
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        searchData(s);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        // close search view on back button pressed
        if (!mSearchView.isIconified()) {
            mSearchView.setIconified(true);
            return true;
        }
        mSearchContent = null;

        return false;
    }

    @Override
    public void onGroupListUpdated() {
        //Reload the contact list to get updated contacts
        loadContactList();
    }

    @Override
    public void onSubscriberListUpdated() {
        //Reload the contact list to get updated contacts
        loadContactList();
    }

    @Override
    public void onSingleContactsUpdated(String phoneNumber) {
        //Reload the contact list to get updated contacts
        loadContactList();
    }

    @Override
    public void onLocalContactUpdated() {
        //Nothing to do here As current screen concern only business contacts
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
