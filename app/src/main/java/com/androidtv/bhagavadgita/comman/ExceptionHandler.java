package com.androidtv.bhagavadgita.comman;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;

    public ExceptionHandler(Context context) {
        this.context = context;
    }

    public static void recordException(Throwable e) {
//        FirebaseCrashlytics.getInstance().recordException(e);
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
    }

    public static void recordResponseException(String url, Throwable e) {
//        FirebaseCrashlytics.getInstance().setCustomKey("Url", url);
//        FirebaseCrashlytics.getInstance().recordException(e);
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        e.printStackTrace();

//        FirebaseCrashlytics.getInstance().recordException(e);
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        if (context != null) {
//            Intent intent = new Intent(context, SplashActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            context.startActivity(intent);
        }
        try {
            System.exit(1);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
