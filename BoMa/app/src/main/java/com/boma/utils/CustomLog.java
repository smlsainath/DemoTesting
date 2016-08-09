package com.boma.utils;


import android.util.Log;

import com.boma.appconfig.ApplicationSettings;


/**
 * Created by SAI on 3/23/2016.
 */
public class CustomLog {

    public CustomLog() {

    }

    public static void debug(String tag, String message) {
        try {
            if (!ApplicationSettings.ENV_RELEASE)
                Log.d(tag, message);
        } catch (Exception e) {

        }

    }

    public static void error(String tag, String message) {
        try {
            if (!ApplicationSettings.ENV_RELEASE)
                Log.e(tag, message);
        } catch (Exception e) {

        }
    }

    public static void info(String tag, String message) {
        try {
            if (!ApplicationSettings.ENV_RELEASE)
                Log.i(tag, message);
        } catch (Exception e) {

        }

    }

}
