/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 10 Jan 2020 17:16:14 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 10 Jan 2020 17:16:00 +0100
 */

package com.stw.protorype.ui.activity.account;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.streamwide.smartms.lib.core.api.STWOperationCallback;
import com.streamwide.smartms.lib.core.api.account.STWAccountError;
import com.streamwide.smartms.lib.core.api.account.STWAccountManager;
import com.streamwide.smartms.lib.core.api.account.login.LogoutCallBack;
import com.streamwide.smartms.lib.core.api.account.settings.STWAccountChanged;
import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettings;
import com.streamwide.smartms.lib.core.api.account.settings.STWAccountSettingsError;
import com.streamwide.smartms.lib.core.api.account.settings.STWCompanyInformationChanged;
import com.streamwide.smartms.lib.core.api.account.settings.STWOperationStatusListener;
import com.streamwide.smartms.lib.core.api.account.settings.STWRoleIconListener;
import com.streamwide.smartms.lib.core.api.account.settings.STWUserAvailability;
import com.streamwide.smartms.lib.core.api.account.settings.STWUserCustomStatusCallback;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.DefaultContactIconResources;
import com.streamwide.smartms.lib.core.data.item.OperationalStatusItem;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.core.data.item.RoleIconItem;
import com.stw.protorype.MainApplication;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.login.LoginActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;

public class AccountActivity extends AppCompatActivity {

    private String CLASS_NAME = "AccountActivity";
    private String TAG = "Account";

    private static final int PICK_IMAGE_REQUEST_CODE = 10;

    private ImageView mCompanyLogoImageView;
    private TextView mPhoneNumberText;
    private TextView mUserNameText;
    private TextView mUserStatusText;

    private ImageView mProfilePicture;
    private ProgressBar mProfilePictureProgress;
    private Button mUserAvailabilityButton;

    private LinearLayout mUserOperationalStatusContainer;
    private TextView mUserOperationalStatusText;
    private Button mUserOperationalStatusButton;

    private LinearLayout mContactRoleIconContainer;
    private TextView mContactRoleIconName;
    private AppCompatImageView mContactRoleIconPicture;

    private ProgressBar mProgressBar;

    private int mUserOperationalStatusId = 0;

    private ContactItem mUserContactItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mCompanyLogoImageView = findViewById(R.id.company_logo);

        //User phone number
        mPhoneNumberText = findViewById(R.id.activity_account_phone_number_text);

        //User name
        mUserNameText = findViewById(R.id.activity_account_user_name_text);

        //User status
        mUserStatusText = findViewById(R.id.activity_account_status_text);
        mUserStatusText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                ContextCompat.getDrawable(this, R.drawable.ic_edit_text), null);
        mUserStatusText.setOnClickListener(mUserStatusClickListener);

        //User Operational status
        mUserOperationalStatusContainer = findViewById(R.id.activity_account_oper_status_container);
        mUserOperationalStatusText = findViewById(R.id.activity_account_oper_status_text);
        mUserOperationalStatusButton = findViewById(R.id.user_operational_status_btn);
        mUserOperationalStatusButton.setOnClickListener(mUserOperationalStatusClickListener);

        //User icon per role
        mContactRoleIconContainer = findViewById(R.id.activity_account_role_icon_container);
        mContactRoleIconName = findViewById(R.id.activity_account_role_icon_name);
        mContactRoleIconPicture = findViewById(R.id.activity_account_role_icon_picture);

        //User profile picture
        mProfilePicture = findViewById(R.id.user_picture);
        mProfilePictureProgress = findViewById(R.id.user_picture_progress);
        mProfilePicture.setOnClickListener(mProfilePictureClickListener);

        //User availability
        mUserAvailabilityButton = findViewById(R.id.user_availability);
        mUserAvailabilityButton.setOnClickListener(mUserAvailabilityClickListener);

        //Logout button
        Button mLogoutButton = findViewById(R.id.logout_button);
        mLogoutButton.setOnClickListener(mOnLogoutClickListener);

        //Loading info progress bar
        mProgressBar = findViewById(R.id.progress);

        //initialize account data
        initAccountData();

        //Register account listeners
        STWAccountSettings.getInstance().registerCompanyInformationChanged(mCompanyInformationChangedListener);
        STWAccountSettings.getInstance().registerAccountChanged(mAccountChangedListener);
        STWAccountSettings.getInstance().registerToUserCustomStatus(mUserCustomStatusCallback);
        STWAccountSettings.getInstance().registerToOperationalStatusEvents(mSTWOperationStatusListener);
        STWAccountSettings.getInstance().registerToRoleIconEvents(mSTWRoleIconListener);

    }

    private View.OnClickListener mUserStatusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            handleEditUserStatus();
        }
    };

    private View.OnClickListener mUserOperationalStatusClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            handleEditUserOperationalStatus();
        }
    };

    private View.OnClickListener mProfilePictureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean isProfileModificationAllowed = STWAccountSettings.getInstance().isProfileModificationsFromClientAllowed(AccountActivity.this);
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "initEvent"), TAG,
                    " isProfileModificationAllowed Value " + isProfileModificationAllowed);
            if (!isProfileModificationAllowed) {
                Toast.makeText(AccountActivity.this, "Option is not available.  Contact your Company Administrator to activate it.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, PICK_IMAGE_REQUEST_CODE);
        }
    };

    private View.OnClickListener mUserAvailabilityClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int userAvailability = (int) STWAccountSettings.getInstance().getUserAvailability(AccountActivity.this);
            showUserAvailabilityDialog(userAvailability);
        }
    };


    View.OnClickListener mOnLogoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            STWAccountManager.getInstance().logout(AccountActivity.this, true, new LogoutCallBack() {
                @Override
                public void onError(@NonNull @NotNull STWAccountError stwAccountError) {

                }

                @Override
                public void onStart() {
                    displayMessage("Start logout");
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onFinish() {
                    mProgressBar.setVisibility(View.GONE);
                    displayMessage("Finish logout");
                    Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    // you must reeinit your configuration
                    MainApplication.getInstance().reeinitConfiguration();
                }


            });
        }
    };

    private STWCompanyInformationChanged mCompanyInformationChangedListener = new STWCompanyInformationChanged() {

        @Override
        public void onLogoChanged() {
            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onLogoChanged"), TAG,
                    "company logo changed ");
            loadCompanyLogo();
        }

    };


    private STWAccountChanged mAccountChangedListener = new STWAccountChanged() {

        @Override
        public void onProfilePictureChanged() {
            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onLogoChanged"), TAG,
                    "company logo changed ");

            displayPicture();

        }

        @Override
        public void onProfileInformationChanged() {

            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onProfileInformationChanged"), TAG,
                    " profile information changed ");

            initAccountData();
        }

        @Override
        public void onProfileSettingsUpdated() {
            //use this callback to update profile settings
        }

    };

    private STWUserCustomStatusCallback mUserCustomStatusCallback = new STWUserCustomStatusCallback() {
        @Override
        public void onUserCustomStatusChanged(String userCustomStatus) {
            STWLoggerHelper.LOGGER.i(Pair.create(CLASS_NAME, "onUserCustomStatusChanged"),
                    TAG, "custom status changed");
            //display user status
            mUserStatusText.setText(userCustomStatus);
        }
    };

    private STWOperationStatusListener mSTWOperationStatusListener = new STWOperationStatusListener(){

        @Override
        public void onOperationalStatusFeatureChanged(boolean isAllowed) {

            initOperationalStatus();
        }

        @Override
        public void onOperationalStatusListChanged() {
            initOperationalStatus();
        }

        @Override
        public void onOperationalStatusUpdated(int operationalStatusId) {
            initOperationalStatus();
        }
    };

    private STWRoleIconListener mSTWRoleIconListener = new STWRoleIconListener(){
        @Override
        public void onRoleIconFeatureChanged(boolean isAllowed) {
            initUserRoleIcon();
        }
    };

    public void initAccountData() {

        //display phone number
        PhoneItem userPhone = STWAccountSettings.getInstance().getUserPhone(this);
        if(userPhone == null){
            return;
        }
        String phone = userPhone.getDisplayNumber();
        mPhoneNumberText.setText(phone);

        //get user contact to retrieve user info
        mUserContactItem =
                STWContactManager.getInstance().getSingleContactByPhoneItem(this, userPhone);

        if (mUserContactItem == null) {
            return;
        }

        //display user name
        String name = STWContactManager.getInstance().getDisplayNameForContactItem(this, mUserContactItem);
        mUserNameText.setText(name);

        //display user status
        mUserStatusText.setText(mUserContactItem.getStatus());

        //display user operational status
        initOperationalStatus();

        //display user role icon
        initUserRoleIcon();

        //display user profile picture
        displayPicture();


        // display user availability
        initUserAvailability();

        //load company logo
        loadCompanyLogo();
    }

    private void initUserAvailability() {
        long userAvailability = STWAccountSettings.getInstance().getUserAvailability(this);
        int availability = (int) userAvailability;
        switch (availability) {
            case STWUserAvailability.AVAILABLE: {
                mUserAvailabilityButton.setText("Availability : On Duty");
                mUserAvailabilityButton.setTextColor(Color.GREEN);
                break;
            }
            case 1: {
                mUserAvailabilityButton.setText("Availability : Off Duty");
                mUserAvailabilityButton.setTextColor(Color.RED);
                break;
            }
            default:
                mUserAvailabilityButton.setText("Availability");
                mUserAvailabilityButton.setTextColor(Color.BLACK);
                break;
        }
    }

    private void initOperationalStatus(){

        boolean isOperationalStatusAllowed = STWAccountSettings.getInstance().isOperationalStatusAllowed(this);

        if(isOperationalStatusAllowed){
            mUserOperationalStatusContainer.setVisibility(View.VISIBLE);
            mUserOperationalStatusButton.setVisibility(View.VISIBLE);
        }else{
            mUserOperationalStatusContainer.setVisibility(View.GONE);
            mUserOperationalStatusButton.setVisibility(View.GONE);

            return;
        }


        if (mUserContactItem == null) {
            return;
        }

        mUserOperationalStatusId = mUserContactItem.getOperationalStatus();

        if(mUserOperationalStatusId > 0){

            OperationalStatusItem operationalStatusItem =
                    STWContactManager.getInstance().getOperationalStatusByIdentifier(this, mUserOperationalStatusId);

            if(operationalStatusItem == null){
                mUserOperationalStatusText.setText(getString(R.string.operational_status_off));
            }else{

                String operationalStatusToShow = operationalStatusItem.getCode()+" - "+ operationalStatusItem.getLabel();

                mUserOperationalStatusText.setText(operationalStatusToShow);
                mUserOperationalStatusText.setVisibility(View.VISIBLE);
            }
        }else{
            mUserOperationalStatusText.setText(getString(R.string.operational_status_off));
        }
    }

    private void initUserRoleIcon() {

        //user phone number
        PhoneItem userPhone = STWAccountSettings.getInstance().getUserPhone(this);

        //Check if Icon per role feature is allowed by the administrator or not
        boolean isRoleIconFeatureAllowed = STWContactManager.getInstance().isIconPerRoleFeatureAllowed(this);

        if(isRoleIconFeatureAllowed && userPhone != null){

            RoleIconItem roleIconItem = STWContactManager.getInstance().getRoleIconByPhone(this, userPhone);

            if(roleIconItem == null){
                mContactRoleIconContainer.setVisibility(View.GONE);
            }else{

                mContactRoleIconName.setText(roleIconItem.getName());

                new Thread(() -> {
                    Bitmap roleIconPicture = STWContactManager.getInstance().getRoleIconPicture(userPhone.getRoleIcon());

                    mContactRoleIconPicture.setImageBitmap(roleIconPicture);
                }).start();

                mContactRoleIconContainer.setVisibility(View.VISIBLE);
            }
        }else{

            mContactRoleIconContainer.setVisibility(View.GONE);
        }
    }

    private STWOperationCallback<STWAccountSettingsError> mDeleteProfilePictureCallback = new STWOperationCallback<STWAccountSettingsError>() {
        @Override
        public void onError(STWAccountSettingsError stwAccountSettingsError) {
            STWLoggerHelper.LOGGER.e(Pair.create(CLASS_NAME, "onError"), TAG,
                    "Delete Profile Picture error : " + stwAccountSettingsError.getMessage());
            Toast.makeText(AccountActivity.this, "Error occurred while deleting Profile Picture",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess() {
            STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "onSuccess"), TAG,
                    "Delete Profile Picture  success ");


            displayPicture();
        }
    };

    private void displayPicture() {

        DefaultContactIconResources defaultContactIconResources = new DefaultContactIconResources
                .Builder()
                .placeHolderResourceId(R.drawable.ic_profile_picture)
                .build();
        STWAccountSettings.getInstance().loadUserPicture(this,mProfilePicture ,defaultContactIconResources);
    }


    public void handleEditUserStatus() {

        boolean userModificationsAllowedFromClient = STWAccountSettings.getInstance().isProfileModificationsFromClientAllowed(this);

        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "showStatusDialog"), TAG,
                " userModificationsAllowedFromClient value : " + userModificationsAllowedFromClient);

        String content = mUserStatusText.getText().toString();

        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("STATUS");

        final EditText input = new EditText(this);
        if(!TextUtils.isEmpty(content)){
            input.setText(content);
        }
        alertDialog.setView(input);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        updateUserStatus(input.getText().toString());
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                null);

        // closed

        // Showing Alert Message
        alertDialog.show();
    }

    public void handleEditUserOperationalStatus() {

        // get all operational status items
        List<OperationalStatusItem> operationalStatusItemList = STWContactManager.getInstance().getOperationalStatusList(this);

        List<String> operationalStatusList = new ArrayList<>();

        int selectedItem = -1;
        for(int i = 0 ; i< operationalStatusItemList.size() ; i++){

            OperationalStatusItem operationalStatusItem = operationalStatusItemList.get(i);
            operationalStatusList.add(operationalStatusItem.getCode()+" - "+ operationalStatusItem.getLabel());

            if(operationalStatusItem.getServerId() == mUserOperationalStatusId ){
                selectedItem = i;
            }
        }
        final CharSequence[] items = operationalStatusList.toArray(new CharSequence[operationalStatusList.size()]);

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, selectedItem, null)
                .setPositiveButton(R.string.select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                        OperationalStatusItem selectedItem = operationalStatusItemList.get(selectedPosition);

                        if(selectedItem != null) {
                            STWAccountSettings.getInstance().updateUserOperationalStatus(AccountActivity.this, selectedItem.getId(), null);
                        }
                    }
                })
                .setNeutralButton(R.string.disable_operational_status, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();

                            STWAccountSettings.getInstance().updateUserOperationalStatus(AccountActivity.this, 0, null);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void updateUserStatus(String newStatus) {
        STWAccountSettings.getInstance().updateUserCustomStatus(AccountActivity.this, newStatus,
                new STWOperationCallback<STWAccountSettingsError>() {

                    @Override
                    public void onError(STWAccountSettingsError error) {
                        STWLoggerHelper.LOGGER.d(
                                Pair.create(CLASS_NAME, "updateUserStatus"),
                                TAG,
                                "An error occurred on sending the new user custom status");
                    }

                    @Override
                    public void onSuccess() {

                        STWLoggerHelper.LOGGER.d(
                                Pair.create(CLASS_NAME, "updateUserStatus"),
                                TAG,
                                "The new user custom status sent successfully");
                        mUserStatusText.setText(newStatus);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PICK_IMAGE_REQUEST_CODE == requestCode && RESULT_OK == resultCode) {
            String dataString = data.getDataString();
            if (!TextUtils.isEmpty(dataString)) {
                Uri selectedImageUri = Uri.parse(dataString);
                File imageFile = getImageFile(selectedImageUri);
                uploadPicture(imageFile);
            }
        }
    }

    public File getImageFile(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        return new File(path);
    }


    private void uploadPicture(File file) {
        mProfilePictureProgress.setVisibility(View.VISIBLE);
        STWAccountSettings.getInstance()
                .uploadUserPicture(this, file, new STWOperationCallback<STWAccountSettingsError>() {

                    @Override
                    public void onError(STWAccountSettingsError stwAccountSettingsError) {
                        STWLoggerHelper.LOGGER.e(Pair.create(CLASS_NAME, "uploadPicture"),
                                TAG, "upload Picture error");
                        Toast.makeText(AccountActivity.this, "upload Picture error ",
                                Toast.LENGTH_LONG).show();
                        mProfilePictureProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onSuccess() {
                        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "uploadPicture"),
                                TAG, "upload Picture success");
                        Toast.makeText(AccountActivity.this, "upload Picture success ",
                                Toast.LENGTH_LONG).show();
                        displayPicture();
                        mProfilePictureProgress.setVisibility(View.GONE);
                    }
                });
    }


    private void showUserAvailabilityDialog(int availability) {
        final String[] availabilityChoicesList =
                getResources().getStringArray(R.array.setting_availability_options);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("User Availability");
        mBuilder.setSingleChoiceItems(availabilityChoicesList, availability, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int newUserAvailability;
                if (i == 0) {
                    newUserAvailability = STWUserAvailability.AVAILABLE;
                } else {
                    newUserAvailability = STWUserAvailability.BUSY;
                }
                updateUserAvailability(newUserAvailability);
                dialogInterface.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void updateUserAvailability(int newUserAvailability) {
        STWAccountSettings.getInstance().updateUserAvailability(AccountActivity.this, (int) newUserAvailability,
                new STWOperationCallback<STWAccountSettingsError>() {

                    @Override
                    public void onError(STWAccountSettingsError error) {
                        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME,
                                "updateUserAvailability"),
                                TAG,
                                " onError: " + error.getMessage());
                    }

                    @Override
                    public void onSuccess() {
                        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME,
                                "updateUserAvailability"),
                                TAG,
                                " onSuccess changing USER AVAILABILITY ");
                        initUserAvailability();
                    }
                });
    }


    private void loadCompanyLogo() {

        STWAccountSettings.getInstance().loadCompanyLogo(mCompanyLogoImageView, new STWOperationCallback<STWAccountSettingsError>() {
            @Override
            public void onSuccess() {
                STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME,
                        "loadCompanyLogo"),
                        TAG,
                        " onSuccess");
            }

            @Override
            public void onError(STWAccountSettingsError error) {
                STWLoggerHelper.LOGGER.e(Pair.create(CLASS_NAME,
                        "loadCompanyLogo"),
                        TAG,
                        " onError: " + error.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayMessage(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        //unregister account listeners
        STWAccountSettings.getInstance().unregisterCompanyInformationChanged(mCompanyInformationChangedListener);
        STWAccountSettings.getInstance().unregisterAccountChanged(mAccountChangedListener);
        STWAccountSettings.getInstance().unregisterToUserCustomStatus(mUserCustomStatusCallback);
        STWAccountSettings.getInstance().unregisterFromOperationalStatusEvents(mSTWOperationStatusListener);
        STWAccountSettings.getInstance().unregisterFromRoleIconEvents(mSTWRoleIconListener);
        super.onDestroy();
    }
}
