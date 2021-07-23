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
 * @lastModifiedOn jeu., 26 déc. 2019 15:10:49 +0100
 */

package com.stw.protorype.ui.activity.contact;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettings;
import com.streamwide.smartms.lib.core.api.contact.ContactsError;
import com.streamwide.smartms.lib.core.api.contact.STWContactDetailsCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.DefaultContactIconResources;
import com.streamwide.smartms.lib.core.data.item.OperationalStatusItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.RoleIconItem;
import com.stw.protorype.R;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

public class SingleContactActivity extends AppCompatActivity implements STWContactDetailsCallback {

    private static final String TAG = "DemoContactDetails";

    private AppCompatImageView mContactImage;

    private TextView mContactFirstName;
    private TextView mContactLastName;

    private LinearLayout mContactPositionContainer;
    private TextView mContactPosition;

    private LinearLayout mContactStatusContainer;
    private TextView mContactStatus;

    private LinearLayout mContactOperationalStatusContainer;
    private TextView mContactOperationalStatus;

    private TextView mContactPhoneNumber;
    private TextView mContactEmail;
    private LinearLayout mContactGroups;

    private LinearLayout mContactRoleIconContainer;
    private TextView mContactRoleIconName;
    private AppCompatImageView mContactRoleIconPicture;

    private ContactItem mCurrentContactItem = null;
    private PhoneItem mPhoneItem;

    private AsyncTask mAsyncTask;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_contact);

        Log.i(TAG,"Contact business detail activity is created");

        initView();
        initResolver(getIntent());
        initEvent();
        initData();
    }

    private void initResolver(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mCurrentContactItem = extras.getParcelable("contact_item");
            }
        }
    }

    private void initView()
    {
        mContactImage = findViewById(R.id.activity_single_contact_picture);
        mContactFirstName = findViewById(R.id.demo_contact_details_first_name);
        mContactLastName = findViewById(R.id.demo_contact_details_last_name);
        mContactPosition = findViewById(R.id.demo_contact_details_position);
        mContactPositionContainer = findViewById(R.id.demo_contact_details_position_container);
        mContactStatus = findViewById(R.id.demo_contact_details_status);
        mContactStatusContainer = findViewById(R.id.demo_contact_details_status_container);
        mContactOperationalStatus = findViewById(R.id.demo_contact_details_operational_status);
        mContactOperationalStatusContainer = findViewById(R.id.demo_contact_details_operational_status_container);
        mContactRoleIconContainer = findViewById(R.id.demo_contact_details_role_icon_container);
        mContactRoleIconName = findViewById(R.id.demo_contact_details_role_icon_name);
        mContactRoleIconPicture = findViewById(R.id.demo_contact_details_role_icon_picture);
        mContactPhoneNumber = findViewById(R.id.demo_contact_details_phone_number);
        mContactEmail = findViewById(R.id.demo_contact_details_email);
        mContactGroups = findViewById(R.id.demo_contact_details_Groups);
    }

    private void initData()
    {
        if (mAsyncTask != null) {
            Log.i(TAG,"cancel Http task : mGetContactBusinessDetailHttpTask ");
            mAsyncTask.cancel(true);
        }

        displayContactDetails(mCurrentContactItem);
    }

    private void initEvent(){

        mContactGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SingleContactActivity.this, GroupListActivity.class);
                intent.putExtra("contact_item", mCurrentContactItem);
                startActivity(intent);
            }
        });
    }

    private void handleUpdateContactDetailsFromServer(String phoneNumber)
    {
        Log.i(TAG,"get Contact details");

        mAsyncTask = STWContactManager.getInstance().getContactDetails(this, phoneNumber, this);
    }

    private void displayContactDetails(ContactItem contactItem)
    {
        if (contactItem == null) {
            return;
        }
        mCurrentContactItem = contactItem;
        handlePhoneContact();
        handleDetailsContact();
        DefaultContactIconResources defaultContactIconResources = ContactUtils.getDefaultIconResources();
        STWContactManager.getInstance().loadContactPicture(this,mContactImage, mCurrentContactItem, defaultContactIconResources);

        /**
         * check if need to fetch data from server or from local db
         */
        long currentLastLocalUpdate = 0;
        String lastLocalUpdateValue = mCurrentContactItem.getLastLocalUpdate();
        if (!ContactUtils.isEmpty(lastLocalUpdateValue)) {
            currentLastLocalUpdate = Long.valueOf(lastLocalUpdateValue);
        }
        long currentTimeStamps = System.currentTimeMillis();
        long deltaTime = currentTimeStamps - currentLastLocalUpdate;
        long updateContactDetailsDelay = STWContactManager.getInstance().getContactDetailCacheLifeTime(this);

        if (TimeUnit.MILLISECONDS.toSeconds(deltaTime) >= updateContactDetailsDelay) {
            Log.i(TAG,"get contact details from server");

            if(mPhoneItem!= null) {
                handleUpdateContactDetailsFromServer(mPhoneItem.getInternationalNumber());
            }
        }

    }


    private void handlePhoneContact()
    {
        if (mCurrentContactItem != null ) {

            List<PhoneItem> phoneList = STWContactManager.getInstance().getContactPhones(getApplicationContext(),mCurrentContactItem);

            if (phoneList != null && !phoneList.isEmpty()) {
                for (PhoneItem phoneItem : phoneList) {
                    mPhoneItem = phoneItem;
                    if(mPhoneItem!= null){
                        mContactPhoneNumber.setText(phoneItem.getInternationalNumber());

                        break;
                    }
                }
            } else {
                Log.i(TAG,"Phones list is null or empty for the current contact");
            }
        }
    }

    private void handleDetailsContact()
    {

        if (mCurrentContactItem == null) {
            Log.i(TAG,"current contact is null ");
            return;
        }

        String contactName = STWContactManager.getInstance().getDisplayNameForContactItem(this,mCurrentContactItem);

        if (!ContactUtils.isEmpty(contactName)&& getSupportActionBar() != null) {
            getSupportActionBar().setTitle(contactName);
        }

        mContactFirstName.setText(mCurrentContactItem.getFirstName());
        mContactLastName.setText(mCurrentContactItem.getLastName());

        String status = mCurrentContactItem.getStatus();
        mContactStatus.setText(status);
        mContactStatusContainer.setVisibility(TextUtils.isEmpty(status)? View.GONE : View.VISIBLE);

        // Operational status feature :

        //Check if operational status feature is allowed by the administrator or not
        boolean isOperationalStatusAllowed = STWAccountSettings.getInstance().isOperationalStatusAllowed(this);

        int operationalStatusId = mCurrentContactItem.getOperationalStatus();

        if(isOperationalStatusAllowed && operationalStatusId > 0){

            OperationalStatusItem operationalStatusItem =
                    STWContactManager.getInstance().getOperationalStatusByIdentifier(this, operationalStatusId);

            if(operationalStatusItem == null){
                mContactOperationalStatusContainer.setVisibility(View.GONE);
            }else{

                String operationalStatusToShow = operationalStatusItem.getCode()+" - "+ operationalStatusItem.getLabel();

                mContactOperationalStatus.setText(operationalStatusToShow);
                mContactOperationalStatusContainer.setVisibility(View.VISIBLE);
            }
        }else{
            mContactOperationalStatusContainer.setVisibility(View.GONE);
        }

        // Icon per role feature :

        //Check if Icon per role feature is allowed by the administrator or not
        boolean isRoleIconFeatureAllowed = STWContactManager.getInstance().isIconPerRoleFeatureAllowed(this);

        if(isRoleIconFeatureAllowed && mPhoneItem != null){

            RoleIconItem roleIconItem = STWContactManager.getInstance().getRoleIconByPhone(this, mPhoneItem);

            if(roleIconItem == null){
                mContactRoleIconContainer.setVisibility(View.GONE);
            }else{

                mContactRoleIconName.setText(roleIconItem.getName());

                new Thread(() -> {
                    Bitmap roleIconPicture = STWContactManager.getInstance().getRoleIconPicture(roleIconItem.getRoleIconId());
                    // or you can also get the role icon id from phoneItem directly mPhoneItem.getRoleIcon()

                    mContactRoleIconPicture.setImageBitmap(roleIconPicture);
                }).start();

                mContactRoleIconContainer.setVisibility(View.VISIBLE);
            }
        }else{

            mContactRoleIconContainer.setVisibility(View.GONE);
        }

        String position = mCurrentContactItem.getPosition();
        mContactPosition.setText(position);
        mContactPositionContainer.setVisibility(TextUtils.isEmpty(status)? View.GONE : View.VISIBLE);

        mContactEmail.setText(mCurrentContactItem.getEmail());


        if (ContactUtils.isEmpty(mCurrentContactItem.getContactIds())) {
            mContactGroups.setVisibility(View.GONE);
        } else {
            mContactGroups.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG,"Contact business detail screen is destroyed");
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onComplete(ContactItem contact) {
        Log.i(TAG,"onSuccess : get contact business detail");

        if (contact == null) {
            Log.i(TAG,"Contact item no longer exists");
            finish();
        } else {
            displayContactDetails(contact);
        }
    }

    @Override
    public void onError(ContactsError error) {
        Log.i(TAG,"error : get contact business detail");
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
    }
}
