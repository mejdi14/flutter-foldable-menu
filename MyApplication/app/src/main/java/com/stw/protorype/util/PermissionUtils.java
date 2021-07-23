/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Thu, 9 Jan 2020 10:49:48 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Wed, 8 Jan 2020 15:35:48 +0100
 */

package com.stw.protorype.util;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;


/**
 * Utility class for access to runtime permissions.
 */
public class PermissionUtils {

    private PermissionUtils() {
    }

    /**
     * Requests the permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public static void requestPermission(Activity activity, int requestId, String permission)
    {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

            Utils.showSnackbar(activity, "Permission required", "OK",
                    view -> {
                        // Request permission
                        ActivityCompat.requestPermissions(activity,
                                new String[]{permission},
                                requestId);
                    });

        } else {
            // permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestId);
        }
    }



    /**
     * Return the current state of the permissions needed.
     */
    public static boolean checkPermissions(Context context, String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(context, permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

}

