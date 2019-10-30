package com.example.demogallery;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class ImageCache {

    private LruCache<String, Bitmap> mMemoryCache;

    public ImageCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 5;
        Log.d("anh.dt2", "cache size = " + cacheSize);
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                Log.d("anh.dt2", "Size of " + key + " to mem cache " + value.getByteCount()  / 1024);
                return value.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap value) {
        if (getBitmapFromMemCache(key) == null) {
            Log.d("anh.dt2", "Put " + key + " to mem cache");
            Bitmap bitmap = mMemoryCache.put(key, value);
            Log.d("anh.dt2", "Put " + key + " to success " + (bitmap != null));
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
}
