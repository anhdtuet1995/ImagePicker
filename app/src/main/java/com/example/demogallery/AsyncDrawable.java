package com.example.demogallery;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

public class AsyncDrawable extends BitmapDrawable {

    private final WeakReference<ThumbnailUpdateRunnable> thumbnailUpdaterWeakReference;

    public AsyncDrawable(Resources res, ThumbnailUpdateRunnable thumbnailUpdater) {
        super(res);
        thumbnailUpdaterWeakReference = new WeakReference<ThumbnailUpdateRunnable>(thumbnailUpdater);
    }

    public ThumbnailUpdateRunnable getThumbnailUpdater() {
        return thumbnailUpdaterWeakReference.get();
    }
}
