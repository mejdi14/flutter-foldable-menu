/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 8 Jan 2020 15:19:13 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 8 Jan 2020 15:19:13 +0100
 */

package com.stw.protorype.ui.activity.geolocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamwide.smartms.lib.core.api.geolocation.GeolocationError;
import com.streamwide.smartms.lib.core.api.geolocation.STWDispatcherStatusListener;
import com.streamwide.smartms.lib.core.api.geolocation.STWGeolocationFeaturesListener;
import com.streamwide.smartms.lib.core.api.geolocation.STWGeolocationManager;
import com.streamwide.smartms.lib.core.api.geolocation.STWGeolocationStatus;
import com.streamwide.smartms.lib.core.api.geolocation.STWTrackingDeviceLocationListener;
import com.streamwide.smartms.lib.core.api.geolocation.TrackingDeviceLocationCallback;
import com.streamwide.smartms.lib.core.data.item.GeolocTrackingRulesItem;
import com.streamwide.smartms.lib.core.data.item.ThreadItem;
import com.stw.protorype.BuildConfig;
import com.stw.protorype.R;
import com.stw.protorype.ui.activity.messaging.utils.ConversationUtils;
import com.stw.protorype.util.PermissionUtils;
import com.stw.protorype.util.Utils;
import java.util.List;

import static com.stw.protorype.MainConstant.GEOLOCATION;
import static com.stw.protorype.ui.activity.messaging.utils.ConversationUtils.EXTRA_RECIPIENT;

public class GeolocationActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = GeolocationActivity.class.getSimpleName();

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_CODE = 34;

    private boolean mIsTrackingDeviceEventsRegistered;
    private boolean mIsGeolocationFeaturesEventsRegistered;
    private boolean mIsDispatchManagerEventsRegistered;
    public   static String GROUP_PREFIX = "group:";
    static String EXTRA_USERS_PHONES = "users_phones";
    static String EXTRA_GET_PLACE_INFORMATION = "get_place_information";

    /**
     * Kicks off the request to start or stop the location tracking when pressed.
     */
    private Button mStartTrackingButton;

    /**
     * Kicks off the request to start tracking users.
     */
    private Button mStartTrackingUsersButton;

    /**
     * Kicks off the request to start searching for places by keyword.
     */
    private Button mSearchForPlacesButton;

    /**
     * Kicks off the request to get place's information by a given position.
     */
    private Button mGetPlaceInformationButton;
    private ImageView mAddUserButton;
    private TextView mTrackingStatusTextView;
    private EditText mPhoneNumberEditText;
    private TextView mGeolocErrorTextView;
    private TextView mTrackingErrorTextView;
    String[] mSelectedContact;

    /**
     * Register to this listener to delegate events related to Geolocation
     * changes performed by the company administrator
     */
    private STWGeolocationFeaturesListener mGeolocationFeaturesChangedListener = new STWGeolocationFeaturesListener() {
        @Override
        public void onGeolocationStatusChanged(@STWGeolocationStatus int geolocationStatus) {
            Log.d(TAG, "geolocation status changed");

            String geolocStatusDiscription = "OFF";
            if (geolocationStatus == STWGeolocationStatus.ON_REQUEST) {
                geolocStatusDiscription = "On Request";
            } else if (geolocationStatus == STWGeolocationStatus.ON_TRACKING) {
                geolocStatusDiscription = "On Tracking";
            }

            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator changed the geolocation status to "+geolocStatusDiscription, "OK" ,
                    view -> {});
            /**
             * update ui widgets
             */
            updateUIWidgets();
        }

        @Override
        public void canChangeGeolocationStatus(boolean canChangeGeolocationStatus) {
            Log.d(TAG, "Authorization to change the geolocation option ");

            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator changed your ability to modify the geolocation status", "OK",
                    view -> {});
        }

        @Override
        public void onTrackingParamsChanged(long trackingInterval, long accuracyValue) {
            Log.d(TAG, " tracking params changed");
            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator changed the tracking params", "OK",
                    view -> {});
        }

        @Override
        public void onGeolocationTrackingRulesChanged(List<GeolocTrackingRulesItem> geolocTrackingRulesItems) {
            Log.d(TAG, " geolocation tracking rules changed");
            Utils.showSnackbar(GeolocationActivity.this,"Your Company administrator changed the geolocation tracking rule", "OK",
                    view -> {});
        }

        @Override
        public void onGeolocationGpsOnlyChanged(boolean geolocationGPSOnlyValue) {
            Log.d(TAG, " GPS only changed");
            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator activated the GPS use only option to determine your location.", "OK",
                    view -> {});

        }

        @Override
        public void onMapProviderChanged(long mapProviderValue) {
            Log.d(TAG, "the map provider has changed");
            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator changed the Map provider", "OK",
                    view -> {});
        }

        @Override
        public void onWakeUpDeviceFeatureChanged(boolean isWakeUpDeviceFeatureAllowed) {
            Log.d(TAG, "wakeup device feature changed");
            Utils.showSnackbar(GeolocationActivity.this, "Your Company administrator changed The wakeup device feature", "OK",
                    view -> {});
        }

        @Override
        public void onDistanceBasedLocationFeatureChanged() {

        }
    };

    /**
     * By registering to this listener, the app will detect whether the user
     * become a dispatch manager or not
     */
    private STWDispatcherStatusListener mDispatcherStatusListener = isDispatcher -> {
        String str = "Your company administrator assign you as a dispatch manager: you will be able to track other users' locations";
        if (!isDispatcher) {
            str = "Dispatch manager changed : You will not be able to locate the company's users";
        }
        Log.d(TAG, "dispatch manager status changed");
        Utils.showSnackbar(GeolocationActivity.this, str, "OK",
                view -> {


                });

        updateTrackingUsersUIWidgets(isDispatcher);


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        mStartTrackingButton = findViewById(R.id.start_tracking_button);
        mStartTrackingUsersButton = findViewById(R.id.track_users_button);
        mSearchForPlacesButton = findViewById(R.id.search_for_places_button);
        mGetPlaceInformationButton = findViewById(R.id.get_place_info_button);
        mAddUserButton = findViewById(R.id.add_phone_number_button);
        mTrackingStatusTextView = findViewById(R.id.tracking_status_text_view);
        mPhoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        mGeolocErrorTextView = findViewById(R.id.geoloc_error_text_view);
        mTrackingErrorTextView = findViewById(R.id.tracking_error_text_view);

        mSelectedContact = getRecipients(getIntent());

        initEvents();

        initMap();

        updateUIWidgets();

        updateSelectedContactUI();
    }

    private void initEvents() {
        mStartTrackingButton.setOnClickListener(this);
        mStartTrackingUsersButton.setOnClickListener(this);
        mAddUserButton.setOnClickListener(this);
        mSearchForPlacesButton.setOnClickListener(this);
        mGetPlaceInformationButton.setOnClickListener(this);

        /*
         * Register to the listener that will be notified by the location tracking events. Like:
         * Start the location tracking.
         * Stop the location tracking.
         * Wake-up device event
         */
        registerForTrackingDeviceLocationEvents();

        /**
         * register to geolocation features changes
         */
        registerForGeolocationEvents();

        /**
         * register to Dispatch manager changes
         */
        registerForDispatchManagerEvents();

    }
    /**
     * initialize the map
     */
    private void initMap() {
        GoogleMapInitializer.init();
    }

    /**
     *  Change the tracking location button text.
     */
    private void updateUIWidgets() {
        boolean isGeolocationEnabled = isGeolocationEnabled(GeolocationActivity.this);
        long subscriberGeolocationStatus = getSubscriberGeolocationStatus(GeolocationActivity.this);
        boolean isDispatcher = isDispatcher(GeolocationActivity.this);

        if (subscriberGeolocationStatus != STWGeolocationStatus.OFF) {

            updateGeolocStatusUIWidget(true);
            mTrackingStatusTextView.setVisibility(View.VISIBLE);

            if (!isGeolocationEnabled) {
                mStartTrackingButton.setText("Start Tracking");
                mTrackingStatusTextView.setText("Tracking is stopped");
                mTrackingStatusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tracking_location_stopped, 0, 0, 0);
            } else {
                mStartTrackingButton.setText("Stop Tracking");
                mTrackingStatusTextView.setText("Tracking is started");
                mTrackingStatusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tracking_location_started, 0, 0, 0);
            }

        } else {
            updateGeolocStatusUIWidget(false);
            mTrackingStatusTextView.setVisibility(View.GONE);
        }


        updateTrackingUsersUIWidgets(isDispatcher);
    }

    private void updateSelectedContactUI() {
        if (mSelectedContact != null && mSelectedContact.length != 0) {
            mPhoneNumberEditText.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder();
            for(String s : mSelectedContact) {
                builder.append(s);
                builder.append(",");
            }
            String str = builder.toString();

            mPhoneNumberEditText.setText(str);

        } else {
            mPhoneNumberEditText.setVisibility(View.GONE);
        }
    }
    /**
     *  Enable UI widgets
     */
    private void updateTrackingUsersUIWidgets(boolean isDispatcher) {


        /**
         * Check UI related to tracking users' locations
         * If user not a dispatch manager disable the tracking button and show an error message
         */
        mStartTrackingUsersButton.setEnabled(isDispatcher);
        mAddUserButton.setEnabled(isDispatcher);

        if (isDispatcher) {
            mTrackingErrorTextView.setVisibility(View.GONE);
        } else{
            mTrackingErrorTextView.setVisibility(View.VISIBLE);
            mTrackingErrorTextView.setText("You have to be a dispatch manager to start tracking users");
        }
    }

    private void updateGeolocStatusUIWidget(boolean isGeolocFeatureEnable) {
        /**
         * Check UI related to geolocation status
         * If geolocation status is OFF: disable the start tracking button and display
         * an error message
         */
        mStartTrackingButton.setEnabled(isGeolocFeatureEnable);
        if (isGeolocFeatureEnable) {
            mGeolocErrorTextView.setVisibility(View.GONE);
        } else {
            mGeolocErrorTextView.setVisibility(View.VISIBLE);
            mGeolocErrorTextView.setText("Your company administrator changed the geolocation status to OFF, you cannot start the location tracking");
        }

    }

    /**
     * register for tracking device location events
     */
    protected void registerForTrackingDeviceLocationEvents() {
        if (mIsTrackingDeviceEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterTrackingLocationListener(mTrackingDeviceLocationListener);
        }

        STWGeolocationManager.getInstance().registerTrackingLocationListener(mTrackingDeviceLocationListener);

        mIsTrackingDeviceEventsRegistered = true;
    }

    /**
     * unregister tracking device location events
     */
    protected void unregisterFromTrackingDeviceLocationEvents() {
        if (mIsTrackingDeviceEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterTrackingLocationListener(mTrackingDeviceLocationListener);
            mIsTrackingDeviceEventsRegistered = false;
        }
    }

    /**
     * register for tracking device location events
     */
    protected void registerForGeolocationEvents() {
        if (mIsGeolocationFeaturesEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterGeolocationChangesListener(mGeolocationFeaturesChangedListener);
        }

        STWGeolocationManager.getInstance().registerGeolocationChangesListener(mGeolocationFeaturesChangedListener);

        mIsGeolocationFeaturesEventsRegistered = true;
    }

    /**
     * unregister tracking device location events
     */
    protected void unregisterForGeolocationEvents() {
        if (mIsGeolocationFeaturesEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterGeolocationChangesListener(mGeolocationFeaturesChangedListener);
            mIsGeolocationFeaturesEventsRegistered = false;
        }
    }

    /**
     * register for dispatch manager events
     */
    protected void registerForDispatchManagerEvents() {
        if (mIsDispatchManagerEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterDispatcherStatusListener(mDispatcherStatusListener);
        }

        STWGeolocationManager.getInstance().registerDispatcherStatusListener(mDispatcherStatusListener);

        mIsGeolocationFeaturesEventsRegistered = true;
    }

    /**
     * unregister from dispatch manager events
     */
    protected void unregisterForDispatchManagerEvents() {
        if (mIsDispatchManagerEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterDispatcherStatusListener(mDispatcherStatusListener);
            mIsDispatchManagerEventsRegistered = false;
        }
    }

    @Override
    public void onClick(View v) {

        boolean isGeolocationEnabled = isGeolocationEnabled(GeolocationActivity.this);
        long subscriberGeolocationStatus = getSubscriberGeolocationStatus(GeolocationActivity.this);

        /*
         * Start tracking device location
         */
        if (v.getId() == R.id.start_tracking_button) {

            if (!Utils.isConnectedToInternet(this)) {
                Utils.showToast(GeolocationActivity.this, "Internet connection error");
                return;
            }

            if (subscriberGeolocationStatus == STWGeolocationStatus.OFF) {
                Utils.showToast(GeolocationActivity.this, "Geolocation feature disabled");
                return;
            }

            if (!isGeolocationEnabled) {
                // start tracking device location

                /* location permission is required so check it first */
                if (PermissionUtils.checkPermissions(GeolocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    startDeviceLocationTracking(GeolocationActivity.this);
                } else {
                    PermissionUtils.requestPermission(GeolocationActivity.this, REQUEST_PERMISSIONS_CODE, Manifest.permission.ACCESS_FINE_LOCATION);
                }

            }else {
                // stop tracking device location
                stopLocationTracking(GeolocationActivity.this);
            }

            /*
             * Start tracking other device's location
             */
        } else if (v.getId() == R.id.track_users_button) {

            if (!Utils.isConnectedToInternet(this)) {
                Utils.showToast(GeolocationActivity.this, "Internet connection error");
                return;
            }

            if (subscriberGeolocationStatus == STWGeolocationStatus.OFF) {
                Utils.showToast(GeolocationActivity.this, "Geolocation feature is disabled");
                return;
            }


           String phoneNumbers = mPhoneNumberEditText.getText().toString();
            if (phoneNumbers == null || phoneNumbers.isEmpty()) {
                Utils.showToast(GeolocationActivity.this, "Please try to add at least a valid user phone number or a valid group id !");
                return;
            }
            openMapActivity(phoneNumbers, false);

        } else if (v.getId() == R.id.add_phone_number_button) {

            if (subscriberGeolocationStatus == STWGeolocationStatus.OFF) {
                Utils.showToast(GeolocationActivity.this, "Geolocation feature is disabled");
                return;
            }

            ConversationUtils.chooseContactsConversation(GeolocationActivity.this, null, ThreadItem.THREAD_TYPE_ONE_TO_MANY, GEOLOCATION);

            finish();
        } else if (v.getId() == R.id.search_for_places_button) {

            openMapActivity("", false );

        } else if (v.getId() == R.id.get_place_info_button) {

            openMapActivity("",  true );
        }
    }

    private void openMapActivity(String usersString, boolean getPlaceInformation) {
        Intent intent = new Intent(this, GeolocationMapActivity.class);
        intent.putExtra(EXTRA_USERS_PHONES, usersString);
        intent.putExtra(EXTRA_GET_PLACE_INFORMATION, getPlaceInformation);
        startActivity(intent);
    }

    /**
     *
     * @param intent
     * @return selected contacts from MessagingContactListActivity
     */
    private String[] getRecipients(Intent intent) {

        if (intent == null || intent.getExtras() == null || !intent.hasExtra(EXTRA_RECIPIENT)) {
            return null;
        }
        return intent.getStringArrayExtra(ConversationUtils.EXTRA_RECIPIENT);
    }
    /**
     * Check if location tracking is enabled
     *
     * @param context the application context
     * @return true if the tracking is already started false otherwise
     */
    private boolean isGeolocationEnabled(Context context) {
        return STWGeolocationManager.getInstance().isGeolocationEnabled(context);
    }

    /**
     * get The subscriber geolocation status:
     *
     * @param context the application context
     * @return long    0: off , 1: onRequest , 2: tracking
     */
    public long getSubscriberGeolocationStatus(Context context) {
        return STWGeolocationManager.getInstance().getGeolocationStatus(context);
    }

    /**
     * Check if the subscriber is a dispatch manager or not
     *
     * @param context the application context
     * @return boolean : true if the subscriber is a dispatch manager, false otherwise
     */
    public boolean isDispatcher(Context context) {
        return STWGeolocationManager.getInstance().isDispatcher(context);
    }

    /**
     * Start the device location tracking
     * @param context
     */
    private void startDeviceLocationTracking(Context context){

        STWGeolocationManager.getInstance().startTrackingDeviceLocation(context, new TrackingDeviceLocationCallback() {

            @Override
            public void onSuccess() {
                Utils.showToast(GeolocationActivity.this, "Tracking location started");
                updateUIWidgets();
            }

            @Override
            public void onError(GeolocationError error) {
                handleTrackingLocationErrors(error.getCode());
            }
        });
    }

    /**
     * Stop the location tracking
     * @param context
     */
    private void stopLocationTracking(Context context){
        STWGeolocationManager.getInstance().stopTrackingDeviceLocation(context, new TrackingDeviceLocationCallback() {
            @Override
            public void onSuccess() {
                Utils.showToast(GeolocationActivity.this, "Tracking location stopped");
                updateUIWidgets();
            }

            @Override
            public void onError(GeolocationError error) {
                handleTrackingLocationErrors(error.getCode());
            }
        });
    }

    private void handleTrackingLocationErrors(int error) {
        switch (error) {
            case GeolocationError.ErrorCode.LOCATION_PERMISSION_NOT_ALLOWED :
                Utils.showToast(GeolocationActivity.this, "Location permission not granted");
                break;
            case GeolocationError.ErrorCode.SYSTEM_SETTING_LOCATION_NOT_ALLOWED:
                Utils.showToast(GeolocationActivity.this, "System setting location not allowed");
                break;
            case GeolocationError.ErrorCode.REQUEST_FAILED:
                Utils.showToast(GeolocationActivity.this, "Sending request failed");
                break;
            case GeolocationError.ErrorCode.TRACKING_DEVICE_LOCATION_FEATURE_NOT_ALLOWED:
                Utils.showToast(GeolocationActivity.this, "Tracking location feature not allowed");
                break;
            case GeolocationError.ErrorCode.CHANGE_TRACKING_DEVICE_LOCATION_NOT_ALLOWED:
                Utils.showToast(GeolocationActivity.this, "You are not authorized to start the location tracking ");
                break;
            default:
                Utils.showToast(GeolocationActivity.this, "Unknown error");

        }
    }

    /**
     * Callback to keep tracked the status of location tracking
     */
    private STWTrackingDeviceLocationListener mTrackingDeviceLocationListener = new STWTrackingDeviceLocationListener() {


        @Override
        public void onStartTrackingDeviceLocation()
        {
            Log.d(TAG,"onStartTrackingLocation");
            mStartTrackingButton.setText("Stop Tracking");
            mTrackingStatusTextView.setText("Tracking is started");
            mTrackingStatusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tracking_location_started, 0, 0, 0);
        }

        @Override
        public void onStopTrackingDeviceLocation()
        {
            Log.d(TAG,"onStopTrackingLocation");
            mStartTrackingButton.setText("Start Tracking");
            mTrackingStatusTextView.setText("Tracking is stopped");
            mTrackingStatusTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tracking_location_stopped, 0, 0, 0);
        }

        @Override
        public void onGeolocWakeupDeviceNeeded()
        {
            /* If the wake-up device feature is enabled, this callback
             * will be triggered when the device goes into deep sleep mode.
             * Implement your logic here to wake-up the device
             */
            Log.d(TAG,"onGeolocWakeupDeviceNeeded : start an activity to wake-up the device");

        }


        @Override
        public void onError(GeolocationError error) {
            Log.d(TAG,"onError: cannot start location tracking "+error.getMessage());
        }
    };

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                startDeviceLocationTracking(GeolocationActivity.this);
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Utils.showSnackbar(this, "Permission required", "Settings",
                        view -> {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
         * unregister from tracking device location events
         */
        unregisterFromTrackingDeviceLocationEvents();

        /**
         * unregister from geolocation features changes
         */
        unregisterForGeolocationEvents();

        /**
         * unregister from dispatch manager changes
         */
        unregisterForDispatchManagerEvents();

    }
}
