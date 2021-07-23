/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Fri, 24 Jan 2020 12:11:00 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Fri, 24 Jan 2020 11:46:35 +0100
 */

package com.stw.protorype.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import static com.stw.protorype.ui.activity.geolocation.GeolocationActivity.GROUP_PREFIX;

public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    private Utils() {

    }
    /**
     * check if device is connected to Internet
     *
     * @param context
     * @return
     */
   public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        Log.i(TAG, " isConnectedToInternet "+isConnected);
        return isConnected;
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param activity         The activity context
     * @param mainTextString The string for the Snackbar text.
     * @param actionString   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    public  static void showSnackbar(Activity activity, String mainTextString, final String actionString,
                              View.OnClickListener listener) {
        Snackbar.make(activity.findViewById(android.R.id.content), mainTextString,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(actionString, listener).show();
    }

    /**
     * Shows a toast with the given text.
     */
    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param userPhoneNumber
     * @return array of string like : "[group:2,group:1,21699926249@smartms206.streamwide.com]"
     */
    public static String[] prepareParticipantArrayString(String userPhoneNumber) {

        if (userPhoneNumber == null) {
            /**
             * return an empty array instead of null object
             */
            return new String[0];
        }
        String[] destIndexArray = userPhoneNumber.split(",");
        String[] users = new String[destIndexArray.length];
        int n = 0;

            for (String phoneNumber : destIndexArray) {
                users[n] =  phoneNumber+",";
                n++;
            }

        return users;
    }
    /**
     * Validation of Phone Number
     */
    public static boolean isValidPhoneNumber(CharSequence target) {
        if (target == null || target.length() < 6 || target.length() > 13) {
            return false;
        }else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }

    }


    public static boolean isThereValidPhoneNumbersOrGroupIds(String phoneNumbers, String regex){
        String[] stringPhones = phoneNumbers.split(regex);

        for (String stringPhone : stringPhones) {
            Log.e("tag"," string phone ==== "+stringPhone);
            if (isValidPhoneNumber(stringPhone)) {
                return true;
            }else if (stringPhone.startsWith(GROUP_PREFIX)){
                return true;

            }
        }

        return false;
    }

    /**
     * hideSoftInput
     */
    public static void hideSoftInput(Activity context)
    {
        // check the input is active
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocusedView = context.getCurrentFocus();
        if (manager != null && manager.isActive() && currentFocusedView != null) {
            IBinder windowToken = currentFocusedView.getWindowToken();
            manager.hideSoftInputFromWindow(windowToken, 0);
        }
    }

}
