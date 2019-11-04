package com.example.demogallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements UpdateMediaRunnable.OnMediaChangedListener, LoadMediaRunnable.OnMediaLoadedListener {

    
    private static final int QUERY_ID = -1;
    private static final int RESTART_QUERY_ID = -2;
    private static final int FIRST_PAGE_ITEM_NUMBER = 30;
    private static final int ITEM_NUMBER_PER_PAGE = 30;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final String MEDIA_URL_COLUMN = MediaStore.Files.FileColumns.DATA;
    private static final String MEDIA_ID_COLUMN = MediaStore.Files.FileColumns._ID;
    private static final String MEDIA_TYPE_COLUMN = MediaStore.Files.FileColumns.MEDIA_TYPE;

    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private List<Item> itemList;
    private List<Item> itemSelectedList;
    private PriorityThreadPoolExecutor executor;

    private boolean shouldReloadGallery = false;

    private int currentPage = 0;

    private Thread mMediaUpdater;
    private Thread mMediaLoader;

    private MediaObserver mImageObserver;
    private MediaObserver mVideoObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = new PriorityThreadPoolExecutor(1, 20, 60L, TimeUnit.SECONDS);

        Log.d("anh.dt2", "Core = " + NUMBER_OF_CORES);

        itemList = new ArrayList<>();
        itemSelectedList = new ArrayList<>();

        galleryRecyclerView = (RecyclerView) findViewById(R.id.gallery_list);
        galleryAdapter = new GalleryAdapter(this, itemList, itemSelectedList, executor);

        LoadMediaRunnable runnable = new LoadMediaRunnable(this, itemList);
        runnable.setOnMediaLoadedListener(this);
        mMediaLoader = new Thread(runnable);
        mMediaLoader.start();

        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryRecyclerView.setAdapter(galleryAdapter);

        initContentObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldReloadGallery) {
            shouldReloadGallery = false;
            stopAllTasks();
            currentPage = 0;
            mMediaUpdater = new Thread(new UpdateMediaRunnable(this, itemList, itemSelectedList, this));
            mMediaUpdater.start();
            Log.d("anh.dt2", "Restart query first page...");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setMediaObserverListener();
    }

    private void removeContentObserver() {
        ContentResolver contentResolver = getContentResolver();
        contentResolver.unregisterContentObserver(mVideoObserver);
        contentResolver.unregisterContentObserver(mImageObserver);
    }

    private void initContentObserver() {
        Uri externalImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri externalVideosUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        mImageObserver = new MediaObserver(new Handler());
        mVideoObserver = new MediaObserver(new Handler());
        setMediaObserverListener();
        ContentResolver contentResolver = getContentResolver();
        contentResolver.
                registerContentObserver(
                        externalImagesUri,
                        false,
                        mImageObserver);
        contentResolver.
                registerContentObserver(
                        externalVideosUri,
                        false,
                        mVideoObserver);
    }

    private void setMediaObserverListener() {
        mImageObserver.setMediaChangedListener(new MediaObserver.OnMediaChangedListener() {
            @Override
            public void onMediaChanged() {
                Log.d("anh.dt2", "MainActivity onMediaChanged Image");
                shouldReloadGallery = true;
            }
        });
        mVideoObserver.setMediaChangedListener(new MediaObserver.OnMediaChangedListener() {
            @Override
            public void onMediaChanged() {
                Log.d("anh.dt2", "MainActivity onMediaChanged Video");
                shouldReloadGallery = true;
            }
        });
    }

    private void stopAllTasks() {
        mImageObserver.setMediaChangedListener(null);
        mVideoObserver.setMediaChangedListener(null);
        for (int i = -1; i <= currentPage; i++) {
            getLoaderManager().destroyLoader(i);
        }
        executor.getQueue().clear();
        if (mMediaUpdater != null && mMediaUpdater.isAlive()) {
            mMediaUpdater.interrupt();
        }
    }

    @Override
    protected void onDestroy() {
        removeContentObserver();
        super.onDestroy();
    }

    @Override
    public void onMediaUpdated() {
        galleryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemFirstPageLoaded(int pos) {
        if (galleryAdapter != null) galleryAdapter.notifyItemInserted(pos);
    }
}
