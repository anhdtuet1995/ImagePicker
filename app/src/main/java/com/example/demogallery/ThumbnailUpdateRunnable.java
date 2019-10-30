package com.example.demogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class ThumbnailUpdateRunnable implements Runnable {

    private Item item;
    private Context context;
    private WeakReference<ImageView> imageViewReference;
    private ImageCache mImageCache;
    private int priority;

    public ThumbnailUpdateRunnable(Context context, ImageCache cache, ImageView imageView, Item item, int priority) {
        this.context = context.getApplicationContext();
        this.imageViewReference = new WeakReference<>(imageView);
        this.item =  item;
        this.mImageCache = cache;
        this.priority = priority;
    }

    @Override
    public void run() {
        final String imageKey = item.getPath();
        Log.d("anh.dt2", "Running " + imageKey + " on thread " + Thread.currentThread().getName() );
        Bitmap bitmap = mImageCache.getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            Log.d("anh.dt2", "Get " + imageKey + " from Memory cache");
        } else {
            bitmap = GalleryUtils.getThumbnail(context, item);
            mImageCache.addBitmapToMemoryCache(item.getPath(), bitmap);
            Log.d("anh.dt2", "Get " + imageKey + " from Content Resolver");
        }

        final Bitmap finalBitmap = bitmap;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Code here will run in UI thread
                if (imageViewReference != null && finalBitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        final ThumbnailUpdateRunnable thumbnailUpdater = GalleryUtils.getThumbnailUpdater(imageView);
                        if (thumbnailUpdater == ThumbnailUpdateRunnable.this) {
                            Log.d("anh.dt2", "Set " + imageKey + " bitmap");
                            imageView.setImageBitmap(finalBitmap);
                        }
                    }
                }
            }
        });
    }

    public int getPriority() {
        return priority;
    }
}
