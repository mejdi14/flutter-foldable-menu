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

import java.util.List;

import com.streamwide.smartms.lib.core.api.contact.ContactsError;
import com.streamwide.smartms.lib.core.api.contact.STWContactFilter;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.contact.STWContactSearchCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactSettingRequestCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactUpdateListener;
import com.streamwide.smartms.lib.core.api.contact.STWLoadContactsCallback;
import com.streamwide.smartms.lib.core.api.contact.STWSearchController;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.stw.protorype.R;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SmartMSContactsFragment extends Fragment implements SearchView.OnQueryTextListener, ContactListActivity.IOnBackPressed, STWContactUpdateListener{

    private static final String TAG = "DemoNoBusinessContact";

    private static final int READ_CONTACT_REQUEST_CODE = 0x100;

    private RecyclerView mContactRecyclerView;

    private ProgressBar mProgress;

    private TextView mEmptyText;
    private Button mPublicVisibilityButton;

    private SearchView mSearchView;
    /**
     * For the MGM contacts list adapter
     */
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = View.inflate(getActivity(), R.layout.fragment_smartms_contact_list, null);
        mContactRecyclerView = view.findViewById(R.id.fragment_smartms_contact_recycler);
        mProgress = view.findViewById(R.id.fragment_smartms_contact_progress);

        mEmptyText = view.findViewById(R.id.fragment_smartms_contact_empty_view_text);
        mPublicVisibilityButton = view.findViewById(R.id.fragment_smartms_contact_public_visibility_btn);

        // Configuring the recyclerView properties.
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mContactRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mEmptyText.setVisibility(View.GONE);
        mPublicVisibilityButton.setVisibility(View.GONE);

        /**
         * READ_CONTACTS Permission is required to retrieve local contacts
         */
        boolean readContactPermissionGranted =
                ContactUtils.checkPermissions(getContext(), Manifest.permission.READ_CONTACTS);

        if (readContactPermissionGranted) {
            mProgress.setVisibility(View.VISIBLE);
            mEmptyText.setVisibility(View.GONE);
            loadContactList();
        }else{
            Log.d(TAG,"Request read contact permission for marshmallow device");
            requestPermissions(new String[] { Manifest.permission.READ_CONTACTS },
                    READ_CONTACT_REQUEST_CODE);
            mProgress.setVisibility(View.GONE);
            mEmptyText.setVisibility(View.VISIBLE);
        }

        initSearchController();

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

                displayContacts(smartMsContacts);
            }
        }, 50);
    }

    private void loadContactList()
    {
        if(STWContactManager.getInstance().isPublicVisibilityEnabled(getActivity())){

            Log.d(TAG, "PublicVisibility | Allowed");

            mEmptyText.setVisibility(View.GONE);
            mPublicVisibilityButton.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            STWContactManager.getInstance().refreshSystemContacts();
            STWContactManager.getInstance().syncSystemContacts();
            STWContactManager.getInstance().getSmartMSContacts(getContext(), new STWLoadContactsCallback() {
                @Override
                public void onError(ContactsError error) {
                    Log.e(TAG, "error : "+ error.getMessage());
                }

                @Override
                public void onComplete(List<ContactItem> list) {
                    displayContacts(list);
                }
            });
        }else{

            Log.d(TAG, "PublicVisibility not disabled");

            mEmptyText.setText("The Public visibility feature is mandatory to get SmartMs contacts\nPress button to enable the public visibility and refresh page");
            mPublicVisibilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    STWContactManager.getInstance().setUserPublicVisibility(true, new STWContactSettingRequestCallback() {
                        @Override
                        public void onError(ContactsError error) {
                            Toast.makeText(getContext(),"An error was encountered while trying to activate public visibility", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onSuccess() {
                            mEmptyText.setVisibility(View.GONE);
                            mPublicVisibilityButton.setVisibility(View.GONE);
                            loadContactList();
                        }
                    });
                }
            });
            mEmptyText.setVisibility(View.VISIBLE);
            mPublicVisibilityButton.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
        }
    }

    private void displayContacts(List<ContactItem> data)
    {
        mProgress.setVisibility(View.GONE);
        if (data == null) {
            Log.w(TAG, "displayContacts : data is null");
            return;
        }

        mContactsAdapter = new ContactAdapter(getActivity(), data, mOnItemClickListener);
        mContactRecyclerView.setAdapter(mContactsAdapter);

        showEmptyText(data.isEmpty());
    }

    private void showEmptyText(boolean isVisible){

        if(isVisible) {
            mEmptyText.setText("No contact available");
            mEmptyText.setVisibility(View.VISIBLE);
        }else{
            mEmptyText.setVisibility(View.GONE);
            mEmptyText.setText("");
        }

        mPublicVisibilityButton.setVisibility(View.GONE);
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

        mSearchController.filter(getActivity(), STWContactFilter.ALL);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(READ_CONTACT_REQUEST_CODE == requestCode){
            if(PackageManager.PERMISSION_GRANTED == grantResults[0]){
                mProgress.setVisibility(View.VISIBLE);
                loadContactList();
            }else{
                mProgress.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.VISIBLE);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onGroupListUpdated() {
        //Nothing to do here As SmartMS contact cannot be group
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
