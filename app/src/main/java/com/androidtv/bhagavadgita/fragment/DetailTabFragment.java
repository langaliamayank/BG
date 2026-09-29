package com.androidtv.bhagavadgita.fragment;

import static com.androidtv.bhagavadgita.DetailActivity.mViewPager;
import static com.androidtv.bhagavadgita.DetailActivity.mAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.LegacyPlayerControlView;
import androidx.preference.PreferenceManager;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.Constants;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerController;
import com.androidtv.bhagavadgita.comman.SharePreferenceManager;
import com.androidtv.bhagavadgita.comman.TranslationRepository;
import com.androidtv.bhagavadgita.model.TranslationModel;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

@OptIn(markerClass = UnstableApi.class)
public class DetailTabFragment extends Fragment implements PlayerController {

    private static WeakReference<DetailTabFragment> sActiveFragment;
    private VersesModel mVersesModel;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();
    private int mSelectedIndex = 0;
    private Player player;

    public static DetailTabFragment newInstance(VersesModel versesModel, ArrayList<VersesModel> versesList, int position) {
        DetailTabFragment fragment = new DetailTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("DATA", versesModel);
        args.putSerializable("LIST", versesList);
        args.putInt("INDEX", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_viewpager_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            mVersesModel = (VersesModel) getArguments().getSerializable("DATA");
            mVersesList = (ArrayList<VersesModel>) getArguments().getSerializable("LIST");
            mSelectedIndex = getArguments().getInt("INDEX", 0);
        }

        DefaultTimeBar timeBar = getView().findViewById(R.id.exo_progress);
        timeBar.setFocusable(false);
        timeBar.setFocusableInTouchMode(false);
        timeBar.setClickable(false); // optional, if you also don't want touch/click interaction

        if (mVersesModel != null && !mVersesModel.isDummy()) {
            ((TextView) getView().findViewById(R.id.textCounter)).setText(mVersesModel.getChapterNumber() + "." + mVersesModel.getVerseNumber());
            ((TextView) getView().findViewById(R.id.textSlok)).setText(mVersesModel.getText().trim());
            ((TextView) getView().findViewById(R.id.textTransliteration)).setText(mVersesModel.getTransliteration().trim());

            String wordMeaning = mVersesModel.getWordMeanings().replace("; ", "\n");
            String[] word = wordMeaning.split("\n");

            int whiteColor = ContextCompat.getColor(getContext(), R.color.colorAccent);
            StringBuilder stringBuilder = new StringBuilder();
            for (String mn : word) {
                if (mn.indexOf("—") != -1) {
                    stringBuilder.append(
                            "<font color='#" + Integer.toHexString(whiteColor).substring(2) + "'>"
                                    + mn.substring(0, mn.indexOf("—")) + "</font> — " + mn.substring(mn.indexOf("—") + 1) + "<br>");
                } else {
                    stringBuilder.append(mn);
                }
            }

            ((TextView) getView().findViewById(R.id.textWordMeanings)).setText(HtmlCompat.fromHtml(stringBuilder.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY));

            Glide.with(getActivity())
                    .load(((MasterActivity) getActivity()).getImagePath(mVersesModel, false))
                    .into(((ImageView) getView().findViewById(R.id.imageview)));

            playMedia(mVersesModel);

            TranslationRepository.getInstance(getActivity()).attachActivity((MasterActivity) getActivity());

            loadTranslation(mVersesModel);
            loadCommentary(mVersesModel);
        }

        getActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        getActivity().sendBroadcast(new Intent("LAST_WATCH")
                                .setPackage(getActivity().getPackageName())
                                .putExtra("index", mViewPager.getCurrentItem()));
                        if (player != null && player.isPlaying()) {
                            player.pause();
                        }
                        getActivity().finish();
                    }
                });
    }

    private void loadTranslation(VersesModel mVersesModel) {
        TranslationRepository.getInstance(getActivity()).getTranslation(mVersesModel, model -> {
            View view = getView();
            if (view == null || !isAdded())
                return; // fragment view may be gone by the time this fires

            String text = (model != null)
                    ? model.getAuthorName() + "\n" + model.getDescription() + "\n\n"
                    : "";
            ((TextView) view.findViewById(R.id.textTranslation)).setText(text);
        });
    }

    private void loadCommentary(VersesModel mVersesModel) {
        TranslationRepository.getInstance(getActivity()).getCommentary(mVersesModel, model -> {
            View view = getView();
            if (view == null || !isAdded()) return;

            String text = (model != null)
                    ? model.getAuthorName() + "\n" + model.getDescription() + "\n\n"
                    : "";
            ((TextView) view.findViewById(R.id.textCommentary)).setText(text);
        });
    }

//    private void loadTranslation(VersesModel mVersesModel) {
//        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
//        Call<ResponseBody> loginCall = apiInterface.getTranslation();
//        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
//
//            @Override
//            public void onSuccess(String response) {
//        try {
////            String response = loadJSONFromAsset(getActivity(), "translation.json");
//            Gson gson = new Gson();
//            List<TranslationModel> translationList = gson.fromJson(response,
//                    new TypeToken<List<TranslationModel>>() {
//                    }.getType());
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (TranslationModel translationModel : translationList) {
//                if (translationModel.getVerseId().equals(mVersesModel.getVerseId()) &&
//                        translationModel.getVerseNumber().equals(mVersesModel.getVerseNumber()) &&
//                        translationModel.getLanguageId().equals(1)) {
//                    stringBuilder
//                            .append(translationModel.getAuthorName())
//                            .append("\n")
//                            .append(translationModel.getDescription())
//                            .append("\n\n"); // <-- adds next line
//                }
//            }
//
//            ((TextView) getView().findViewById(R.id.textTranslation)).setText(stringBuilder.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//            }
//
//            @Override
//            public void onFailure(String error, int responseCode) {
////                activity.showMessageToUser(error + " " + responseCode);
//            }
//
//            @Override
//            public void onError(String error) {
////                activity.showMessageToUser(error);
//            }
//        });
//    }
//
//    private void loadCommentary(VersesModel mVersesModel) {
//        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
//        Call<ResponseBody> loginCall = apiInterface.getCommentary();
//        APIClient.callAPI((MasterActivity) getActivity(), loginCall, new APIClient.APICallback() {
//
//            @Override
//            public void onSuccess(String response) {
//        try {
////            String response = loadJSONFromAsset(getActivity(), "commentary.json");
//            Gson gson = new Gson();
//            List<TranslationModel> translationList = gson.fromJson(response,
//                    new TypeToken<List<TranslationModel>>() {
//                    }.getType());
//
//            StringBuilder stringBuilder = new StringBuilder();
//            for (TranslationModel translationModel : translationList) {
//                if (translationModel.getVerseId().equals(mVersesModel.getVerseId()) &&
//                        translationModel.getVerseNumber().equals(mVersesModel.getVerseNumber()) &&
//                        translationModel.getLanguageId().equals(1)) {
//                    stringBuilder
//                            .append(translationModel.getAuthorName())
//                            .append("\n")
//                            .append(translationModel.getDescription())
//                            .append("\n\n"); // <-- adds next line
//                }
//            }
//
//            ((TextView) getView().findViewById(R.id.textCommentary)).setText(stringBuilder.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//            }
//
//            @Override
//            public void onFailure(String error, int responseCode) {
////                activity.showMessageToUser(error + " " + responseCode);
//            }
//
//            @Override
//            public void onError(String error) {

    /// /                activity.showMessageToUser(error);
//            }
//    });
//}
    @UnstableApi
    private void playMedia(VersesModel mVersesModel) {
        player = new ExoPlayer.Builder(getActivity()).build();
        player.addListener(new PlayerEventListener());
        ((LegacyPlayerControlView) getView().findViewById(R.id.playerView)).setPlayer(player);
        ((TextView) getView().findViewById(R.id.textTitle)).setText("|| " + mVersesModel.getChapterNumber() + "." + mVersesModel.getVerseNumber() + " ||");
        String audioUrl = APIInterface.VERSES_BASE_PATH + mVersesModel.getChapterNumber() + "/" + mVersesModel.getVerseNumber() + ".mp3";
        MediaItem mediaItem = new MediaItem.Builder().setUri(audioUrl)
                .build();

        player.setPlayWhenReady(false); // <-- don't start playing automatically
        player.setMediaItem(mediaItem, true);
        player.prepare();
    }

    @Override
    public void playMedia() {
        // Stop whichever fragment was previously playing, no matter what
        DetailTabFragment prev = (sActiveFragment != null) ? sActiveFragment.get() : null;
        if (prev != null && prev != this) {
            prev.pauseMedia();
        }
        sActiveFragment = new WeakReference<>(this);

        if (player != null) {
            player.play();
        }
    }

    @Override
    public void pauseMedia() {
        if (player != null) {
            player.pause(); // no isPlaying() guard — safe to call anytime, and isPlaying() can be false during buffering, which was letting stale players slip through
        }
    }

    @Override
    public void onDestroyView() {
        if (sActiveFragment != null && sActiveFragment.get() == this) {
            sActiveFragment = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroyView();
    }

    private class PlayerEventListener implements Player.Listener {

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == ExoPlayer.STATE_READY) {

            } else if (playbackState == ExoPlayer.STATE_BUFFERING) {

            } else if (playbackState == ExoPlayer.STATE_IDLE) {

            } else if (playbackState == ExoPlayer.STATE_ENDED) {
                advanceToNextPage();
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);

            StringWriter errors = new StringWriter();
            error.printStackTrace(new PrintWriter(errors));
            String errorLog = errors.toString();
            Log.e("Error", errorLog);
        }
    }

    private void advanceToNextPage() {
        if (mViewPager == null || mAdapter == null) return;

        boolean autoplay = SharePreferenceManager.getBoolean("AUTOPLAY", true);
        if (!autoplay) {
            pauseMedia();
            return;
        }

        int current = mViewPager.getCurrentItem();
        int next = current + 1;
        if (next < mAdapter.getCount()) {
            mViewPager.setCurrentItem(next, true);
        } else {
            pauseMedia();
        }
    }
}
