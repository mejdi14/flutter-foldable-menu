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

package com.stw.protorype;

import com.streamwide.smartms.lib.core.api.environment.logger.STWLoggerHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Pair;

public class MainPreference {

    private static final String CLASS_NAME = "MainPreference";
    private static final Boolean LOG_ENABLED = false;
    private static final String DOUBLE_QUOTE_CHAR = "\"";
    private static final String DOUBLE_QUOTE_CHAR_REPLACEMENT = "&#34;";
    /**
     * Singleton instance
     */
    private static MainPreference mInstance;
    /**
     * System preference class
     */
    private SharedPreferences mPreferences;

    /**
     * Lone Worker selector
     */
    public static final String LONE_WORKER_SETTING_ENABLED = "lone_worker_setting_enabled";

    /**
     * Constructor
     */
    private MainPreference(Context context)
    {
        String sharedPreferencesFileName = context.getPackageName() + "_main_preferences";
        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, CLASS_NAME), MainConstant.PREFERENCES,
                        "Loading shared preferences file");
        mPreferences = context.getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE);
    }

    /**
     * Singleton
     */
    public static synchronized MainPreference getInstance(Context context)
    {
        if (mInstance == null) {
            mInstance = new MainPreference(context);
        }
        return mInstance;
    }

    public boolean getBoolean(String key, boolean defValue) {
        boolean value = mPreferences.getBoolean(key, defValue);
        log("getBooleanPreference", key, String.valueOf(value));
        return value;
    }

    public float getFloat(String key, float defValue) {
        Float value = mPreferences.getFloat(key, defValue);
        log("getFloatPreference", key, String.valueOf(value));
        return value;
    }

    public int getInt(String key, int defValue) {
        int value = defValue;
        try {
            value = mPreferences.getInt(key, defValue);
        } catch (ClassCastException e) {
            value = Integer.parseInt(mPreferences.getString(key, String.valueOf(defValue)));
            putInt(key, value);
        }
        log("getIntPreference", key, String.valueOf(value));
        return value;
    }

    public long getLong(String key, long defValue) {
        Long value;
        try {
            value = mPreferences.getLong(key, defValue);
        } catch (ClassCastException e) {
            value = Long.parseLong(mPreferences.getString(key, String.valueOf(defValue)));
            putLong(key, value);
        }
        log("getLongPreference", key, String.valueOf(value));
        return value;
    }

    public String getString(String key, String defValue) {
        String value = mPreferences.getString(key, defValue).replace(DOUBLE_QUOTE_CHAR_REPLACEMENT, DOUBLE_QUOTE_CHAR);
        log("getStringPreference", key, value);
        return value;
    }

    /**
     * exception for Double put as a String
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value)
    {
        log("setStringPreference", key, "" + value);
        if (value instanceof Boolean) {
            putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            putString(key, (String) value);
        } else if (value instanceof Integer) {
            putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            putLong(key, (Long) value);
        } else if (value instanceof Float) {
            putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            putLong(key, ((Double) value).longValue());
        }
    }

    public void putBoolean(String key, boolean value) {
        log("setBooleanPreference", key, String.valueOf(value));
        Editor edit = mPreferences.edit();
        edit.putBoolean(key, value);
        commitSharedPrefs(edit);
    }

    public void putFloat(String key, float value) {
        log("setFloatPreference", key, String.valueOf(value));
        Editor edit = mPreferences.edit();
        edit.putFloat(key, value);
        commitSharedPrefs(edit);
    }

    public void putInt(String key, int value) {
        log("setIntPreference", key, String.valueOf(value));
        Editor edit = mPreferences.edit();
        edit.putInt(key, value);
        commitSharedPrefs(edit);
    }

    public void putLong(String key, long value) {
        log("setLongPreference", key, String.valueOf(value));
        Editor edit = mPreferences.edit();
        edit.putLong(key, value);
        commitSharedPrefs(edit);
    }

    public void putString(String key, String value) {
        log("setStringPreference", key, value);
        Editor edit = mPreferences.edit();
        edit.putString(key, value.replace(DOUBLE_QUOTE_CHAR, DOUBLE_QUOTE_CHAR_REPLACEMENT));
        commitSharedPrefs(edit);
    }

    /**
     * @param content
     * @param key
     * @param value
     */
    private void log(String content, String key, String value) {
        if (!LOG_ENABLED) {
            return;
        }

        if (null == key) {
            key = "";
        }
        if (null == value) {
            value = "";
        }

        STWLoggerHelper.LOGGER.d(Pair.create(CLASS_NAME, "log"), MainConstant.PREFERENCES, content + " " + key + " = " + value);
    }
    private void commitSharedPrefs(Editor editor){
        editor.commit();
    }

    public interface DefaultPrefs {

        boolean DEFAULT_SETTING_NOTIFICATION_MESSAGE_VIBRATION = true;

    }
}
