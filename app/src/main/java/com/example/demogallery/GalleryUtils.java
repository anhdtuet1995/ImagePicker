package com.example.demogallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

public class GalleryUtils {

    public static Bitmap getThumbnail(Context context, Item item) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        if (item.isVideo()) {
            return MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), item.getId(), MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
        } else {
            return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), item.getId(), MediaStore.Images.Thumbnails.MINI_KIND, bmOptions);
        }
    }

    public static long getDuration(Context context, Item item) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        //use one of overloaded setDataSource() functions to set your data source
        retriever.setDataSource(context, Uri.parse(item.getPath()));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        return timeInMillisec;
    }

    public static ThumbnailUpdateRunnable getThumbnailUpdater(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getThumbnailUpdater();
            }
        }
        return null;
    }
}
