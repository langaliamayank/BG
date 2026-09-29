package com.androidtv.bhagavadgita.playback;


import static com.androidtv.bhagavadgita.DetailActivity.mViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.app.VideoSupportFragmentGlueHost;
import androidx.leanback.media.PlaybackGlue;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.HorizontalGridView;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackTransportRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.palette.graphics.Palette;

import com.androidtv.bhagavadgita.CalendarActivity;
import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.comman.ToggleButtonBase;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.androidtv.bhagavadgita.presenter.CustomListRowPresenter;
import com.androidtv.bhagavadgita.presenter.FixedPlaybackTransportRowPresenter;
import com.androidtv.bhagavadgita.presenter.NowPlayingPresenter;
import com.androidtv.bhagavadgita.presenter.SongsPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class PlaybackVideoFragment extends VideoSupportFragment implements OnBackPressedListener {

    public static ArrayObjectAdapter upNextRowList;
    public ArrayObjectAdapter upNextAdapter;
    public int currentIndex = 0;
    public SongsResultModel currentMovie;
    public ArrayList<SongsResultModel> currentMovieList;
    public static AwesomeTransportControlGlue transportControlGlue;
    private View fastForwardIndicatorView;
    private View rewindIndicatorView;
    private FragmentActivity activity;
    private SongsPresenter upNextPresenter;
    private PlaybackTransportRowPresenter.ViewHolder mPlaybackViewHolder;
    private boolean isPlaybackRowSelected = true;

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (!isInPictureInPictureMode) {
            showControlsOverlay(false);
        }
    }

    @Override
    public void hideControlsOverlay(boolean runAnimation) {
        // Prevent Leanback from hiding the controls overlay internally
        // Do not call super.hideControlsOverlay()
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure controls remain shown when resuming
        setControlsOverlayAutoHideEnabled(false);
        showControlsOverlay(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((PlaybackActivity) getActivity()).setOnBackPressedListener(this);

        activity = getActivity();

        currentMovie = PlayerManager.getInstance(activity).getPlayData();
        currentMovieList = (ArrayList<SongsResultModel>) PlayerManager.getInstance(activity).getPlaylist();
        currentIndex = PlayerManager.getInstance(activity).getCurrentIndex();

        if (currentMovieList != null && currentMovieList.size() > 0) {
            // 1. Instantiate Glue
            transportControlGlue = new AwesomeTransportControlGlue(
                    (MasterActivity) activity,
                    new ExoPlayerAdapter(activity), currentMovieList, this
            );
            transportControlGlue.setFav_movies(currentMovieList);

            // 2. Create and register the CUSTOM presenter BEFORE setHost() —
            // onAttachedToHost() (triggered by setHost) reads it right away.
            FixedPlaybackTransportRowPresenter playbackTransportRowPresenter =
                    new FixedPlaybackTransportRowPresenter();

            AudioHeatmapExtractor.extractHeatmapFromUrl(
                    getContext(),
                    Uri.parse(currentMovie.getDownloadUrl().get(0).getUrl()),
                    80, // Number of wave points
                    new AudioHeatmapExtractor.HeatmapCallback() {
                        @Override
                        public void onHeatmapReady(float[] heatData) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    playbackTransportRowPresenter.setHeatmapData(heatData);
                                });
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                        }
                    }
            );

            transportControlGlue.setPlaybackRowPresenter(playbackTransportRowPresenter);

            // 3. NOW attach host — it will pick up our custom presenter, not the default one.
            transportControlGlue.setHost(new VideoSupportFragmentGlueHost(this));

            // 4. Up Next Presenter
            upNextPresenter = new SongsPresenter((MasterActivity) getActivity(), 0);
            upNextAdapter = new ArrayObjectAdapter(upNextPresenter);

            // 5. Playlist
            transportControlGlue.setPlaylist(currentMovieList);

            // 6. Style AFTER setHost() — styling calls are safe here, only
            // *which instance* had to be set earlier.
            PlayerMoviePresenter mGridPresenter = new PlayerMoviePresenter(getActivity());
            playbackTransportRowPresenter.setSecondaryProgressColor(
                    getActivity().getResources().getColor(R.color.colorWhite));
            playbackTransportRowPresenter.setProgressColor(
                    getActivity().getResources().getColor(R.color.colorFocus));
            playbackTransportRowPresenter.setDescriptionPresenter(mGridPresenter);

            // 7. Load track
            transportControlGlue.load(currentIndex, currentMovieList.get(currentIndex));

            activity.startService(new Intent(activity, TVPlaybackService.class));

            setOnKeyInterceptListener((v, keyCode, event) -> transportControlKeyEvent(v, keyCode, event));

            playbackTransportRowPresenter.setOnSecondaryActionClickListener(this::onSecondaryActionClicked);
        }

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {

            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {


                // 1. Capture the ViewHolder only when it changes
                if (rowViewHolder instanceof PlaybackTransportRowPresenter.ViewHolder) {
                    mPlaybackViewHolder = (PlaybackTransportRowPresenter.ViewHolder) rowViewHolder;
                }

                if (mPlaybackViewHolder == null) return;

                // 2. Check if the selection state actually changed to avoid redundant animations
                boolean currentlySelected = (row instanceof PlaybackControlsRow);
                if (isPlaybackRowSelected == currentlySelected) return;

                isPlaybackRowSelected = currentlySelected;

                // 3. Cache the views once or retrieve them from the ViewHolder
                View view = mPlaybackViewHolder.view;
                float targetAlpha = isPlaybackRowSelected ? 1.0f : 0.0f;

                // 4. Group views to animate them efficiently
                int[] viewIds = {
                        R.id.playback_progress,
                        R.id.containerDock,
                        R.id.containerTimebar,
                        R.id.controls_card,
                        R.id.heatmapSeekBar
                };

                for (int id : viewIds) {
                    View v = view.findViewById(id);
                    if (v != null) {
                        v.animate()
                                .alpha(targetAlpha)
                                .setDuration(150)
                                .start();
                    }
                }
            }
        });

        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            if (row instanceof ListRow && ((ListRow) row).getAdapter() == upNextAdapter) {

                SongsResultModel movie = (SongsResultModel) item;
                int index = 0;

                for (SongsResultModel m : transportControlGlue.getPlaylist()) {
                    if (m.getId().equals(movie.getId())) {
                        transportControlGlue.load(index, m);
                        break;
                    }
                    index++;
                }
            }
        });
    }

    public void onSecondaryActionClicked(View view) {
        if (view.getId() == R.id.btn_repeat) {
            transportControlGlue.repeat();

            int repeatMode = transportControlGlue.repeatAction.getIndex();
            if (repeatMode == PlaybackControlsRow.RepeatAction.INDEX_ONE) {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        androidx.media3.session.R.drawable.media3_icon_repeat_one);
                ((ToggleButtonBase) view).mImageButton.setAlpha(1.0f);

            } else if (repeatMode == PlaybackControlsRow.RepeatAction.INDEX_ALL) {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        androidx.media3.session.R.drawable.media3_icon_repeat_all);
                ((ToggleButtonBase) view).mImageButton.setAlpha(1.0f);

            } else {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        androidx.media3.session.R.drawable.media3_icon_repeat_all);
                ((ToggleButtonBase) view).mImageButton.setAlpha(0.45f);
            }

        } else if (view.getId() == R.id.btn_shuffle) {
            transportControlGlue.shuffle();

            boolean isShuffleOn = (transportControlGlue.shuffleAction.getIndex() == PlaybackControlsRow.ShuffleAction.INDEX_ON);
            if (isShuffleOn) {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        androidx.media3.session.R.drawable.media3_icon_shuffle_on);
                ((ToggleButtonBase) view).mImageButton.setAlpha(1.0f);
            } else {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        androidx.media3.session.R.drawable.media3_icon_shuffle_off);
                ((ToggleButtonBase) view).mImageButton.setAlpha(0.45f);
            }

        } else if (view.getId() == R.id.btn_playpause) {
            if (transportControlGlue == null) return;

            transportControlGlue.togglePlayPause();

            if (view instanceof ToggleButtonBase) {
                ((ToggleButtonBase) view).mImageButton.setImageResource(
                        transportControlGlue.isPlaying()
                                ? androidx.media3.session.R.drawable.media3_icon_pause
                                : androidx.media3.session.R.drawable.media3_icon_play);

                ((ToggleButtonBase) view).mDescView.setText(
                        transportControlGlue.isPlaying() ? "Pause" : "Play");
            }

        } else if (view.getId() == R.id.btn_next) {
            transportControlGlue.next();
            int newPos = transportControlGlue.getSelectedPosition();
            upNextAdapter.notifyItemRangeChanged(newPos, 1);
            updateUpComingList(); /*next*/

        } else if (view.getId() == R.id.btn_prev) {
            transportControlGlue.previous();
            int newPos = transportControlGlue.getSelectedPosition();
            upNextAdapter.notifyItemRangeChanged(newPos, 1);
            updateUpComingList(); /*prev*/
        }
    }

    public void updateBackground(SongsResultModel song) {
        if (song == null || getActivity() == null || isDetached()) return;

        String imageUrl = null;
        if (song.getImage() != null && !song.getImage().isEmpty()) {
            imageUrl = song.getImage().get(song.getImage().size() - 1).getUrl();
        }

        if (imageUrl == null || imageUrl.isEmpty()) return;

        Glide.with(this)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        applyPaletteBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    public void updatePlayPauseButton() {
        View root = getView();
        if (root == null || transportControlGlue == null) return;
        View btn = root.findViewById(R.id.btn_playpause);
        if (btn instanceof ToggleButtonBase) {
            boolean playing = transportControlGlue.isPlaying();
            ToggleButtonBase t = (ToggleButtonBase) btn;
            t.mImageButton.setImageResource(playing
                    ? androidx.media3.session.R.drawable.media3_icon_pause
                    : androidx.media3.session.R.drawable.media3_icon_play);
            t.mDescView.setText(playing ? "Pause" : "Play");
        }
    }

    private void applyPaletteBackground(Bitmap rawBitmap) {
        if (getActivity() == null || isDetached() || rawBitmap == null) return;

        // 1. Crop out 15% borders to ignore red frame banners and text labels
        int insetX = (int) (rawBitmap.getWidth() * 0.15f);
        int insetY = (int) (rawBitmap.getHeight() * 0.15f);
        int cropW = Math.max(1, rawBitmap.getWidth() - (insetX * 2));
        int cropH = Math.max(1, rawBitmap.getHeight() - (insetY * 2));

        Bitmap croppedBitmap;
        try {
            croppedBitmap = Bitmap.createBitmap(rawBitmap, insetX, insetY, cropW, cropH);
        } catch (Exception e) {
            croppedBitmap = rawBitmap;
        }

        Palette.from(croppedBitmap).maximumColorCount(24).generate(palette -> {
            if (palette == null || getActivity() == null || isDetached()) return;

            Palette.Swatch bestSwatch = null;
            float highestScore = -1f;

            for (Palette.Swatch swatch : palette.getSwatches()) {
                float[] hsl = swatch.getHsl();
                float saturation = hsl[1];
                float lightness = hsl[2];

                // Ignore extreme darks and whites
                if (saturation < 0.12f || lightness < 0.15f || lightness > 0.88f) {
                    continue;
                }

                // Score favoring saturated tones
                float score = swatch.getPopulation() * (saturation * saturation);
                if (score > highestScore) {
                    highestScore = score;
                    bestSwatch = swatch;
                }
            }

            if (bestSwatch == null) bestSwatch = palette.getDominantSwatch();

            int baseColor = (bestSwatch != null) ? bestSwatch.getRgb() : 0xFF2A3338;

            // Aura color derived directly from base hue
            float[] baseHsl = new float[3];
            ColorUtils.colorToHSL(baseColor, baseHsl);
            baseHsl[1] = Math.min(1.0f, baseHsl[1] * 1.15f);
            baseHsl[2] = Math.min(0.80f, Math.max(0.50f, baseHsl[2] * 1.25f));
            int auraColor = ColorUtils.HSLToColor(baseHsl);

            int baseTopColor = ColorUtils.blendARGB(baseColor, Color.BLACK, 0.25f);
            int baseBottomColor = 0xFF0A0A0C;

            int auraStart = ColorUtils.setAlphaComponent(auraColor, (int) (255 * 0.32f));
            int auraCenter = ColorUtils.setAlphaComponent(auraColor, (int) (255 * 0.08f));
            int auraEnd = 0x00000000;

            int vignetteCenter = 0x00000000;
            int vignetteEdge = 0x3D000000;

            activity.runOnUiThread(() -> {
                if (activity == null || isDetached()) return;

                View gradientView = activity.findViewById(R.id.v_palette_gradient);
                if (gradientView == null) return;

                int width = activity.getResources().getDisplayMetrics().widthPixels;
                int height = activity.getResources().getDisplayMetrics().heightPixels;

                GradientDrawable layerA = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{baseTopColor, baseBottomColor}
                );

                GradientDrawable layerB = new GradientDrawable();
                layerB.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                layerB.setColors(new int[]{auraStart, auraCenter, auraEnd});
                layerB.setGradientCenter(0.65f, 0.35f);
                layerB.setGradientRadius(Math.max(width, height) * 0.70f);

                GradientDrawable layerC = new GradientDrawable();
                layerC.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                layerC.setColors(new int[]{vignetteCenter, vignetteEdge});
                layerC.setGradientCenter(0.50f, 0.50f);
                layerC.setGradientRadius(Math.max(width, height) * 0.80f);

                gradientView.setBackground(new LayerDrawable(
                        new GradientDrawable[]{layerA, layerB, layerC}
                ));
            });
        });
    }

    public SongsResultModel getMovie(int position) {
        try {
            return (SongsResultModel) upNextAdapter.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean removeMovie(String movieId) {
        SongsResultModel removedObject = null;
        for (int i = 0; i < upNextAdapter.size(); ++i) {
            SongsResultModel m = (SongsResultModel) upNextAdapter.get(i);
            if (m.getId().equals(movieId)) {
                removedObject = m;
                break;
            }
        }
        if (removedObject != null) {
            upNextAdapter.remove(removedObject);
            upNextAdapter.notifyArrayItemRangeChanged(0, upNextAdapter.size());
            return true;
        }
        return false;
    }

    public boolean transportControlKeyEvent(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            updateUpComingList(); /*key up -> transportControlKeyEvent*/

            if (!isPlaybackRowSelected) {
                showControlsOverlay(false);
                setSelectedPosition(0, true);
                View playPause = getView().findViewById(R.id.btn_playpause);
                if (playPause != null) {
                    playPause.setFocusable(true);
                    playPause.requestFocus();
                    return true;
                }
                return true;
            }

            return false;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            updateUpComingList(); /*key down transportControlKeyEvent*/

            if (mPlaybackViewHolder != null && mPlaybackViewHolder.view != null) {
                View progressBar = mPlaybackViewHolder.view.findViewById(R.id.playback_progress);
                if (progressBar != null && progressBar.isFocused()) {
                    View playPause = getView().findViewById(R.id.btn_playpause);
                    if (playPause != null) {
                        playPause.setFocusable(true);
                        playPause.requestFocus();
                        return true;
                    }
                }
            }

            return false;
        }

        if (isControlsOverlayVisible() || event.getRepeatCount() > 0) {
            setShowOrHideControlsOverlayOnUserInteraction(true);
        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                setShowOrHideControlsOverlayOnUserInteraction(event.getAction() != KeyEvent.ACTION_DOWN);
                animateIndicator(fastForwardIndicatorView);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                setShowOrHideControlsOverlayOnUserInteraction(event.getAction() != KeyEvent.ACTION_DOWN);
                animateIndicator(rewindIndicatorView);
            }
        }

        updateUpComingList(); /*transportControlKeyEvent*/
        return transportControlGlue.onKey(v, keyCode, event);
    }

    public void updateUpComingList() {
        HorizontalGridView gridView = (HorizontalGridView) activity.findViewById(R.id.row_content);
        if (gridView != null && !gridView.hasFocus()) {
            gridView.setSelectedPosition(transportControlGlue.getSelectedPosition());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        fastForwardIndicatorView = inflater.inflate(R.layout.view_playback_forward, viewGroup, false);
        viewGroup.addView(fastForwardIndicatorView);

        rewindIndicatorView = inflater.inflate(R.layout.view_playback_rewind, viewGroup, false);
        viewGroup.addView(rewindIndicatorView);

        return viewGroup;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set Leanback background to NONE so activity background is visible
        setBackgroundType(BG_NONE);

        // If you want to hide the black video surface, do it safely after view creation:
        if (getSurfaceView() != null) {
            getSurfaceView().setAlpha(0f); // alpha 0 avoids layout/context detachment crashes
        }

        if (currentMovie != null) {
            updateBackground(currentMovie);
        }

        List<SongsResultModel> playlist = transportControlGlue.getPlaylist();
        if (savedInstanceState == null && playlist.size() > 1) {
            ((ClassPresenterSelector) getAdapter().getPresenterSelector())
                    .addClassPresenter(ListRow.class, new CustomListRowPresenter((MasterActivity) getActivity()));
            ListRow upNextRow = new ListRow(1L, new HeaderItem("Up Next"), upNextAdapter);

            upNextRowList = ((ArrayObjectAdapter) getAdapter());
            upNextRowList.add(upNextRow);
            upNextAdapter.setItems(playlist, null);

            updateUpComingList(); /*onViewCreated*/
        }

        transportControlGlue.addPlayerCallback(new PlaybackGlue.PlayerCallback() {
            @Override
            public void onPlayCompleted(PlaybackGlue glue) {
                super.onPlayCompleted(glue);
                if (transportControlGlue.hasNext()) {
                    transportControlGlue.next();
                } else {
                    activity.finish();
                }
            }
        });
    }

    private void animateIndicator(View indicatorView) {
        indicatorView.animate().withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        indicatorView.setVisibility(View.GONE);
                        indicatorView.setAlpha(1F);
                        indicatorView.setScaleX(1F);
                        indicatorView.setScaleY(1F);
                    }
                }).withStartAction(() -> {
                    indicatorView.setVisibility(View.VISIBLE);
                }).alpha(0.2F)
                .scaleX(2f)
                .scaleY(2f)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    @Override
    public void doBack() {
        getActivity().sendBroadcast(new Intent("LAST_WATCH")
                .setPackage(getActivity().getPackageName())
                .putExtra("index", transportControlGlue.getSelectedPosition()));
        getActivity().finish();
    }
}
