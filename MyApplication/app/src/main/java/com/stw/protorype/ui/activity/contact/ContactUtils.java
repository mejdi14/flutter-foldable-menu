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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.streamwide.smartms.lib.core.api.contact.STWContactManager;
import com.streamwide.smartms.lib.core.data.item.ContactItem;
import com.streamwide.smartms.lib.core.data.item.DefaultContactIconResources;
import com.streamwide.smartms.lib.core.data.item.PhoneItem;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    private static final String TAG = "ContactUtils";

    public static DefaultContactIconResources getDefaultIconResources()
    {
        int backgroundColor = Color.parseColor("#f1f1f1");
        int textColor =Color.parseColor("#112250");
        return new DefaultContactIconResources.Builder()
                .textColor(textColor)
                .backgroundColor(backgroundColor)
                .build();
    }

    public static boolean isEmpty(CharSequence value)
    {
        return (value == null || TextUtils.isEmpty(value.toString().trim()));
    }

    public static List<ContactItem> constructContactListFromPhoneList(Context mContext, List<PhoneItem> phones)
    {
        if (phones == null) {
            return null;
        }
        List<ContactItem> contacts = new ArrayList<>();

        for (PhoneItem phoneItem : phones) {
            if (phoneItem != null) {
                ContactItem contactItem =
                        STWContactManager.getInstance().getSingleContactByPhoneItem(mContext, phoneItem);
                if (contactItem != null) {
                    contacts.add(contactItem);
                }
            }

        }
        return contacts;
    }

    /**
     * provides all phones in given group
     *
     * @param groupContact
     * @return List<PhoneItem>
     */

    public static List<PhoneItem> getPhoneNumbersInGroup(Context context, ContactItem groupContact)

    {
        ArrayList<PhoneItem> relatedPhones = null;
        if (groupContact == null || !groupContact.isGroup()) {
            Log.i(TAG,"getPhoneNumbersInGroup : groupContact is null or not group");
            return null;
        }

        if (isEmpty(groupContact.getGroupPhoneNumbers())) {
            Log.i(TAG,"getPhoneNumbersInGroup : phoneNumbers is  null");
            return null;
        }

        if (isEmpty(groupContact.getGroupPhoneNumbers()) == false) {
            String[] phoneNumbers = groupContact.getGroupPhoneNumbers().split(",");
            if (phoneNumbers != null && phoneNumbers.length > 0) {
                for (int i = 0; i < phoneNumbers.length; i++) {
                    PhoneItem phoneItem = STWContactManager.getInstance().getPhoneByNumber(context, phoneNumbers[i]);
                    if (phoneItem != null) {
                        if (relatedPhones == null) {
                            relatedPhones = new ArrayList<>();
                        }
                        relatedPhones.add(phoneItem);
                    }
                }
            }
        }

        return relatedPhones;
    }

    /**
     * Added for group of groups feature
     * construct GroupItem list from group ids
     *
     * @param context
     * @param groupContact
     * @return List<ContactAdapter>
     */
    public static List<ContactItem> getGroupsByGroupId(Context context, ContactItem groupContact)
    {
        Log.i(TAG,"group Name = " + groupContact.getGroupName());
        ArrayList<ContactItem> realtedGroups = null;
        String contactIds = groupContact.getGroupIds();
        Log.i(TAG,"getGroupsIds = " + contactIds);
        if (contactIds == null) {
            Log.i(TAG,"getGroupsInGroup : contactIds is null");
            return null;
        }

        if (isEmpty(contactIds)) {
            Log.i(TAG,"getmGroupOfGroupsIds : groupIds is  null");
            return null;
        }

        if (isEmpty(contactIds) == false && groupContact.isGroup()) {
            Log.i(TAG,"contactIds = " + contactIds);
            String[] groupsIds = contactIds.split( " ");
            if (groupsIds != null && groupsIds.length > 0) {
                for (int i = 0; i < groupsIds.length; i++) {
                    ContactItem groupItem = STWContactManager.getInstance().getContactByGroupId(context, groupsIds[i]);
                    if (groupItem != null) {
                        if (realtedGroups == null) {
                            realtedGroups = new ArrayList<>();
                        }
                        realtedGroups.add(groupItem);
                    }
                }
            }
        }

        return realtedGroups;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkPermissions(Context context, String permission)
    {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            int permissionState = context.checkSelfPermission(permission);

            // Check if the permission is already available.
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                // permission has not been granted.
                Log.i(TAG,permission + " permission has not been granted.");
                return false;
            } else {
                // permissions are available.
                Log.i(TAG,permission + " permission has already been granted.");

                return true;
            }
        } else {
            return true;
        }
    }
}
