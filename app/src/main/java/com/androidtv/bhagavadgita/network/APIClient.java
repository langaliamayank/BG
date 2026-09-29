package com.androidtv.bhagavadgita.network;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.comman.ExceptionHandler;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;

public class APIClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(APIInterface.API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(NetworkClient.getUnsafeOkHttpClient())
                .build();

        return retrofit;
    }

    public static class RequestInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            // Get the original request
            Request request = chain.request();

            // Log the request method (GET, POST, etc.)
            String method = request.method();
            ExceptionHandler.recordException(new Exception("API Request Method: " + method));

            // Get and log the URL and query parameters
            HttpUrl url = request.url();
            ExceptionHandler.recordException(new Exception("API Request URL: " + String.valueOf(url)));
            LogTag.e("API Request URL: " + String.valueOf(url));

            // Log individual query parameters
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < url.querySize(); i++) {
                String queryParameterName = url.queryParameterName(i);
                String queryParameterValue = url.queryParameterValue(i);
                stringBuilder.append(queryParameterName + " => " + queryParameterValue);
            }

            ExceptionHandler.recordException(new Exception("API Parameter: " + stringBuilder.toString()));
            LogTag.e("API Parameter: " + stringBuilder.toString());

            // Log request headers
            ExceptionHandler.recordException(new Exception("API Request Headers: " + String.valueOf(request.headers())));
            LogTag.e("API Request Headers: " + String.valueOf(request.headers()));

            // Log request body (for POST, PUT, etc.)
            if (request.body() != null) {
                RequestBody requestBody = request.body();
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                String body = buffer.readUtf8();

                ExceptionHandler.recordException(new Exception("API Request Body: " + body));
                LogTag.e("API Request Body: " + body);
            }

            // Proceed with the request
            return chain.proceed(request);
        }
    }

    public static void callAPI(final MasterActivity activity, Call<ResponseBody> call, final APICallback apiCallback) {
        final long start = System.currentTimeMillis();
//        if (ConnectivityReceiver.isConnected() == false) {
//            if (activity != null) {
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        activity.showNoInternetDialog();
//                    }
//                });
//
//            }
//            return;
//        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                String url = call.request().url().toString();
                if (response.isSuccessful()) {
                    try {
                        String res = response.body().string();
//                        LogTag.e("API Response " + url + "\n" + res);

                        if (apiCallback != null) {
                            apiCallback.onSuccess(res);
                        }

                    } catch (Exception e) {
                        LogTag.e("API Exception " + url + "\n" + e.getMessage());
                        ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" + e + "\ncode: " + response.code()));
                        if (apiCallback != null) {
                            apiCallback.onSuccess("");
                        }
                    }

                } else {
                    if (response.code() == 401) {
//                        SharePreferenceManager.clearDB();
//                        activity.showMessageToUser("Session Expired");
//                        activity.openActivity(activity, LoginActivity.class);
//                        activity.finish();
                    }

                    if (apiCallback != null) {
                        try {
                            if (response.errorBody() != null) {
                                String res = response.errorBody().string();
                                LogTag.e("API ErrorBody " + url + "\n" + res);

                                try {
                                    JSONObject jsonObject = new JSONObject(res);
                                    if (jsonObject.has("message")) {
                                        apiCallback.onError(jsonObject.getString("message"));
                                        ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" +
                                                jsonObject.getString("message") + "\ncode: " + response.code()));
                                    }

                                    if (jsonObject.has("errors")) {
                                        apiCallback.onError(jsonObject.getString("errors"));
                                        ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" +
                                                jsonObject.getString("errors") + "\ncode: " + response.code()));
                                    }

                                } catch (IllegalStateException | JsonSyntaxException exception) {
                                    apiCallback.onError(exception.getMessage().toString());
                                    ExceptionHandler.recordException(new Exception("Url: " + url + "\nJSONException:" +
                                            exception.getMessage().toString() + "\ncode: " + response.code()));

                                } catch (Exception e) {
                                    apiCallback.onError(e.getMessage().toString());
                                    ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" +
                                            e.getMessage().toString() + "\ncode: " + response.code()));
                                }
                            }

                        } catch (Exception e) {
                            apiCallback.onError(e.getMessage().toString());
                            ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" + e + "\ncode: " + response.code()));
                            LogTag.e("API Exception " + url + "\n" + e + "\n" + response.code());
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String url = call.request().url().toString();

                long end = System.currentTimeMillis();
                if (t.getCause() != null && t.getCause() instanceof SSLHandshakeException) {
                    if (apiCallback != null) {
                        ExceptionHandler.recordException(new Exception("SSL Handshake Exception"));
                        apiCallback.onError("SSL Handshake Exception");
                    }
                    return;
                }
                if (t instanceof ConnectException) {
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                activity.showNoInternetDialog();
                            }
                        });

                    }
                    return;
                }

                ExceptionHandler.recordException(new Exception("Url: " + url + "\nException:" + t + " Duration:" + (end - start) + " in milliseconds"));
                LogTag.e("API Failure " + url + "\n" + t.toString());
            }
        });
    }

    public interface APICallback {
        void onSuccess(String response);

        void onFailure(String error, int responseCode);

        void onError(String error);
    }
}
