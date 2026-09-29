package com.androidtv.bhagavadgita;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.androidtv.bhagavadgita.comman.OnBackPressedListener;
import com.androidtv.bhagavadgita.fragment.MusicDetailFragment;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsAllModel;
import com.androidtv.bhagavadgita.model.music.albums.AlbumsResultsModel;
import com.androidtv.bhagavadgita.model.music.artists.ArtistsResultModel;
import com.androidtv.bhagavadgita.model.music.playlists.PlaylistsResultModel;
import com.androidtv.bhagavadgita.model.music.songs.SongsResultModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.stream.Collectors;

public class MusicDetailActivity extends MasterActivity {

    private Object object;
//    private OnBackPressedListener onBackPressedListener;

    public static Intent createIntent(Context context, SongsResultModel object) {
        Intent intent = new Intent(context, MusicDetailActivity.class);
        intent.putExtra("DATA", object);
        return intent;
    }

    public static Intent createIntent(Context context, ArtistsResultModel object) {
        Intent intent = new Intent(context, MusicDetailActivity.class);
        intent.putExtra("DATA", object);
        return intent;
    }

    public static Intent createIntent(Context context, AlbumsResultsModel object) {
        Intent intent = new Intent(context, MusicDetailActivity.class);
        intent.putExtra("DATA", object);
        return intent;
    }

    public static Intent createIntent(Context context, PlaylistsResultModel object) {
        Intent intent = new Intent(context, MusicDetailActivity.class);
        intent.putExtra("DATA", object);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_detail);

        object = getIntent().getSerializableExtra("DATA");
        if (object instanceof ArtistsResultModel) {
            ArtistsResultModel artists = (ArtistsResultModel) object;

            Glide.with(MusicDetailActivity.this)
                    .asDrawable()
                    .load(artists.getImage().get(artists.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.songImage));

            ((TextView) findViewById(R.id.textSongTitle)).setText(artists.getName());
            ((TextView) findViewById(R.id.textSongDetail)).setText(artists.getRole());
        }

        if (object instanceof AlbumsResultsModel) {
            AlbumsResultsModel albums = (AlbumsResultsModel) object;

            Glide.with(MusicDetailActivity.this)
                    .asDrawable()
                    .load(albums.getImage().get(albums.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.songImage));

            ((TextView) findViewById(R.id.textSongTitle)).setText(albums.getName());

            String artists = albums.getArtists().getAll().stream()
                    .map(AlbumsAllModel::getName)
                    .collect(Collectors.joining(", "));
            ((TextView) findViewById(R.id.textSongDetail)).setText(artists);
        }

        if (object instanceof PlaylistsResultModel) {
            PlaylistsResultModel playlist = (PlaylistsResultModel) object;

            Glide.with(MusicDetailActivity.this)
                    .asDrawable()
                    .load(playlist.getImage().get(playlist.getImage().size() - 1).getUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((ImageView) findViewById(R.id.songImage));

            ((TextView) findViewById(R.id.textSongTitle)).setText(playlist.getName());
            ((TextView) findViewById(R.id.textSongDetail)).setText("N/A");

        }

        if (savedInstanceState == null)
            switchFragment(new MusicDetailFragment());

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
//                        if (onBackPressedListener != null) {
//                            onBackPressedListener.doBack();
//                        } else {
                            finish();
//                        }
                    }
                });
    }

    public boolean switchFragment(final Fragment fragment) {
        if (fragment != null) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction();
                    transaction.setCustomAnimations(
                            R.anim.fadein,  // enter
                            R.anim.fadeout  // exit
                    );
                    transaction.replace(R.id.music_container, fragment);
                    transaction.addToBackStack(fragment.getClass().getName());
                    transaction.commitAllowingStateLoss();
                }
            }, 200);

            return true;
        }
        return false;
    }
}
