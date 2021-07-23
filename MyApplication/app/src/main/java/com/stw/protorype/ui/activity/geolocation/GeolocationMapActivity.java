/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 8 Jan 2020 16:13:19 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 8 Jan 2020 16:13:19 +0100
 */

package com.stw.protorype.ui.activity.geolocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.api.geolocation.GeolocationError;
import com.streamwide.smartms.lib.core.api.geolocation.STWGeolocationManager;
import com.streamwide.smartms.lib.core.api.geolocation.STWRequestAddressListener;
import com.streamwide.smartms.lib.core.api.geolocation.STWSearchPlaceListener;
import com.streamwide.smartms.lib.core.api.geolocation.STWTrackingParticipantsListener;
import com.streamwide.smartms.lib.core.api.geolocation.StartTrackingParticipantsCallback;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.LocationAttachment;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;
import com.streamwide.smartms.lib.template.location.PositionItem;
import com.streamwide.smartms.lib.template.map.ISearchLocation;
import com.stw.protorype.R;
import com.stw.protorype.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.stw.protorype.ui.activity.geolocation.GeolocationActivity.EXTRA_GET_PLACE_INFORMATION;
import static com.stw.protorype.ui.activity.geolocation.GeolocationActivity.EXTRA_USERS_PHONES;

public class GeolocationMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = GeolocationMapActivity.class.getSimpleName();

    private GoogleMap mMap;
    private AppCompatAutoCompleteTextView mAutoCompleteTextView;
    private AppCompatImageView mPinIndicator;
    ArrayList<Marker> mMarkers = new ArrayList<>();

    private List<LocationAttachment> mLocations = new ArrayList<>();
    PlacesAutoCompleteAdapter mAdapter;

    private String mUsersPhone;
    private boolean mCanGetAddressByPosition = false;
    private boolean mIsTrackingParticipantsEventsRegistered;


    private STWTrackingParticipantsListener mTrackingParticipantsListener =  new STWTrackingParticipantsListener() {
        @Override
        public void onInitialParticipantsReceived(ArrayList<PhoneItem> participants) {
            Log.d(TAG, " onInitialParticipantsReceived ");
            clearMap();
            for (PhoneItem participant : participants) {
                Log.d(TAG, " data participants name = "+participant.getDisplayNumber());
                addMarker(participant);
            }

            animateMap();
        }

        @Override
        public void onParticipantsUpdatesReceived(ArrayList<PhoneItem> participants) {

            Log.d(TAG, " onParticipantsUpdatesReceived ");

        }

        @Override
        public void onStopTrackingParticipants() {
            Log.d(TAG, " onStopTracking ");
        }

        @Override
        public void onFailToRequestPosition(int error) {
            Log.d(TAG, "tracking request failed with error = "+error);

            handleTrackingLocationErrors(error);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation_map);
        mAutoCompleteTextView = findViewById(R.id.autocomplete_edit_text);
        mPinIndicator = findViewById(R.id.pin_indicator);
        SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

        if (mSupportMapFragment != null){
            mSupportMapFragment.getMapAsync(GeolocationMapActivity.this);
        }

        initResolver();
        initEvents();
        initData();
    }

    private void initEvents() {
        mAutoCompleteTextView.addTextChangedListener(mTextWatcher);
        mAutoCompleteTextView.setOnItemClickListener(onItemClickListener);

        /**
         * Register for tracking users location listener
         */
        registerForTrackingParticipantsEvents();
    }

    private void initData() {

        if (mUsersPhone.isEmpty()) {
            mAutoCompleteTextView.setVisibility(View.VISIBLE);
            if (mCanGetAddressByPosition) {
                mPinIndicator.setVisibility(View.VISIBLE);
            } else {
                mPinIndicator.setVisibility(View.GONE);
                mAdapter = new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line, mLocations);
                mAutoCompleteTextView.setAdapter(mAdapter);
                mAutoCompleteTextView.setThreshold(3);
            }
        }
    }

    private void initResolver() {
        Intent intent = getIntent();
        mUsersPhone = getUsersPhones(intent);
        mCanGetAddressByPosition = canGetAddressByPosition(intent);
    }

    /**
     * create a request to get updates on user's location
     */
    private void startTrackingUsers() {

        /**
         * you can track users by entering their phone numbers or a group of
         * users by entering the group id
         */
        String[] users = Utils.prepareParticipantArrayString(mUsersPhone);

        /**
         * Need to stop the old tracking server before instantiating and starting another
         */
        stopTrackingUsers();


        /**
         * Needed for the auto refresh time. The request will be triggered again automatically
         * after the time set in the refresh time value
         */
        int refreshTime = 60; // 1 minute

        STWGeolocationManager.getInstance().startTrackingParticipantsLocations(users, refreshTime, new StartTrackingParticipantsCallback() {
            @Override
            public void onStartTracking() {
                Log.d(TAG, "Tracking users started");
            }

            @Override
            public void onError(GeolocationError error) {
                handleTrackingLocationErrors(error.getCode());
            }
        });

    }

    private void addMarker(PhoneItem phoneItem) {
        if (phoneItem == null) {
            return;
        }
        ContactItem contactItem =
                STWContactManager.getInstance().getSingleContactByPhoneItem(GeolocationMapActivity.this, phoneItem);
        String contactName = "Not found";
        String contactEmail = "Not found";
        if (contactItem != null) {
            contactEmail = contactItem.getEmail();
            contactName = contactItem.getNormalizedName();
        }
        String snippet = "Address: " + phoneItem.getGeolocationAddress() + "\n" +
                "Phone Number: " + phoneItem.getDisplayNumber() + "\n" +
                "Email: " + contactEmail + "\n";

        addMarker(phoneItem.getGeolocationLatitude(), phoneItem.getGeolocationLongitude(), contactName, snippet);

    }

    private void addMarker(Double lat, Double lng, String title, String snippet){
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title(title)
                .snippet(snippet);

        mMarkers.add(mMap.addMarker(options));
    }

    private void animateMap() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 20; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (!mCanGetAddressByPosition) {
                searchForPlace(s.toString());
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            // do nothing...
        }

        @Override
        public void afterTextChanged(Editable s)
        {
            // do nothing...
        }
    };

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    LocationAttachment locationAttachment = mAdapter.getItem(i);
                    if (locationAttachment != null) {
                        mAutoCompleteTextView.setText(locationAttachment.getAddress());

                        handleSearchForPlaceDetail(locationAttachment.getAttachmentId(), new ISearchLocation() {
                            @Override
                            public void onError(Object result, boolean isLibraryError) {
                                Utils.showToast(GeolocationMapActivity.this, "Internet connection error");
                            }

                            @Override
                            public void onComplete(Object response) {
                                if (response != null) {

                                    clearMap();

                                    PositionItem positionItem = (PositionItem) response;
                                    if (positionItem != null) {
                                        addMarker(Double.parseDouble(positionItem.getLatitude()), Double.parseDouble(positionItem.getLongitude()),
                                                locationAttachment.getTitle(),locationAttachment.getAddress());
                                        animateMap();
                                    }
                                }
                            }
                        });
                    }

                }
            };

    /**
     * Kicks off the request to search for places by a given search string
     * @param query The search string
     */
    private void searchForPlace(final String query)
    {
        if (TextUtils.isEmpty(query)) {
            return;
        }

        STWGeolocationManager.getInstance().searchForPlaces(this, query, new STWSearchPlaceListener() {
            @Override
            public void onComplete(ArrayList<LocationAttachment> locations)
            {
                mLocations.clear();
                if (locations == null || locations.isEmpty()) {
                    Log.d(TAG, "No result for search places with this query");
                    return;
                }
                mLocations.addAll(locations);
                mAdapter.setValue(mLocations);
            }

            @Override
            public void onError(GeolocationError error)
            {
                if (error.getCode() == GeolocationError.ErrorCode.NO_CONNECTION_ERROR) {
                    Toast.makeText(getApplicationContext(), "Internet connection error",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Get the address by given position
     * @param latitude latitude point
     * @param longitude longitude point
     */
    private void getAddressByPosition(Double latitude, Double longitude) {

        STWGeolocationManager.getInstance().getAddressFromPosition(latitude, longitude, new STWRequestAddressListener() {
            @Override
            public void onComplete(String address) {
                mAutoCompleteTextView.setText(address);
            }

            @Override
            public void onError(GeolocationError error) {
                Log.e(TAG, "an error occurred when getting the address" );
            }
        });
    }

    private void handleSearchForPlaceDetail(String placeId, final ISearchLocation listener) {

        String url = GoogleMapProvider.getGoogleMapsPlaceDetailsURL(this, placeId);
        RequestQueue queue = Volley.newRequestQueue(GeolocationMapActivity.this);
        // Request a string response from the provided URL.
        StringRequest searchDetailRequest =
                new StringRequest(Request.Method.GET, url, response -> {
                    if (response == null || response.isEmpty()) {
                        if (listener != null) {
                            listener.onComplete(null);
                        }
                        return;
                    }

                    final PositionItem positionItem = GoogleMapProvider.parseGooglePlacesDetails(response);

                    if (positionItem == null) {
                        if (listener != null) {
                            listener.onComplete(null);
                        }
                        return;
                    }
                    listener.onComplete(positionItem);
                }, error -> {
                    if (listener != null) {
                        listener.onError(error, false);
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(searchDetailRequest);

    }

    /**
     * stop tracking participants service
     */
    private void stopTrackingUsers() {
        STWGeolocationManager.getInstance().stopTrackingParticipants();
    }

    private String getUsersPhones(Intent intent) {

        if (intent == null || intent.getExtras() == null || !intent.hasExtra(EXTRA_USERS_PHONES)) {
            return "";
        }
        return intent.getStringExtra(EXTRA_USERS_PHONES);
    }

    private boolean canGetAddressByPosition(Intent intent) {

        if (intent == null || intent.getExtras() == null || !intent.hasExtra(EXTRA_GET_PLACE_INFORMATION)) {
            return false;
        }
        return intent.getBooleanExtra(EXTRA_GET_PLACE_INFORMATION, false);
    }

    private void handleTrackingLocationErrors(int error) {
        switch (error) {
            case GeolocationError.ErrorCode.TRACKING_PARTICIPANT_NOT_ALLOWED:
                Utils.showToast(GeolocationMapActivity.this, "You are not allowed to track these users");
                break;
            case GeolocationError.ErrorCode.EMPTY_PARTICIPANT_TO_TRACK:
                Utils.showToast(GeolocationMapActivity.this, "There is no participants to track");
                break;
            default:
                Utils.showToast(GeolocationMapActivity.this, "unknown error");
        }
    }

    /** Called when the map is ready. */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        clearMap();

        /*
         * set a custom Info window to display users data
         */
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(GeolocationMapActivity.this));

        /*
         * Start tracking the realtime user's location
         */
        if (!mUsersPhone.isEmpty()) {
            startTrackingUsers();
        }

        mMap.setOnCameraIdleListener(() -> {
            // Get the center coordinate of the map, if the overlay view is center too
            CameraPosition cameraPosition = mMap.getCameraPosition();
            LatLng currentCenter = cameraPosition.target;

            if (mCanGetAddressByPosition) {
                /*
                 * start getting information on a specified place
                 */
                getAddressByPosition(currentCenter.latitude, currentCenter.longitude);
            }
        });

    }

    /**
     * clear map and markers list
     */
    private void clearMap() {
        mMap.clear();
        mMarkers.clear();
    }

    /**
     * register for tracking participants location events
     */
    private void registerForTrackingParticipantsEvents(){
        Log.d(TAG, "register for tracking participants events");
        if (mIsTrackingParticipantsEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterTrackingParticipantsService(mTrackingParticipantsListener);
        }

        STWGeolocationManager.getInstance().registerTrackingParticipantsService(mTrackingParticipantsListener);
        mIsTrackingParticipantsEventsRegistered = true;
    }

    /**
     * unregister tracking participants location events
     */
    private void unregisterForTrackingParticipantsEvents(){
        if (mIsTrackingParticipantsEventsRegistered) {
            STWGeolocationManager.getInstance().unregisterTrackingParticipantsService(mTrackingParticipantsListener);
            mIsTrackingParticipantsEventsRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTrackingUsers();

        /*
         * unregister from tracking participants location events
         */
        unregisterForTrackingParticipantsEvents();
    }
}
