package com.example.demogallery;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoadMediaRunnable implements Runnable {

    private static final String MEDIA_URL_COLUMN = MediaStore.Files.FileColumns.DATA;
    private static final String MEDIA_ID_COLUMN = MediaStore.Files.FileColumns._ID;
    private static final String MEDIA_TYPE_COLUMN = MediaStore.Files.FileColumns.MEDIA_TYPE;

    private Context mContext;
    private List<Item> itemList;
    private OnMediaLoadedListener onMediaLoadedListener;

    public LoadMediaRunnable(Context context, List<Item> items) {
        this.mContext = context.getApplicationContext();
        this.itemList = items;
    }

    public void setOnMediaLoadedListener(OnMediaLoadedListener listener) {
        this.onMediaLoadedListener = listener;
    }

    @Override
    public void run() {
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
        Cursor cursor = mContext.getContentResolver().query(
                queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
        );
        if (cursor != null && cursor.getCount() >= 1) {
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
                    item.setDuration(GalleryUtils.getDuration(mContext, item));
                }
                itemList.add(item);
                if (itemList.size() < 30) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            // Code here will run in UI thread
                            if (onMediaLoadedListener != null) onMediaLoadedListener.onItemFirstPageLoaded(itemList.size() - 1);
                        }
                    });
                }
            }
            cursor.close();


        }
    }

    public interface OnMediaLoadedListener {
        void onItemFirstPageLoaded(int pos);
    }
}
