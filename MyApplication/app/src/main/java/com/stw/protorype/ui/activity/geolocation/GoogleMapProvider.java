/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Wed, 8 Jan 2020 16:01:37 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 8 Jan 2020 10:37:31 +0100
 */

package com.stw.protorype.ui.activity.geolocation;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.streamwide.smartms.lib.template.location.PositionItem;
import com.streamwide.smartms.lib.template.map.ISearchLocation;
import com.streamwide.smartms.lib.template.map.MapProvider;
import com.stw.protorype.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GoogleMapProvider extends MapProvider {
    public static final String TAG = "GoogleMapProvider";

    private static final String STATIC_MAPS_LOCATION_SEARCH =
            "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String STATIC_MAPS_PLACE_DETAILS = "https://maps.googleapis.com/maps/api/place/details/json?";
    private static final String STATIC_MAPS_LOCATION_SEARCH_NEAR_BY =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String STATIC_MAPS_LOCATION_THUMBNAIL = "https://maps.googleapis.com/maps/api/staticmap?";

    private static final String PARAMS_INPUT = "input";
    private static final String PARAMS_KEYMAP = "key";
    private static final String PARAMS_PLACE_ID = "placeid";

    private static final String GOOGLE_STATUS = "status";
    private static final String GOOGLE_OK = "OK";
    private static final String GOOGLE_PREDICTIONS = "predictions";
    private static final String GOOGLE_DESCRIPTION = "description";
    private static final String GOOGLE_PLACE_ID = "place_id";
    private static final String GOOGLE_RESULT = "result";
    private static final String GOOGLE_GEOMETRY = "geometry";
    private static final String GOOGLE_LOCATION = "location";
    private static final String GOOGLE_LAT = "lat";
    private static final String GOOGLE_LNG = "lng";

    @Override
    public String getSearchPlacesURL(Context context, String search) {

        /**
         * get your key from google map console
         */
        String browserApiKey = context.getString(R.string.browser_api_key);

        if (TextUtils.isEmpty(browserApiKey)) {
            throw new Resources.NotFoundException("missing browser_api_key in gradle file!");
        }

        try {
            search = URLEncoder.encode(search, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            return null;
        }

        String buffer = (PARAMS_INPUT + "=" + search) + "&" + PARAMS_KEYMAP + "=" + browserApiKey;

        return STATIC_MAPS_LOCATION_SEARCH + buffer;

    }

    @Override
    public void searchForPlace(Context context, String query, ISearchLocation listener) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = getSearchPlacesURL(context, query);

        if (url == null || url.isEmpty()) {

            Log.d(TAG, "url is empty");
            if (listener != null) {
                listener.onError(null, false);
            }
            return;
        }

        // Request a string response from the provided URL.
        StringRequest searchRequest = new StringRequest(Request.Method.GET, url, response -> {

            List<PositionItem> positionItemList = null;

            if (response == null || response.isEmpty()) {
                Log.d(TAG, "response is empty");
            } else {
                Log.d(TAG, "parse places from response");
                positionItemList = handleParsePlaces(response);
            }

            if (listener != null) {
                listener.onComplete(positionItemList);
            }

        }, error -> {
            Log.e(TAG, "Error occured when search place!");
            if (listener != null) {
                listener.onError(error, true);
            }
        });

        // Add the request to the RequestQueue.
        queue.add(searchRequest);
    }

    @Override
    public String getLocationThumbnailUrl(Context context, String latitude, String longitude, String markerColor, boolean addMarker) {
        String browserApiKey = context.getString(R.string.browser_api_key);

        if (TextUtils.isEmpty(browserApiKey)) {
            throw new Resources.NotFoundException("missing browser_api_key in gradle file!");
        }

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(STATIC_MAPS_LOCATION_THUMBNAIL);
        stringBuffer.append("center=");
        stringBuffer.append(latitude);
        stringBuffer.append(",");
        stringBuffer.append(longitude);
        stringBuffer.append("&zoom=");
        stringBuffer.append("16");
        stringBuffer.append("&size=");
        stringBuffer.append(context.getResources().getDimensionPixelOffset(R.dimen.map_width));
        stringBuffer.append("x");
        stringBuffer.append(context.getResources().getDimensionPixelOffset(R.dimen.map_height));
        stringBuffer.append("&maptype=roadmap");

        if (addMarker) {
            stringBuffer.append("&markers=" + "color:");
            stringBuffer.append(markerColor);
        }

        stringBuffer.append("%7Clabel:S%7C");
        stringBuffer.append(latitude);
        stringBuffer.append(",");
        stringBuffer.append(longitude);
        stringBuffer.append("&sensor=true&format=jpg");
        stringBuffer.append("&key=");
        stringBuffer.append(browserApiKey);

        return stringBuffer.toString();
    }


    @Override
    public String handleParseGeoCoderCountryCode(String response) {
        return null;
    }

    @Override
    public List<PositionItem> handleParsePlaces(String jsonResult) {
        List<PositionItem> positionItemList = null;
        int maxResult = 4;

        try {

            JSONObject json = new JSONObject(jsonResult);

            String status = json.getString(GOOGLE_STATUS);
            if (status.equals(GOOGLE_OK)) {

                JSONArray array = json.getJSONArray(GOOGLE_PREDICTIONS);

                positionItemList = new ArrayList<>();

                for (int i = 0; i < maxResult && i < array.length(); i++) {
                    json = array.getJSONObject(i);

                    String address = json.getString(GOOGLE_DESCRIPTION);
                    String placeId = json.getString(GOOGLE_PLACE_ID);

                    PositionItem positionItem = new PositionItem();

                    positionItem.setAddress(address);
                    positionItem.setPlaceId(placeId);

                    positionItemList.add(positionItem);
                }
            }

        } catch (Exception ignored) {
        }

        return positionItemList;
    }

    @Override
    public void handleAddressFromLocation(Context context, double lat, double lng, ISearchLocation listener) {
        try {
            Geocoder geocoder = new Geocoder(context);

            List<Address> addressList =
                    geocoder.getFromLocation(lat, lng, 1);

            String address =  parseGoogleGeocoderAddressName(addressList);

            if (listener != null) {
                listener.onComplete(address);
            }

        } catch (IOException e) {
            if (listener != null) {
                listener.onError(null, false);
            }
        }
    }

    @Override
    public void handleDetailedAddressFromLocation(Context context, double lat, double lng, ISearchLocation listener) {

    }

    public static String getGoogleMapsPlaceDetailsURL(Context context, String placeId)
    {
        String browserApiKey = context.getString(R.string.browser_api_key);
        String buffer = (PARAMS_PLACE_ID + "=" + placeId) + "&" + PARAMS_KEYMAP + "=" + browserApiKey;

        return STATIC_MAPS_PLACE_DETAILS + buffer;
    }

    private String parseGoogleGeocoderAddressName(List<Address> addressList)
    {

        String addressName = null;

        if (addressList != null && !addressList.isEmpty()) {
            Address address = addressList.get(0);

            Set<String> listElementsAddress = new LinkedHashSet<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                listElementsAddress.add(address.getAddressLine(i));
            }

            /*
             * if locality is already exist with code postal
             * then not add the locality to address
             */
            String postalCode = address.getPostalCode();

            if (postalCode != null && !TextUtils.isEmpty(postalCode)
                    && address != null && address.getLocality() != null
                    && !TextUtils.isEmpty(address.getLocality())) {
                String postalCodeLocality1 = postalCode + " " + address.getLocality();
                String postalCodeLocality2 = address.getLocality() + " " + postalCode;

                postalCodeLocality1 = preparePlaceField(postalCodeLocality1, listElementsAddress);
                postalCodeLocality2 = preparePlaceField(postalCodeLocality2, listElementsAddress);

                if (!TextUtils.isEmpty(postalCodeLocality1)
                        && !TextUtils.isEmpty(postalCodeLocality2)) {

                    String locality = preparePlaceField(address.getLocality(), listElementsAddress);

                    if (!TextUtils.isEmpty(locality)) {
                        listElementsAddress.add(locality);
                    }
                }
            } else {
                String locality = preparePlaceField(address.getLocality(), listElementsAddress);

                if (!TextUtils.isEmpty(locality)) {
                    listElementsAddress.add(locality);
                }
            }

            /*
             * if countryName is already exist in listElementsAddress
             * then not add the countryName to address
             */

            String countryName = preparePlaceField(address.getCountryName(), listElementsAddress);

            if (!TextUtils.isEmpty(countryName)) {
                listElementsAddress.add(countryName);
            }

            addressName =
                    TextUtils.join("," + " ", listElementsAddress);
        }

        return addressName;
    }

    static String preparePlaceField(String field, Set<String> listElementsAddress)
    {
        if (field != null && !TextUtils.isEmpty(field) ) {

            for (String elementAddress : listElementsAddress) {
                if (!TextUtils.isEmpty(elementAddress)) {

                    String[] addressItems = elementAddress.split(",");

                    for (String addressItem : addressItems) {
                        if (!TextUtils.isEmpty(addressItem)) {

                            addressItem = addressItem.trim();

                            if (addressItem.equals(field)) {
                                return "";
                            }
                        }
                    }
                }
            }

        }

        return field;
    }

    /**
     *
     * @param jsonResult
     */
    public static PositionItem parseGooglePlacesDetails(String jsonResult)
    {
        try {
            JSONObject json = new JSONObject(jsonResult);

            String status = json.getString(GOOGLE_STATUS);

            if (status.equals(GOOGLE_OK)) {

                JSONObject resultJsonObject = json.getJSONObject(GOOGLE_RESULT);
                JSONObject jsonObject = resultJsonObject.getJSONObject(GOOGLE_GEOMETRY);
                JSONObject jsonGeo = jsonObject.getJSONObject(GOOGLE_LOCATION);

                final String latitude = jsonGeo.getString(GOOGLE_LAT);
                final String longitude = jsonGeo.getString(GOOGLE_LNG);

                PositionItem positionItem = new PositionItem();
                positionItem.setLatitude(latitude);
                positionItem.setLongitude(longitude);

                return positionItem;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error!", e);
        }

        return null;
    }
}
