package com.androidtv.bhagavadgita.comman; // change to your actual package

import android.content.Context;
import android.util.Log;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.model.TranslationModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Singleton repository that fetches translation/commentary lists ONCE,
 * caches them in memory for the app session, and persists them to disk
 * so subsequent app launches don't need a network call at all (until
 * you decide to force-refresh).
 *
 * Usage from a Fragment:
 *   TranslationRepository.getInstance(getActivity())
 *       .getTranslation(mVersesModel, model -> { ... update UI ... });
 */
public class TranslationRepository {

    private static final String TAG = "TranslationRepository";
    private static final String TRANSLATION_CACHE_FILE = "translation_cache.json";
    private static final String COMMENTARY_CACHE_FILE = "commentary_cache.json";
    private static final int DEFAULT_LANGUAGE_ID = 1;

    private static volatile TranslationRepository instance;

    private final Context appContext;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    // In-memory caches. Null = not loaded yet.
    private volatile Map<String, TranslationModel> translationMap;
    private volatile Map<String, TranslationModel> commentaryMap;

    // Pending callbacks so we don't fire duplicate network calls if
    // multiple verses are requested while a load is already in flight.
    private final List<LookupRequest> pendingTranslationRequests = new ArrayList<>();
    private final List<LookupRequest> pendingCommentaryRequests = new ArrayList<>();
    private boolean isLoadingTranslation = false;
    private boolean isLoadingCommentary = false;

    private TranslationRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static TranslationRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (TranslationRepository.class) {
                if (instance == null) {
                    instance = new TranslationRepository(context);
                }
            }
        }
        return instance;
    }

    public interface LookupCallback {
        void onResult(TranslationModel model); // null if not found for this verse
    }

    private static class LookupRequest {
        final Integer verseId;
        final Integer verseNumber;
        final LookupCallback callback;

        LookupRequest(Integer verseId, Integer verseNumber, LookupCallback callback) {
            this.verseId = verseId;
            this.verseNumber = verseNumber;
            this.callback = callback;
        }
    }

    // ---------------------------------------------------------------
    // PUBLIC API
    // ---------------------------------------------------------------

    public void getTranslation(VersesModel verse, LookupCallback callback) {
        getEntry(verse, callback, true);
    }

    public void getCommentary(VersesModel verse, LookupCallback callback) {
        getEntry(verse, callback, false);
    }

    /** Call this e.g. on pull-to-refresh if you want fresh data from the server. */
    public void invalidateCache() {
        translationMap = null;
        commentaryMap = null;
        executor.execute(() -> {
            deleteCacheFile(TRANSLATION_CACHE_FILE);
            deleteCacheFile(COMMENTARY_CACHE_FILE);
        });
    }

    // ---------------------------------------------------------------
    // CORE LOGIC
    // ---------------------------------------------------------------

    private void getEntry(VersesModel verse, LookupCallback callback, boolean isTranslation) {
        Map<String, TranslationModel> map = isTranslation ? translationMap : commentaryMap;

        // 1. Already in memory -> instant return
        if (map != null) {
            callback.onResult(map.get(key(verse.getVerseId(), verse.getVerseNumber(), DEFAULT_LANGUAGE_ID)));
            return;
        }

        // 2. Queue this request; if a load is already running, just wait for it
        synchronized (this) {
            List<LookupRequest> pending = isTranslation ? pendingTranslationRequests : pendingCommentaryRequests;
            pending.add(new LookupRequest(verse.getVerseId(), verse.getVerseNumber(), callback));

            boolean alreadyLoading = isTranslation ? isLoadingTranslation : isLoadingCommentary;
            if (alreadyLoading) {
                return; // will be resolved when in-flight load completes
            }
            if (isTranslation) isLoadingTranslation = true; else isLoadingCommentary = true;
        }

        // 3. Try disk cache first (fast, no network)
        executor.execute(() -> {
            String cached = readCacheFile(isTranslation ? TRANSLATION_CACHE_FILE : COMMENTARY_CACHE_FILE);
            if (cached != null) {
                Map<String, TranslationModel> parsed = parseToMap(cached);
                if (parsed != null) {
                    onLoaded(parsed, isTranslation);
                    return;
                }
            }
            // 4. Nothing cached -> hit the network
            fetchFromNetwork(isTranslation);
        });
    }

    private void fetchFromNetwork(boolean isTranslation) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> call = isTranslation ? apiInterface.getTranslation() : apiInterface.getCommentary();

        // NOTE: APIClient.callAPI needs an Activity in your current code.
        // If you have a version that doesn't require an Activity, prefer that
        // here since the repository can be asked for data before any
        // fragment/activity view is ready. If not, pass the last known
        // activity in from the caller (see Fragment usage below).
        APIClient.callAPI(currentActivityRef, call, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                executor.execute(() -> {
                    writeCacheFile(isTranslation ? TRANSLATION_CACHE_FILE : COMMENTARY_CACHE_FILE, response);
                    Map<String, TranslationModel> parsed = parseToMap(response);
                    onLoaded(parsed, isTranslation);
                });
            }

            @Override
            public void onFailure(String error, int responseCode) {
                Log.w(TAG, "Load failed: " + error + " " + responseCode);
                onLoaded(new HashMap<>(), isTranslation); // resolve pending callbacks with "not found"
                resetLoadingFlag(isTranslation);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Load error: " + error);
                onLoaded(new HashMap<>(), isTranslation);
                resetLoadingFlag(isTranslation);
            }
        });
    }

    private void onLoaded(Map<String, TranslationModel> map, boolean isTranslation) {
        if (isTranslation) {
            translationMap = map;
        } else {
            commentaryMap = map;
        }

        List<LookupRequest> pending;
        synchronized (this) {
            pending = new ArrayList<>(isTranslation ? pendingTranslationRequests : pendingCommentaryRequests);
            (isTranslation ? pendingTranslationRequests : pendingCommentaryRequests).clear();
            if (isTranslation) isLoadingTranslation = false; else isLoadingCommentary = false;
        }

        for (LookupRequest req : pending) {
            TranslationModel result = map.get(key(req.verseId, req.verseNumber, DEFAULT_LANGUAGE_ID));
            // Hop back to main thread for UI-safe callback
            runOnMainThread(() -> req.callback.onResult(result));
        }
    }

    private void resetLoadingFlag(boolean isTranslation) {
        synchronized (this) {
            if (isTranslation) isLoadingTranslation = false; else isLoadingCommentary = false;
        }
    }

    // ---------------------------------------------------------------
    // PARSING / KEYING
    // ---------------------------------------------------------------

    private String key(Integer verseId, Integer verseNumber, int langId) {
        return verseId + "_" + verseNumber + "_" + langId;
    }

    private Map<String, TranslationModel> parseToMap(String json) {
        try {
            List<TranslationModel> list = gson.fromJson(json, new TypeToken<List<TranslationModel>>() {}.getType());
            Map<String, TranslationModel> map = new HashMap<>();
            if (list != null) {
                for (TranslationModel m : list) {
                    if (m.getLanguageId() != null && m.getLanguageId().equals(DEFAULT_LANGUAGE_ID)) {
                        map.put(key(m.getVerseId(), m.getVerseNumber(), m.getLanguageId()), m);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            Log.e(TAG, "Parse failed", e);
            return null;
        }
    }

    // ---------------------------------------------------------------
    // DISK CACHE (simple file-based; swap for Room/DB if data grows large)
    // ---------------------------------------------------------------

    private String readCacheFile(String fileName) {
        try {
            File file = new File(appContext.getFilesDir(), fileName);
            if (!file.exists()) return null;
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private void writeCacheFile(String fileName, String content) {
        try (FileOutputStream fos = appContext.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e(TAG, "Cache write failed", e);
        }
    }

    private void deleteCacheFile(String fileName) {
        new File(appContext.getFilesDir(), fileName).delete();
    }

    // ---------------------------------------------------------------
    // MAIN-THREAD HOP
    // ---------------------------------------------------------------

    private final android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    private void runOnMainThread(Runnable r) {
        mainHandler.post(r);
    }

    // If your APIClient.callAPI truly requires a MasterActivity, store the
    // last one passed in so background-triggered calls still work.
    private MasterActivity currentActivityRef;

    public void attachActivity(MasterActivity activity) {
        this.currentActivityRef = activity;
    }
}
