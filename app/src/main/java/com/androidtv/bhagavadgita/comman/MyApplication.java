package com.androidtv.bhagavadgita.comman;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.model.DarshanModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.conscrypt.Conscrypt;

import java.lang.ref.WeakReference;
import java.security.Security;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class MyApplication extends Application {

    private static MyApplication instance;
    private String TAG = MyApplication.class.getSimpleName();

    // Weak reference so we don't leak the Activity if it's destroyed
    private WeakReference<MasterActivity> currentActivityRef;

    public static MyApplication getInstance() {
        return instance;
    }

    public static List<DarshanModel> darshanList = new ArrayList<>();

    public static boolean hasNetwork() {
        return instance.isNetworkConnected();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (instance == null) {
            instance = this;
        }

        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getApplicationContext()));

        // Track whichever MasterActivity is currently in the foreground
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof MasterActivity) {
                    currentActivityRef = new WeakReference<>((MasterActivity) activity);
                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
                if (currentActivityRef != null && currentActivityRef.get() == activity) {
                    currentActivityRef = null;
                }
            }

            // Unused lifecycle callbacks - required by the interface
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override public void onActivityStarted(Activity activity) {}
            @Override public void onActivityStopped(Activity activity) {}
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    private MasterActivity getCurrentActivity() {
        return currentActivityRef != null ? currentActivityRef.get() : null;
    }



    public interface DarshanListCallback {
        void onLoaded(List<DarshanModel> darshanList);
        void onFailed(String error);
    }

    public void createNextDarshan(DarshanListCallback callback) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getDarshan();

        MasterActivity activity = getCurrentActivity();

        APIClient.callAPI(activity, loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                List<DarshanModel> newList = new ArrayList<>();

                try {
                    List<DarshanModel> parsedList = new Gson().fromJson(response, new TypeToken<List<DarshanModel>>() {}.getType());

                    if (parsedList == null) {
                        LogTag.e("createNextDarshan: parsed list is null");
                        if (callback != null) callback.onFailed("Empty response");
                        return;
                    }

                    LocalTime now = LocalTime.now();
                    for (DarshanModel darshanModel : parsedList) {
                        darshanModel.updateStatus(now);
                        newList.add(darshanModel);
                    }

                    darshanList = newList;

                    if (callback != null) callback.onLoaded(darshanList);

                } catch (Exception e) {
                    e.printStackTrace();
                    LogTag.e("createNextDarshan parse error: " + e.getMessage());
                    if (callback != null) callback.onFailed(e.getMessage());
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                LogTag.e("createNextDarshan " + error + " " + responseCode);
                if (callback != null) callback.onFailed(error);
            }

            @Override
            public void onError(String error) {
                LogTag.e("createNextDarshan " + error);
                if (callback != null) callback.onFailed(error);
            }
        });
    }

    public static DarshanModel getDarshanTheme() {
        if (darshanList != null && !darshanList.isEmpty()) {
            return DarshanModel.getActiveTheme(darshanList);
        }
        return null;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}