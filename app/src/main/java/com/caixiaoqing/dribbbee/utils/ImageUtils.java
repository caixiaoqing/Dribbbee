package com.caixiaoqing.dribbbee.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.caixiaoqing.dribbbee.R;
import com.caixiaoqing.dribbbee.dribbble.Dribbble;
import com.caixiaoqing.dribbbee.model.Shot;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Picasso;

/**
 * Created by caixiaoqing on 22/12/16.
 */

public class ImageUtils {

    public static void loadUserPicture(@NonNull final Context context,
                                       @NonNull ImageView imageView,
                                       @NonNull String url) {

        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.user_picture_placeholder)
                .into(imageView);
    }

    public static void loadCircularUserPicture(@NonNull final Context context,
                                               @NonNull ImageView imageView,
                                               @NonNull String url) {

        Glide.with(context)
                .load(url)
                .asBitmap()
                .placeholder(ContextCompat.getDrawable(context, R.drawable.user_picture_placeholder))
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        view.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public static void loadShotImage(@NonNull Shot shot, @NonNull SimpleDraweeView imageView) {
        String imageUrl = shot.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Uri imageUri = Uri.parse(imageUrl);
            if (shot.animated) {
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(imageUri)
                        .setAutoPlayAnimations(true)
                        .build();
                imageView.setController(controller);
            } else {
                imageView.setImageURI(imageUri);
            }
        }
    }
}

