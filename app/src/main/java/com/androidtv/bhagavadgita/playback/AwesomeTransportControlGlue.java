package com.androidtv.bhagavadgita.playback;

import android.app.PictureInPictureParams;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.media3.common.util.UnstableApi;

import com.androidtv.bhagavadgita.MasterActivity;
import com.androidtv.bhagavadgita.R;
import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.model.music.songs.SongsAllModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@UnstableApi
public class AwesomeTransportControlGlue extends PlaybackTransportControlGlue<ExoPlayerAdapter> {
    private final ArrayList<SongsResultModel> playlist = new ArrayList<SongsResultModel>();
    public int playlistPosition = 0, currentIndex = 0;
    public PlaybackControlsRow.RepeatAction repeatAction;
    private List<Integer> shuffledPositions = new ArrayList<>();
    private MasterActivity activity;
    // Primary actions
    private PlaybackControlsRow.PlayPauseAction playPauseAction;
    private PlaybackControlsRow.FastForwardAction forwardAction;
    private PlaybackControlsRow.RewindAction rewindAction;
    private PlaybackControlsRow.SkipNextAction skipNextAction;
    private PlaybackControlsRow.SkipPreviousAction skipPreviousAction;
    public PlaybackControlsRow.ShuffleAction shuffleAction;
    public PlaybackControlsRow.PictureInPictureAction pipAction;
    public PlaybackControlsRow.ClosedCaptioningAction ccAction;

    private PlaybackVideoFragment playbackVideoFragment;
    private List<SongsResultModel> fav_movies = new ArrayList<>();

    public AwesomeTransportControlGlue(MasterActivity activity, ExoPlayerAdapter impl,
                                       ArrayList<SongsResultModel> list, PlaybackVideoFragment playbackVideoFragment) {
        super(activity, impl);
        this.activity = activity;
        this.playbackVideoFragment = playbackVideoFragment;

        setPlaylist(list);
        forwardAction = new PlaybackControlsRow.FastForwardAction(activity);
        rewindAction = new PlaybackControlsRow.RewindAction(activity);
        skipNextAction = new PlaybackControlsRow.SkipNextAction(activity);
        skipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(activity);

        playPauseAction = new PlaybackControlsRow.PlayPauseAction(activity);

        repeatAction = new PlaybackControlsRow.RepeatAction(activity);
        shuffleAction = new PlaybackControlsRow.ShuffleAction(activity);
        pipAction = new PlaybackControlsRow.PictureInPictureAction(activity);
        ccAction = new PlaybackControlsRow.ClosedCaptioningAction(activity);

        setSeekEnabled(true);
    }

    @Override
    protected void onPlayStateChanged() {
        super.onPlayStateChanged();
        if (playbackVideoFragment != null) playbackVideoFragment.updatePlayPauseButton();
    }

    public boolean contains(SongsResultModel movie) {
        if (fav_movies == null)
            return false;
        if (fav_movies.size() == 0)
            return false;
        for (SongsResultModel m : fav_movies) {
            if (m.getId().equals(movie.getId()))
                return true;
        }
        return false;
    }

    public void toggle(SongsResultModel movie) {
        if (contains(movie))
            fav_movies.remove(movie);
        else
            fav_movies.add(movie);
    }

    public void togglePlayPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public void setFav_movies(List<SongsResultModel> fav_movies) {
        this.fav_movies = fav_movies;
    }

    @Override
    protected void onCreatePrimaryActions(@org.jspecify.annotations.NonNull ArrayObjectAdapter primaryActionsAdapter) {

    }

    @Override
    public void setControlsRow(PlaybackControlsRow controlsRow) {
        super.setControlsRow(controlsRow);
//        if (controlsRow.getPrimaryActionsAdapter() != null) {
//            ArrayObjectAdapter adapter = new ArrayObjectAdapter(
//                    new NewControlButtonPresenterSelector());
//            onCreatePrimaryActions(adapter);
//            controlsRow.setPrimaryActionsAdapter(adapter);
//        }
//        if (controlsRow.getSecondaryActionsAdapter() != null) {
//            ArrayObjectAdapter adapter = new ArrayObjectAdapter(
//                    new NewControlButtonPresenterSelector());
//            onCreateSecondaryActions(adapter);
//            controlsRow.setSecondaryActionsAdapter(adapter);
//        }
    }

    private void attemptPipTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. CLEAR FOCUS: Force focus away from the transport row.
            // This is the #1 reason for "false" return.
            activity.getWindow().getDecorView().requestFocus();

            // 2. USE A NULL OR DUMMY RECT:
            // If you don't have the exact rect of the video surface,
            // passing a dummy rect is SAFER than passing the Control Row rect.
            // The system will default to an animation from the whole window.
            Rect sourceRect = new Rect(0, 0, 0, 0);

            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                builder.setAutoEnterEnabled(true);
                builder.setSourceRectHint(sourceRect);
            }

            PictureInPictureParams params = builder.build();

            // 3. APPLY PARAMS
            activity.setPictureInPictureParams(params);

            // 4. ENTER PIP
            boolean success = activity.enterPictureInPictureMode(params);
            LogTag.e("PIP_DEBUG " + "Enter PiP success: " + success);
        }
    }

    private void notifySecondaryActionChanged(Action act) {
        notifyItemChanged((ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter(), act);

    }

    @Override
    protected void onPreparedStateChanged() {
        super.onPreparedStateChanged();
    }

    @Override
    public void next() {
        if (getPlayerAdapter().repeatMode != PlaybackControlsRow.RepeatAction.INDEX_NONE)
            playlistPosition = (playlistPosition + 1) % playlist.size();

        else if (playlistPosition == playlist.size() - 1)
            return;

        else
            playlistPosition = playlistPosition >= playlist.size() ? playlist.size() - 1 : playlistPosition + 1;

        if (!getPlayerAdapter().shuffleEnabled) {
            loadMovie(playlistPosition); //next()
        } else {
            loadMovie(shuffledPositions.get(playlistPosition));//next()
        }
    }

    @Override
    public void previous() {
        if (playlistPosition > 0) {
            playlistPosition -= 1;
            if (getPlayerAdapter().shuffleEnabled)
                loadMovie(shuffledPositions.get(playlistPosition)); //previous()
            else
                loadMovie(playlistPosition);//previous()

        }
    }

    public void fastForward() {
        getPlayerAdapter().fastForward();
        onUpdateProgress();
    }

    public void rewind() {
        getPlayerAdapter().rewind();
        onUpdateProgress();
    }

    /**
     * 1. SHUFFLE: Toggles Shuffle between ON and OFF, shuffles indices,
     * and refreshes the 'Up Next' playlist.
     */
    public void shuffle() {
        if (shuffleAction == null || playlist.isEmpty()) return;

        shuffleAction.nextIndex();
        notifySecondaryActionChanged(shuffleAction);

        boolean isShuffleOn = (shuffleAction.getIndex() == PlaybackControlsRow.ShuffleAction.INDEX_ON);
        getPlayerAdapter().setShuffleAction(shuffleAction.getIndex());

        SongsResultModel currentSong = getCurrentMovie();

        if (isShuffleOn) {
            Collections.shuffle(shuffledPositions);
            ArrayList<SongsResultModel> shuffledList = new ArrayList<>();
            int newCurrentIndex = 0;

            for (int i = 0; i < shuffledPositions.size(); i++) {
                SongsResultModel song = playlist.get(shuffledPositions.get(i));
                if (currentSong != null && song.getId().equals(currentSong.getId())) {
                    newCurrentIndex = i;
                }
                shuffledList.add(song);
            }
            playlistPosition = newCurrentIndex;

            if (playbackVideoFragment != null && playbackVideoFragment.upNextAdapter != null) {
                playbackVideoFragment.upNextAdapter.setItems(shuffledList, null);
                playbackVideoFragment.upNextAdapter.notifyArrayItemRangeChanged(0, shuffledList.size());
                playbackVideoFragment.updateUpComingList();
            }
        } else {
            // Shuffle OFF: Restore original playlist order
            int originalIndex = 0;
            for (int i = 0; i < playlist.size(); i++) {
                if (currentSong != null && playlist.get(i).getId().equals(currentSong.getId())) {
                    originalIndex = i;
                    break;
                }
            }
            playlistPosition = originalIndex;

            if (playbackVideoFragment != null && playbackVideoFragment.upNextAdapter != null) {
                playbackVideoFragment.upNextAdapter.setItems(playlist, null);
                playbackVideoFragment.upNextAdapter.notifyArrayItemRangeChanged(0, playlist.size());
                playbackVideoFragment.updateUpComingList();
            }
        }
    }

    /**
     * 2. REPEAT: Cycles through NONE (off) -> ALL -> ONE,
     * updating the media player adapter mode.
     */
    public void repeat() {
        if (repeatAction == null) return;

        repeatAction.nextIndex();
        notifySecondaryActionChanged(repeatAction);

        // 2. Synchronize the adapter
        int currentRepeatMode = repeatAction.getIndex();
        getPlayerAdapter().setRepeatAction(currentRepeatMode);
    }

    /**
     * 3. CLOSE CAPTION: Toggles subtitles/captions ON and OFF.
     */
    public void closeCaption() {
        if (ccAction == null) return;

        int nextIndex = (ccAction.getIndex() + 1) % 2;
        ccAction.setIndex(nextIndex);
        notifySecondaryActionChanged(ccAction);

        // boolean subtitlesEnabled = (nextIndex == PlaybackControlsRow.ClosedCaptioningAction.INDEX_ON);
        // If your AwesomeMediaPlayerAdapter has caption support:
        // getPlayerAdapter().setSubtitlesEnabled(subtitlesEnabled);
    }

    /**
     * 4. PICTURE IN PICTURE: Enters TV PiP mode safely with aspect ratio.
     */
    public void pip() {
        if (pipAction == null) return;

        // Keep controls overlay visible or hide based on preference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            attemptPipTransition();
        }
    }

    private void updateMovieInfo(SongsResultModel songsResultModel) {
        if (songsResultModel != null) {
            setTitle(songsResultModel.getName());

            String artists = songsResultModel.getArtists().getAll().stream()
                    .map(SongsAllModel::getName)
                    .collect(Collectors.joining(", "));
            setSubtitle(artists);

            Glide.with(activity)
                    .asBitmap()
                    .load(songsResultModel.getImage().get(songsResultModel.getImage().size() - 1).getUrl())
                    .transition(GenericTransitionOptions.with(R.anim.fadein))
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            getControlsRow().setImageBitmap(activity, resource);
                            getHost().notifyPlaybackRowChanged();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            getControlsRow().setImageDrawable(placeholder);
                            getHost().notifyPlaybackRowChanged();
                        }
                    });
        } else {
            setTitle(null);
            setSubtitle(null);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (getHost().isControlsOverlayVisible() || event.getRepeatCount() > 0) {
            return super.onKey(v, keyCode, event);
        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;
                else {
                    onActionClicked(forwardAction);
                    return true;
                }
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;
                else {
                    onActionClicked(rewindAction);
                    return true;
                }
            } else {
                return super.onKey(v, keyCode, event);
            }
        }
    }

    public boolean hasNext() {
        return playlistPosition + 1 < playlist.size();
    }

    public int getSelectedPosition() {
        return playlistPosition;
    }

    public void loadMovie(int position) {
        if (position < 0 || position >= playlist.size()) return;

        this.playlistPosition = position;
        this.currentIndex = position;

        SongsResultModel movie = playlist.get(position);
        LogTag.e("loadMovie " + movie.toString());

        if (movie != null) {
            playbackVideoFragment.currentMovie = movie;

            // Make sure to use the proper audio/video stream URL:
            String mediaUrl = (movie.getDownloadUrl() != null && !movie.getDownloadUrl().isEmpty())
                    ? movie.getDownloadUrl().get(0).getUrl()
                    : movie.getUrl();

            if (mediaUrl != null) {
                getPlayerAdapter().setDataSource(Uri.parse(mediaUrl));
                updateMovieInfo(movie);
                playWhenPrepared();
            }
        }
    }

    public void load(int playlistPosition, SongsResultModel songResultModel) {
        LogTag.e("load " + songResultModel.toString());

        if (songResultModel != null) {
            playbackVideoFragment.currentMovie = songResultModel;
            this.playlistPosition = playlistPosition;

            if (songResultModel.getDownloadUrl().get(0).getUrl() != null) {
                getPlayerAdapter().setDataSource(Uri.parse(songResultModel.getDownloadUrl().get(0).getUrl()));
                updateMovieInfo(songResultModel);
                playWhenPrepared();
            }
        }
    }

    public List<SongsResultModel> getPlaylist() {
        if (!getPlayerAdapter().shuffleEnabled) {
            return playlist;
        } else {
            List<SongsResultModel> shuffledPlaylist = new ArrayList<>(playlist.size());
            for (Integer integer : shuffledPositions) {
                shuffledPlaylist.add(playlist.get(integer));
            }
            return shuffledPlaylist;
        }
    }

    public void setPlaylist(List<SongsResultModel> list) {
        playlist.clear();
        playlist.addAll(list);
        shuffledPositions.clear();
        for (int i = 0; i < playlist.size(); ++i) {
            shuffledPositions.add(i);
        }
    }

    public SongsResultModel getCurrentMovie() {
        if (getPlayerAdapter().shuffleEnabled) {
            return playlist.get(shuffledPositions.get(playlistPosition));
        } else
            return playlist.get(playlistPosition);
    }
}
