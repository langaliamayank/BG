package com.androidtv.bhagavadgita.comman;

import android.util.Log;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class LogTag {
    private static final String TAG = "BG:";

    public static void e(String msg) {
        Log.e(TAG, msg);
        Log.d(TAG, msg);
//        Exception exception = new Exception(msg);
//        FirebaseCrashlytics.getInstance().recordException(exception);
    }
}
