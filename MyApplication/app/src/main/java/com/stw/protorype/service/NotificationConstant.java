/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on Mon, 6 Jan 2020 09:52:46 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn Mon, 6 Jan 2020 09:52:46 +0100
 */

package com.stw.protorype.service;

import androidx.annotation.StringDef;

public class NotificationConstant {

    /**
     * setting key
     */
    public interface NotificationKey {

        /** Notification id **/
        int LONE_WORKER_SERVICE_NOTIFICATION_ID = 0x1;
        int GEOLOCATION_SERVICE_NOTIFICATION_ID = 0x2;
        int VOIP_SERVICE_NOTIFICATION_ID = 0x3;

    }

    @StringDef({ ChannelId.FOREGROUND_SERVICE_CHANNEL_ID,

    })

    public @interface ChannelId {

        String FOREGROUND_SERVICE_CHANNEL_ID = "FOREGROUND_SERVICE_CHANNEL_ID";

    }

    public interface NotificationServiceAction {

        String LONE_WORKER_NOTIFICATION_SERVICE = "START_LONE_WORKER_NOTIFICATION";
        String GEOLOCATION_NOTIFICATION_SERVICE = "START_GEOLOCATION_NOTIFICATION";
        String VOIP_NOTIFICATION_SERVICE = "VOIP_NOTIFICATION_SERVICE";

    }

    public interface NotificationChannelName {

        String NOTIFICATION_CHANNEL_NAME = "NOTIFICATION_CHANNEL_NAME";

    }
}
