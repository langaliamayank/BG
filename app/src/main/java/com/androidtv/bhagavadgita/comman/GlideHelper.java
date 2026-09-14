package com.androidtv.bhagavadgita.comman;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.Locale;

public class GlideHelper {

    public static void loadImageWithCrossFadeTransition(Context context, ImageView imageView,
                                                        String url, final int crossFadeDuration,
                                                        int error) {

        // With this listener we override the onResourceReady of the RequestListener to force
        // the cross fade animation.
        RequestListener crossFadeListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                LogTag.e(String.format(Locale.ROOT, "onException(%s, %s, %s, %s)", e, model,
                        target, isFirstResource) + e);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                ImageViewTarget<Drawable> imageTarget
                        = (ImageViewTarget<Drawable>) target;
                Drawable current = imageTarget.getCurrentDrawable();
                if (current != null) {
                    TransitionDrawable transitionDrawable
                            = new TransitionDrawable(new Drawable[]{current, resource});
                    transitionDrawable.setCrossFadeEnabled(true);
                    transitionDrawable.startTransition(crossFadeDuration);
                    imageTarget.setDrawable(transitionDrawable);
                    return true;
                } else
                    return false;
            }
        };

        loadImageIntoView(imageView, context, url, new LoggingListener<>(), error,
                imageView.getDrawable());

        // Adding this second Glide call enables cross-fade transition even if the image is cached.
        createDrawableRequestBuilder(context, url, crossFadeListener, error,
                imageView.getDrawable()).transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
    }

    public static void loadImageIntoView(ImageView imageView, Context context, String url,
                                         RequestListener<Drawable> listener,
                                         int error, Drawable placeholder) {

        createDrawableRequestBuilder(context, url, listener, error, placeholder).into(imageView);
    }

    public static class LoggingListener<T> implements RequestListener<T> {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<T> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(T resource, Object model, Target<T> target, DataSource dataSource, boolean isFirstResource) {
            return false;
        }
    }

    private static RequestBuilder createDrawableRequestBuilder(Context context, String
            url, RequestListener<Drawable> listener, int error, Drawable placeholder) {

        return Glide.with(context)
                .load(url)
                .listener(listener)
                .placeholder(placeholder)
                .error(error);
    }

    public static void loadWithCinematicZoom(Context context, final ImageView imageView, String url,
                                             int crossFadeDuration, int errorPlaceholder) {

//        loadImageWithCrossFadeTransition(context, imageView, url, crossFadeDuration, errorPlaceholder);

        clearImage(imageView);
        Glide.with(context)
                .load(url)
                .placeholder(errorPlaceholder)
                .error(errorPlaceholder)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        imageView.setAlpha(1.0f);
                        imageView.setScaleX(1.0f);
                        imageView.setScaleY(1.0f);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        imageView.setScaleX(1.1f);
                        imageView.setScaleY(1.1f);
                        imageView.setAlpha(0f);

                        imageView.animate()
                                .alpha(1.0f)
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(crossFadeDuration)
                                .setInterpolator(new LinearInterpolator())
                                .start();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        imageView.animate().cancel();
                        imageView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageView.animate().cancel();
                        imageView.setImageDrawable(errorDrawable);
                    }
                });
    }

    public static void clearImage(ImageView imageView) {
        if (imageView == null) return;

        imageView.animate().cancel();
        imageView.setImageDrawable(null);
        imageView.setAlpha(1.0f);
        imageView.setScaleX(1.0f);
        imageView.setScaleY(1.0f);
    }
}