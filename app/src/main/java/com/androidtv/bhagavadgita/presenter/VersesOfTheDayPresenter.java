package com.androidtv.bhagavadgita.presenter;


import android.graphics.Outline;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.ImageCardView;
import androidx.media3.common.Player;

import com.androidtv.bhagavadgita.DetailActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.model.ChapterModel;
import com.androidtv.bhagavadgita.model.VersesCache;
import com.androidtv.bhagavadgita.model.VersesModel;
import com.androidtv.bhagavadgita.network.APIClient;
import com.androidtv.bhagavadgita.network.APIInterface;
import com.androidtv.bhagavadgita.comman.WaveSeekBar;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class VersesOfTheDayPresenter extends AbstractBasePresenter<BaseCardView> {
    private MasterActivity mContext;
    private ArrayList<VersesModel> mVersesList = new ArrayList<>();
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;

    public VersesOfTheDayPresenter(MasterActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    protected BaseCardView onCreateView(ViewGroup parent) {
        mDefaultBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorTransparent);
        mSelectedBackgroundColor =
                ContextCompat.getColor(getContext(), R.color.colorWhite25);

        BaseCardView cardView = new BaseCardView(mContext, null, R.style.SideInfoCardStyle) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        cardView.setClickable(true);

        cardView.addView(LayoutInflater.from(mContext).inflate(R.layout.card_verse_otd_item, null));
        cardView.addOnLayoutChangeListener(sLayoutChangeListener);

        updateCardBackgroundColor(cardView, false);

        return cardView;
    }

    private void updateCardBackgroundColor(BaseCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        view.setBackgroundColor(color);
    }

    private View.OnLayoutChangeListener sLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            v.setPivotY(v.getMeasuredHeight());
        }
    };

    @Override
    public void onBindViewHolder(Object object, BaseCardView cardView) {

        if (object instanceof VersesModel) {
            VersesModel versesModel = (VersesModel) object;

            getChapterInformation(mContext, versesModel, new ChapterCallback() {
                @Override
                public void onChapterLoaded(ChapterModel chapterModel) {
                    ((TextView) cardView.findViewById(R.id.textChapterNumber)).setText(
                            chapterModel.getNameTranslation());
                    ((TextView) cardView.findViewById(R.id.textVerseNumber)).setText(
                            "Chapter " + chapterModel.getChapterNumber() + " - " +  "Verses " + versesModel.getVerseNumber());
                }

                @Override
                public void onError(String message) {

                }
            });

            String singleSpacedText = versesModel.getText().replaceAll("(\r?\n)+", "\n").trim();
            ((TextView) cardView.findViewById(R.id.textVerse)).setText(singleSpacedText);

            PlayerManager playerManager = PlayerManager.getInstance(cardView.getContext());
            WaveSeekBar waveSeekBar = cardView.findViewById(R.id.waveSeekBar);

            String audioUrl = APIInterface.VERSES_BASE_PATH + versesModel.getChapterNumber() + "/" +
                    versesModel.getVerseNumber() + ".mp3";

            playerManager.playMedia(
                    "MAYANK",
                    audioUrl,
                    versesModel.getTitle(),
                    versesModel.getChapterNumber() + " " + versesModel.getVerseNumber(),
                    mContext.getImagePath(versesModel, false));

            Handler progressHandler = new Handler(Looper.getMainLooper());
            Runnable updateProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (playerManager.getPlayer() != null && playerManager.isPlaying()) {
                        int currentPos = (int) playerManager.getCurrentPosition();
                        long totalDuration = playerManager.getDuration();

                        waveSeekBar.setProgress(currentPos);

                        String formattedTime = formatTime(currentPos) + " / " + formatTime(totalDuration);
                        ((TextView) cardView.findViewById(R.id.textCurrentDuration)).setText(formattedTime);

                        progressHandler.postDelayed(this, 100);
                    }
                }
            };


            ImageView buttonAction = cardView.findViewById(R.id.imageAction);
            playerManager.getPlayer().addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        long totalDuration = playerManager.getDuration();
                        waveSeekBar.visualize(versesModel, totalDuration, 0);
                    } else if (playbackState == Player.STATE_ENDED) {
                        progressHandler.removeCallbacks(updateProgressRunnable);

                        Player player = playerManager.getPlayer();
                        if (player != null) {
                            player.pause();
                            player.seekTo(0);
                        }

                        waveSeekBar.setProgress(0);
                        long totalDuration = playerManager.getDuration();
                        String formattedTime = "00:00 / " + formatTime(totalDuration);
                        ((TextView) cardView.findViewById(R.id.textCurrentDuration)).setText(formattedTime);

                        buttonAction.setImageResource(R.drawable.ic_action_play_small);
                    }
                }

                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    if (isPlaying) {
                        progressHandler.post(updateProgressRunnable);
                    } else {
                        progressHandler.removeCallbacks(updateProgressRunnable);
                    }

                    buttonAction.setImageResource(isPlaying
                            ? R.drawable.ic_action_pause_small
                            : R.drawable.ic_action_play_small);
                }
            });

            // 2. Allow user seeking
            waveSeekBar.setOnSeekBarChangeListener(new WaveSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onSeekBarSeekTo(WaveSeekBar seekBar, int position) {
                    if (playerManager.getPlayer() != null) {
                        playerManager.getPlayer().seekTo(position);
                    }
                }

                @Override
                public void onSeekBarTouchDown(WaveSeekBar seekBar) {
                }

                @Override
                public void onSeekBarTouchUp(WaveSeekBar seekBar) {
                }

                @Override
                public void onSeekBarSeeking(int seekingValue) {
                }
            });

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    togglePlayback(playerManager);
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    openDetailActivity(versesModel, playerManager, progressHandler, updateProgressRunnable);
                    return false;
                }
            });
        }
    }

    private void togglePlayback(PlayerManager playerManager) {
        Player player = playerManager.getPlayer();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    private void openDetailActivity(VersesModel versesModel,
                                    PlayerManager playerManager,
                                    Handler progressHandler,
                                    Runnable updateProgressRunnable) {

        if (progressHandler != null && updateProgressRunnable != null) {
            progressHandler.removeCallbacks(updateProgressRunnable);
        }

        Player player = playerManager.getPlayer();
        if (player != null) {
            player.pause();
            player.setPlayWhenReady(false); // Ensures Media3 ExoPlayer halts immediately
        }

        ArrayList<VersesModel> mVersesList = VersesCache.getInstance().getVerses();
        int mIndex = mVersesList.indexOf(versesModel);

        mContext.startActivity(DetailActivity.createIntent(mContext, versesModel, mIndex, false));
    }

    private void getChapterInformation(MasterActivity activity, VersesModel versesModel, ChapterCallback callback) {
        if (versesModel == null || activity == null) {
            if (callback != null) callback.onError("Activity or VersesModel is null");
            return;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getChapters();

        APIClient.callAPI(activity, loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ChapterModel>>() {}.getType();
                    List<ChapterModel> chapterList = gson.fromJson(response, listType);

                    if (chapterList == null || chapterList.isEmpty()) {
                        if (callback != null) callback.onError("Chapter list is empty");
                        return;
                    }

                    ChapterModel selectedChapterModel = null;
                    for (ChapterModel chapterModel : chapterList) {
                        if (chapterModel != null &&
                                Objects.equals(versesModel.getChapterNumber(), chapterModel.getChapterNumber())) {
                            selectedChapterModel = chapterModel;
                            break;
                        }
                    }

                    if (selectedChapterModel == null) {
                        if (callback != null) callback.onError("Chapter not found");
                        return;
                    }

                    // Deliver the returned object
                    if (callback != null) {
                        callback.onChapterLoaded(selectedChapterModel);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                if (callback != null) callback.onError(error);
            }

            @Override
            public void onError(String error) {
                if (callback != null) callback.onError(error);
            }
        });
    }

   /* private CompletableFuture<ChapterModel> getChapterInformation(MasterActivity activity, VersesModel versesModel) {
        CompletableFuture<ChapterModel> future = new CompletableFuture<>();

        if (versesModel == null || activity == null) {
            future.completeExceptionally(new IllegalArgumentException("Null input"));
            return future;
        }

        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseBody> loginCall = apiInterface.getChapters();

        APIClient.callAPI(activity, loginCall, new APIClient.APICallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    Type listType = new TypeToken<List<ChapterModel>>() {}.getType();
                    List<ChapterModel> chapterList = new Gson().fromJson(response, listType);

                    for (ChapterModel chapterModel : chapterList) {
                        if (chapterModel != null && Objects.equals(versesModel.getChapterNumber(), chapterModel.getChapterNumber())) {
                            future.complete(chapterModel); // Resolves the future
                            return;
                        }
                    }
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(String error, int responseCode) {
                future.completeExceptionally(new RuntimeException(error));
            }

            @Override
            public void onError(String error) {
                future.completeExceptionally(new RuntimeException(error));
            }
        });

        return future;
    }*/

    public interface ChapterCallback {
        void onChapterLoaded(ChapterModel chapterModel);
        void onError(String message);
    }

    private String formatTime(long millis) {
        if (millis < 0) return "00:00";
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }

    private void setChildRounded(ImageCardView cardView) {
        View mainImageView = cardView.getMainImageView();
        mainImageView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int cornerRadius = 13;
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
    }

    @Override
    public void onUnbindViewHolder(BaseCardView cardView) {
        super.onUnbindViewHolder(cardView);
    }

    public void setList(ArrayList<VersesModel> listFromAdapter) {
        mVersesList.addAll(listFromAdapter);
    }

    public ArrayList<VersesModel> getList() {
        return mVersesList;
    }

}
