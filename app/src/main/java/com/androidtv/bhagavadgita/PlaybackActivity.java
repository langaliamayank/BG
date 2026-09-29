package com.androidtv.bhagavadgita;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.text.Cue;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.CaptionStyleCompat;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.LegacyPlayerControlView;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;

import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.comman.ToggleButtonBase;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class PlaybackActivity extends MasterActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private PlayerManager mPlayerManager;
    private PlayerView mPlayerView;
    private ProgressBar mProgressBar;
    private ImageView mArtworkImageView;
    private RelativeLayout mTopController, mBottomController;

    private float currentSize = 0.053f; // Default "Medium"
    private int currentEdgeType = CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW;
    private SubtitleView subtitleView;

//    private void showTopBar(View view) {
//        view.animate()
//                .translationY(0)
//                .alpha(1f)
//                .setDuration(350)
//                .start();
//    }
//
//    private void hideTopBar(View view) {
//        view.animate()
//                .translationY(-view.getHeight())
//                .alpha(0f)
//                .setDuration(350)
//                .start();
//    }
//
//    private void slideUp(View view) {
//        view.animate()
//                .translationY(0) // move up
//                .alpha(1f)
//                .setDuration(350)
//
//                .start();
//    }
//
//    private void slideDown(View view) {
//        view.setVisibility(View.VISIBLE);
//        view.setAlpha(0f);
//        view.setTranslationY(-view.getHeight()); // start from up
//        view.animate()
//                .translationY(0)
//                .alpha(1f)
//                .setDuration(350)
//                .start();
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        mPlayerView = findViewById(R.id.playerView);
        mPlayerManager = PlayerManager.getInstance(PlaybackActivity.this);
        mTopController = mPlayerView.findViewById(R.id.exo_top_bar);
        mBottomController = mPlayerView.findViewById(R.id.exo_bottom_bar);

//        if (mTopController != null) mTopController.setTranslationY(-200f);
//        if (mBottomController != null) mBottomController.setTranslationY(200f);

        mProgressBar = findViewById(R.id.progress);
        mArtworkImageView = mPlayerView.findViewById(R.id.exo_thumbnail);
        mArtworkImageView.animate().alpha(0.0f).setDuration(2000); // Default

        mPlayerView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
            @Override
            public void onVisibilityChanged(int visibility) {
                if (mBottomController != null) {
//                    if (visibility == View.VISIBLE) {
//                        mBottomController.setVisibility(View.VISIBLE);
//                        mBottomController.animate().alpha(1f).setDuration(300).start();
//                    } else {
//                        mBottomController.animate().alpha(0f).setDuration(300).withEndAction(() -> {
//                            mBottomController.setVisibility(View.GONE);
//                        }).start();
//                    }
                }
            }
        });

//        mPlayerView.setControllerVisibilityListener(new PlayerView.ControllerVisibilityListener() {
//            @Override
//            public void onVisibilityChanged(int visibility) {
//                if (visibility == View.VISIBLE){
////                    showTopBar(mTopController);
////                    slideUp(mBottomController);
////                    showControllers();
////                    hideTopBar(mBottomController);
//                } if (visibility == View.GONE){
////                    hideTopBar(mTopController);
////                    slideDown(mBottomController);
////                    hideControllers();
////                    showTopBar(mBottomController);
//                }
//            }
//        });

        Player player = mPlayerManager.getPlayer();
        if (player == null) {
            LogTag.e("TAG" + "player is null!");
            return;
        }

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);

                showMetadata();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);

                if (playbackState == ExoPlayer.STATE_READY) {
                    mArtworkImageView.animate().alpha(0.0f).setDuration(2000);
                    mProgressBar.setVisibility(View.INVISIBLE);

                } else if (playbackState == ExoPlayer.STATE_BUFFERING) {
                    mProgressBar.setVisibility(View.VISIBLE);

                } else if (playbackState == ExoPlayer.STATE_IDLE) {
                    mArtworkImageView.animate().alpha(1.0f).setDuration(2000);
                    mProgressBar.setVisibility(View.INVISIBLE);

                } else if (playbackState == ExoPlayer.STATE_ENDED) {
                    mArtworkImageView.animate().alpha(1.0f).setDuration(2000);
                }
            }
        });


        mPlayerView.setPlayer(player);

        MediaCard mediaCard = new MediaCard("1", "Mayank", "https://html5demos.com/assets/dizzy.mp4", "Subtitles", "TTML positioning", null);
        MediaCard mediaCard1 = new MediaCard("2", "Mayank", "https://html5demos.com/assets/dizzy.mp4", "Subtitles", "TTML Japanese features", null);
        MediaCard mediaCard2 = new MediaCard("3", "Mayank", "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4", "Subtitles", "TTML Netflix Japanese examples (IMSC1.1)", null);
        MediaCard mediaCard3 = new MediaCard("4", "Mayank", "https://storage.googleapis.com/exoplayer-test-media-1/mp4/dizzy-with-tx3g.mp4", "Subtitles", "MPEG-4 Timed Text", null);
        MediaCard mediaCard4 = new MediaCard("5", "Mayank", "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-with-subrip.mkv", "Subtitles", "SubRip muxed into MKV", null);
        MediaCard mediaCard5 = new MediaCard("6", "Mayank", "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-with-overlapping-ssa.mkv", "Subtitles", "Overlapping SSA muxed into MKV", null);

        List<MediaCard> list = new ArrayList<>();
        list.add(mediaCard);
        list.add(mediaCard1);
        list.add(mediaCard2);
        list.add(mediaCard3);
        list.add(mediaCard4);
        list.add(mediaCard5);

//        mPlayerManager.playMedia("Mayank", "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/video-avc-baseline-480.mp4", "Subtitles", "TTML Netflix Japanese examples (IMSC1.1)", null);
        mPlayerManager.playMediaList(list);

        ((ToggleButtonBase) mPlayerView.findViewById(R.id.togglePlayPause)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ToggleButtonBase) mPlayerView.findViewById(R.id.togglePlayPause)).mImageButton.setImageResource(
                        player.isPlaying() ? androidx.media3.session.R.drawable.media3_icon_play :
                                androidx.media3.session.R.drawable.media3_icon_pause);
                ((ToggleButtonBase) mPlayerView.findViewById(R.id.togglePlayPause)).mDescView.setText(
                        player.isPlaying() ? "Play" : "Pause");
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.play();
                }
            }
        });

        subtitleView = mPlayerView.findViewById(R.id.exo_subtitles);
        setSubtitleSize(subtitleView, "Small"); // Options: "default"
        setSubtitleStyle(subtitleView, "Drop Shadow"); // Options: "default"

        mPlayerView.findViewById(R.id.linearSmall).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearMedium).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearLarge).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearDropShadow).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearDark).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearContrast).setOnClickListener(this);
        mPlayerView.findViewById(R.id.linearLight).setOnClickListener(this);

        mPlayerView.findViewById(R.id.linearSmall).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearMedium).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearLarge).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearDropShadow).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearDark).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearContrast).setOnFocusChangeListener(this);
        mPlayerView.findViewById(R.id.linearLight).setOnFocusChangeListener(this);


        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleSubtitle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerView.findViewById(R.id.linearSubtitle).setVisibility(View.VISIBLE);
                mBottomController.setVisibility(View.GONE);
//                TrackSelectionParameters current = player.getTrackSelectionParameters();
//                boolean showing = current.selectUndeterminedTextLanguage
//                        || current.preferredTextLanguages != null;
//
//                TrackSelectionParameters params = current.buildUpon()
//                        .setSelectUndeterminedTextLanguage(showing ? false : true)
//                        .clearOverridesOfType(C.TRACK_TYPE_TEXT)
//                        .build();
//                player.setTrackSelectionParameters(params);
//                Toast.makeText(PlaybackActivity.this, showing ? "Subtitles off" : "Subtitles on", Toast.LENGTH_SHORT).show();
            }
        });

        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleNext)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerManager.getPlayer().hasNextMediaItem()) {
                    mPlayerManager.getPlayer().seekToNext();
                }
            }
        });

        ((ToggleButtonBase) mPlayerView.findViewById(R.id.togglePrevious)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerManager.getPlayer().hasPreviousMediaItem()) {
                    mPlayerManager.getPlayer().seekToPrevious();
                }
            }
        });

        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleRepeat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMode = mPlayerManager.getPlayer().getRepeatMode();
                int newMode;

                switch (currentMode) {
                    case Player.REPEAT_MODE_OFF:
                        newMode = Player.REPEAT_MODE_ONE;
                        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_one);
                        break;
                    case Player.REPEAT_MODE_ONE:
                        newMode = Player.REPEAT_MODE_ALL;
                        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_all);
                        break;
                    case Player.REPEAT_MODE_ALL:
                    default:
                        newMode = Player.REPEAT_MODE_OFF;
                        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_off);
                        break;
                }

                mPlayerManager.getPlayer().setRepeatMode(newMode);
            }
        });

        ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleShuffle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShuffled = mPlayerManager.getPlayer().getShuffleModeEnabled();
                mPlayerManager.getPlayer().setShuffleModeEnabled(!isShuffled);

                if (!isShuffled) {
                    ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleShuffle)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_shuffle_on);
                } else {
                    ((ToggleButtonBase) mPlayerView.findViewById(R.id.toggleShuffle)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_shuffle_off);
                }
            }
        });


    }

//    private void showControllers() {
//        if (mTopController != null) {
//            mTopController.setVisibility(View.VISIBLE);
//            mTopController.animate()
//                    .translationY(0f)
//                    .alpha(1f)
//                    .setDuration(1000)
//                    .setListener(null)
//                    .start();
//        }
//
//        if (mBottomController != null) {
//            mBottomController.setVisibility(View.VISIBLE);
//            mBottomController.animate()
//                    .translationY(0f)
//                    .alpha(1f)
//                    .setDuration(1000)
//                    .setListener(null)
//                    .start();
//        }
//    }

//    private void hideContrrrollers() {
//        if (mTopController != null) {i
//            mTopController.animate()
//                    .translationY(-mTopController.getHeight())
//                    .alpha(0f)
//                    .setDuration(1000)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mTopController.setVisibility(View.GONE);
//                        }
//                    })
//                    .start();
//        }
//
//        if (mBottomController != null) {
//            mBottomController.animate()
//                    .translationY(mBottomController.getHeight())
//                    .alpha(0f)
//                    .setDuration(1000)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mBottomController.setVisibility(View.GONE);
//                        }
//                    })
//                    .start();
//        }
//    }

    @Override
    public void onBackPressed() {
        if (mPlayerView != null) {
//            if (mPlayerView != null && mPlayerView.isControllerFullyVisible()) {
////                hideControllers();
//                return; // prevent exiting immediately
//            }
        }
        // Controller already hidden → exit activity
        super.onBackPressed();
    }

    private void setSubtitleSize(SubtitleView subtitleView, String sizeName) {
        if (subtitleView == null) return;

        // 1. Resolve the scale using a helper method
        float textSize = getScaleFromName(sizeName);

        // 2. Apply to actual SubtitleView
        subtitleView.setFractionalTextSize(textSize);
        subtitleView.setViewType(SubtitleView.VIEW_TYPE_CANVAS);

        // 3. Cache UI references (Optimize performance)
        TextView labelSize = mPlayerView.findViewById(R.id.labelSize);
        TextView labelCaption = mPlayerView.findViewById(R.id.labelCaption);

        labelSize.setText(String.format("Size: %s", sizeName));

        // 4. Update Preview Text Size
        // Use post() to ensure the height is measured if the view is still initializing
        mPlayerView.post(() -> {
            int containerHeight = mPlayerView.getHeight();
            if (containerHeight > 0) {
                float pixelSize = textSize * containerHeight;
                labelCaption.setTextSize(TypedValue.COMPLEX_UNIT_PX, pixelSize);
            }
        });
    }

    private float getScaleFromName(String name) {
        if (name == null) return 0.05f;

        // Normalize string to handle case sensitivity and extra spaces
        switch (name.toLowerCase().trim()) {
            case "small":
                return 0.03f;
            case "large":
                return 0.08f;
            case "medium (default)":
            default:
                return 0.05f;
        }
    }

    private void setSubtitleStyle(SubtitleView subtitleView, String styleName) {
        if (subtitleView == null) return;

        // 1. Resolve the style object using a helper
        CaptionStyleCompat captionStyle = getStyleFromName(styleName);

        // 2. Apply to SubtitleView (The actual player)
        subtitleView.setStyle(captionStyle);
        subtitleView.setViewType(SubtitleView.VIEW_TYPE_CANVAS);

        // 3. Cache the Preview Views (Avoid multiple findViewByID calls)
        TextView labelStyle = mPlayerView.findViewById(R.id.labelStyle);
        TextView labelCaption = mPlayerView.findViewById(R.id.labelCaption);

        // 4. Update UI labels
        labelStyle.setText(String.format("Style: %s", styleName));

        // 5. Apply style properties to the Preview TextView
        labelCaption.setTextColor(captionStyle.foregroundColor);
        labelCaption.setBackgroundColor(captionStyle.backgroundColor);
        labelCaption.setTypeface(captionStyle.typeface);

        // 6. Handle Edge Styles
        applyEdgeToTextView(labelCaption, captionStyle.edgeType, captionStyle.edgeColor);
    }

    private CaptionStyleCompat getStyleFromName(String name) {
        // Use a normalized string for comparison
        String normalized = name != null ? name.toLowerCase().trim() : "";

        switch (normalized) {
            case "drop shadow":
                return new CaptionStyleCompat(Color.WHITE, Color.TRANSPARENT, Color.BLACK,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW, Color.BLACK, null);

            case "dark":
                return new CaptionStyleCompat(Color.WHITE, Color.BLACK, Color.BLACK,
                        CaptionStyleCompat.EDGE_TYPE_NONE, Color.BLACK, null);

            case "contrast":
                return new CaptionStyleCompat(Color.YELLOW, Color.BLACK, Color.BLACK,
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE, Color.WHITE, null);

            case "light":
                return new CaptionStyleCompat(Color.BLACK, Color.WHITE, Color.WHITE,
                        CaptionStyleCompat.EDGE_TYPE_NONE, Color.TRANSPARENT, null);

            default:
                return CaptionStyleCompat.DEFAULT;
        }
    }

    private void applyEdgeToTextView(TextView textView, int edgeType, int edgeColor) {
        switch (edgeType) {
            case CaptionStyleCompat.EDGE_TYPE_OUTLINE:
                textView.setShadowLayer(1.0f, 0, 0, edgeColor);
                break;
            case CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW:
                textView.setShadowLayer(4.0f, 2.0f, 2.0f, edgeColor);
                break;
            case CaptionStyleCompat.EDGE_TYPE_RAISED:
                textView.setShadowLayer(1.0f, -1.0f, -1.0f, edgeColor);
                break;
            case CaptionStyleCompat.EDGE_TYPE_DEPRESSED:
                textView.setShadowLayer(1.0f, 1.0f, 1.0f, edgeColor);
                break;
            default:
                textView.setShadowLayer(0, 0, 0, 0); // Clear shadow for NONE
                break;
        }
    }


    private void showMetadata() {
        MediaItem current = PlayerManager.getInstance(PlaybackActivity.this).getPlayer().getCurrentMediaItem();
        if (current != null) {
            MediaCard card = new MediaCard(
                    current.mediaId,
                    current.mediaMetadata.writer != null ? current.mediaMetadata.writer.toString() : "Unknown", "",
                    current.mediaMetadata.title != null ? current.mediaMetadata.title.toString() : "Unknown",
                    current.mediaMetadata.subtitle != null ? current.mediaMetadata.subtitle.toString() : "Unknown",
                    current.mediaMetadata.artworkUri);

            ((TextView) mPlayerView.findViewById(R.id.textTitle)).setText(card.getTitle());
            ((TextView) mPlayerView.findViewById(R.id.textSubtitle)).setText(card.getSubtitle());
            Glide.with(PlaybackActivity.this)
                    .load(card.getImageUri())
                    .into(((ImageView) mPlayerView.findViewById(R.id.imagePoster)));

            Glide.with(getApplicationContext())
                    .load(card.getImageUri())
                    .into(mArtworkImageView);
        }
    }

    private void playMedia(MediaCard mediaCard) {
        if (mediaCard == null) {
            Log.e("PlayerFragment", "Invalid song data: missing URL");
            return;
        }

        mPlayerManager.playMedia(
                mediaCard.getWriter(),
                mediaCard.getUrl(),
                mediaCard.getTitle(),
                mediaCard.getSubtitle(),
                String.valueOf(mediaCard.getImageUri()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        finish();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.linearSmall) {
            setSubtitleSize(subtitleView, "Small");
        }

        if (view.getId() == R.id.linearMedium) {
            setSubtitleSize(subtitleView, "Medium");
        }

        if (view.getId() == R.id.linearLarge) {
            setSubtitleSize(subtitleView, "Large");
        }

        if (view.getId() == R.id.linearDropShadow) {
            setSubtitleStyle(subtitleView, "Drop Shadow");
        }

        if (view.getId() == R.id.linearDark) {
            setSubtitleStyle(subtitleView, "Dark");
        }

        if (view.getId() == R.id.linearContrast) {
            setSubtitleStyle(subtitleView, "Contrast");
        }

        if (view.getId() == R.id.linearLight) {
            setSubtitleStyle(subtitleView, "Light");
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        int color = hasFocus ? Color.BLACK : Color.WHITE;

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    int id = child.getId();
                    if (id == R.id.textSmall || id == R.id.textMedium ||
                            id == R.id.textLarge || id == R.id.textDropShadow) {
                        ((TextView) child).setTextColor(color);
                    }
                }
            }
        }
    }
}
