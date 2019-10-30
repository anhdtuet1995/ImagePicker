package com.example.demogallery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    
    private static final int QUERY_ID = -1;
    private static final int FIRST_PAGE_ITEM_NUMBER = 27;
    private static final int ITEM_NUMBER_PER_PAGE = 21;
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final String MEDIA_URL_COLUMN = MediaStore.Files.FileColumns.DATA;
    private static final String MEDIA_ID_COLUMN = MediaStore.Files.FileColumns._ID;
    private static final String MEDIA_TYPE_COLUMN = MediaStore.Files.FileColumns.MEDIA_TYPE;

    private RecyclerView galleryRecyclerView;
    private GalleryAdapter galleryAdapter;
    private List<Item> itemList;
    private List<Item> itemSelectedList;
    private PriorityThreadPoolExecutor executor;

    private int currentPage = 0;

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

        getLoaderManager().initLoader(QUERY_ID, null, this);

        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryRecyclerView.setAdapter(galleryAdapter);

        galleryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int totalItemCount = 0;
                int lastVisible = 0;
                if (layoutManager != null) {
                    totalItemCount = layoutManager.getItemCount();
                    lastVisible = layoutManager.findLastVisibleItemPosition();
                }

                boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
                if (totalItemCount > 0 && endHasBeenReached) {
                    getLoaderManager().initLoader(currentPage, null, MainActivity.this);
                }
            }
        });
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] columns = {MEDIA_ID_COLUMN,
                MEDIA_URL_COLUMN,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATE_ADDED,
        };
        String selection = MEDIA_TYPE_COLUMN + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MEDIA_TYPE_COLUMN + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        Uri queryUri = MediaStore.Files.getContentUri("external");
        if (id == QUERY_ID) {
            return new CursorLoader(this,
                    queryUri,
                    columns,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT " + FIRST_PAGE_ITEM_NUMBER // Sort order.
            );
        } else if (id == currentPage) {
            int loadedItems = FIRST_PAGE_ITEM_NUMBER + ITEM_NUMBER_PER_PAGE * currentPage;
            return new CursorLoader(this,
                    queryUri,
                    columns,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT " + loadedItems + "," + ITEM_NUMBER_PER_PAGE // Sort order.
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        currentPage++;
        int column_index_data = cursor.getColumnIndexOrThrow(MEDIA_URL_COLUMN);
        int column_index_id = cursor.getColumnIndexOrThrow(MEDIA_ID_COLUMN);
        int column_index_type = cursor.getColumnIndex(MEDIA_TYPE_COLUMN);
        String mediaPath;
        int type, id;

        while (cursor.moveToNext()) {
            mediaPath = cursor.getString(column_index_data);
            type = cursor.getInt(column_index_type);
            id = cursor.getInt(column_index_id);
            Log.d("anh.dt2", mediaPath);
            Item item = new Item(id, mediaPath, type != MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                item.setDuration(GalleryUtils.getDuration(this, item));
            }
            itemList.add(item);
            int currentPos = itemList.size() - 1;
            galleryAdapter.notifyItemInserted(currentPos);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}