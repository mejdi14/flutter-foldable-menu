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

import com.streamwide.smartms.lib.core.api.STWOperationCallback;
import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettings;
import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettingsError;
import com.streamwide.smartms.lib.core.api.account.settings.STWDisplayNameOrder;
import com.streamwide.smartms.lib.core.api.account.settings.STWDisplayNameOrderListener;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.stw.protorype.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";
    //list of fragments
    private Fragment[] mFragments;

    private FragmentRefreshListener mFragmentRefreshListener;

    //Interface to handle back click from fragments.
    public interface IOnBackPressed {
        /**
         * Called to dispatch the back pressed event to fragments.
         *
         * @return true if the back event was consumed by fragment, false to close activity.
         */
        boolean onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_list);

        //initialize fragment list
        mFragments = new Fragment[3];
        mFragments[0] = new BusinessContactsFragment();
        mFragments[1] = new SmartMSContactsFragment();
        mFragments[2] = new LocalContactsFragment();

        //Add BusinessContactsFragment by default.
        setTitle("Company Contacts");
        // Let's first dynamically add a fragment into a frame container
        displayFragment(mFragments[0]);

        STWAccountSettings.getInstance().registerToDisplayNameOrderEvents(mDisplayNameOrderCallback);
    }


    private void displayFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contact_fragment, fragment);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_business_contacts) {
            displayFragment(mFragments[0]);
            setTitle("Company Contacts");
        } else if (item.getItemId() == R.id.menu_smartms_contacts) {
            displayFragment(mFragments[1]);
            setTitle("SmartMS Contacts");
        } else if (item.getItemId() == R.id.menu_local_contacts) {
            displayFragment(mFragments[2]);
            setTitle("Local Contacts");
        } else if (item.getItemId() == R.id.menu_sort_contacts) {
            showDisplayNameOrderDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contact_fragment);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void showDisplayNameOrderDialog() {
        final String[] displayNameOrderChoicesList =
                getResources().getStringArray(R.array.setting_display_name_order_options);
        int displayNameOrder = STWContactManager.getInstance().isReverseContactNameDisplayEnabled(this) ? 1 : 0;

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle(getResources().getString(R.string.dialog_display_name_order_title));
        mBuilder.setSingleChoiceItems(displayNameOrderChoicesList, displayNameOrder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newDisplayNameOrder;
                if (i == 0) {
                    newDisplayNameOrder = STWDisplayNameOrder.FIRST_NAME;
                } else {
                    newDisplayNameOrder = STWDisplayNameOrder.LAST_NAME;
                }
                updateDisplayNameOrder(newDisplayNameOrder);
                dialogInterface.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void updateDisplayNameOrder(String newDisplayNameOrder) {
        STWAccountSettings.getInstance().updateDisplayNameOrder(this, newDisplayNameOrder,
                new STWOperationCallback<STWAccountSettingsError>() {

                    @Override
                    public void onError(STWAccountSettingsError error) {
                    }

                    @Override
                    public void onSuccess() {
                        if(mFragmentRefreshListener!=null){
                            mFragmentRefreshListener.onRefresh();
                        }
                    }
                });
    }


    private STWDisplayNameOrderListener mDisplayNameOrderCallback = new STWDisplayNameOrderListener() {
        @Override
        public void onDisplayNameOrderUpdated(String displayNameOrder) {
            refreshDisplayNameOrder(displayNameOrder);
        }
    };

    private void refreshDisplayNameOrder(String displayNameOrder)
    {
        if(mFragmentRefreshListener!=null){
            mFragmentRefreshListener.onRefresh();
        }
    }

    @Override
    protected void onDestroy() {
        STWAccountSettings.getInstance().unregisterFromDisplayNameOrderEvents(mDisplayNameOrderCallback);
        super.onDestroy();
    }

    public interface FragmentRefreshListener{
        void onRefresh();
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.mFragmentRefreshListener = fragmentRefreshListener;
    }

}