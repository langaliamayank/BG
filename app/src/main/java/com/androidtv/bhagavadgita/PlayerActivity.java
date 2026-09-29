package com.androidtv.bhagavadgita;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.LegacyPlayerControlView;

import com.androidtv.bhagavadgita.comman.LogTag;
import com.androidtv.bhagavadgita.comman.PlayerManager;
import com.androidtv.bhagavadgita.comman.ToggleButtonBase;
import com.androidtv.bhagavadgita.model.MediaCard;
import com.androidtv.bhagavadgita.model.music.songs.SongsAllModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.Glide;

import java.util.stream.Collectors;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends MasterActivity {
    private SongsResultModel song;
    private PlayerManager mPlayerManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPlayerManager = PlayerManager.getInstance(PlayerActivity.this);
        ((LegacyPlayerControlView) findViewById(R.id.playerView)).setPlayer(mPlayerManager.getPlayer());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            song = (SongsResultModel) bundle.getSerializable("DATA");
            playMedia(song);
        }

        showMetadata();
    }

    private void showMetadata() {
        MediaItem current = PlayerManager.getInstance(PlayerActivity.this).getPlayer().getCurrentMediaItem();
        if (current != null) {
            MediaCard card = new MediaCard(
                    current.mediaId,
                    current.mediaMetadata.writer != null ? current.mediaMetadata.writer.toString() : "Unknown", "",
                    current.mediaMetadata.title != null ? current.mediaMetadata.title.toString() : "Unknown",
                    current.mediaMetadata.subtitle != null ? current.mediaMetadata.subtitle.toString() : "Unknown",
                    current.mediaMetadata.artworkUri);

            ((TextView) findViewById(R.id.textTitle)).setText(card.getTitle());
            ((TextView) findViewById(R.id.textSubtitle)).setText(card.getSubtitle());
            Glide.with(PlayerActivity.this)
                    .load(card.getImageUri())
                    .into(((ImageView) findViewById(R.id.imagePoster)));

            Glide.with(PlayerActivity.this)
                    .asGif()
                    .load(R.drawable.ic_action_visualizer)
                    .into((ImageView) findViewById(R.id.imageVisualizer));


        }
    }

    private void playMedia(SongsResultModel songData) {
        if (songData == null || songData.getDownloadUrl() == null || songData.getDownloadUrl().isEmpty()) {
            Log.e("PlayerFragment", "Invalid song data: missing URL");
            return;
        }

        String url = songData.getDownloadUrl().get(0).getUrl();
        LogTag.e("playback " + url);

        if (url == null || url.isEmpty()) {
            Log.e("PlayerFragment", "Song URL is null/empty");
            return;
        }

        String artists = song.getArtists().getAll().stream()
                .map(SongsAllModel::getName)
                .collect(Collectors.joining(", "));

        mPlayerManager.playMedia(
                song.getId(),
                url,
                songData.getName(),
                artists,
                songData.getImage().get(songData.getImage().size() - 1).getUrl());

        ((ToggleButtonBase) findViewById(R.id.toggleRepeat)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentMode = mPlayerManager.getPlayer().getRepeatMode();
                int newMode;

                switch (currentMode) {
                    case Player.REPEAT_MODE_OFF:
                        newMode = Player.REPEAT_MODE_ONE;
                        ((ToggleButtonBase) findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_one);
                        break;
                    case Player.REPEAT_MODE_ONE:
                        newMode = Player.REPEAT_MODE_ALL;
                        ((ToggleButtonBase) findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_all);
                        break;
                    case Player.REPEAT_MODE_ALL:
                    default:
                        newMode = Player.REPEAT_MODE_OFF;
                        ((ToggleButtonBase) findViewById(R.id.toggleRepeat)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_repeat_off);
                        break;
                }

                mPlayerManager.getPlayer().setRepeatMode(newMode);
            }
        });

        ((ToggleButtonBase) findViewById(R.id.toggleShuffle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShuffled = mPlayerManager.getPlayer().getShuffleModeEnabled();
                mPlayerManager.getPlayer().setShuffleModeEnabled(!isShuffled);

                if (!isShuffled) {
                    ((ToggleButtonBase) findViewById(R.id.toggleShuffle)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_shuffle_on);
                } else {
                    ((ToggleButtonBase) findViewById(R.id.toggleShuffle)).mImageButton.setImageResource(androidx.media3.session.R.drawable.media3_icon_shuffle_off);
                }
            }
        });
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
}
