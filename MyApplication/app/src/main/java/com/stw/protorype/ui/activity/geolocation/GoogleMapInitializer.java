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
 * @lastModifiedOn Mon, 16 Dec 2019 14:37:45 +0100
 */

package com.stw.protorype.ui.activity.geolocation;

import com.streamwide.smartms.lib.core.api.geolocation.STWGeolocationManager;
import com.streamwide.smartms.lib.core.api.geolocation.map.MapConfig;

public class GoogleMapInitializer {

    /**
     * initialize the map
     */
    public static void init() {

        MapConfig googleMapConfig = new MapConfig.Builder().mapProvider(new GoogleMapProvider()).build();
        STWGeolocationManager.getInstance().initMapConfig(googleMapConfig);

    }
}
