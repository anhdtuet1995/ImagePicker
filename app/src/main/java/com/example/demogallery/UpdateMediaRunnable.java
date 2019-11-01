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

public class UpdateMediaRunnable implements Runnable {

    private static final String MEDIA_URL_COLUMN = MediaStore.Files.FileColumns.DATA;
    private static final String MEDIA_ID_COLUMN = MediaStore.Files.FileColumns._ID;
    private static final String MEDIA_TYPE_COLUMN = MediaStore.Files.FileColumns.MEDIA_TYPE;

    private Context mContext;
    private int loadedItemNumber;
    private List<Item> itemList;
    private List<Item> selectedItems;
    private OnMediaChangedListener mListener;
    private HashMap<String, Item> newMediaHashMap;

    UpdateMediaRunnable(Context context, List<Item> items, List<Item> selectedItems, int loadedItemNumber, OnMediaChangedListener listener) {
        this.mContext = context.getApplicationContext();
        this.itemList = items;
        this.selectedItems = selectedItems;
        this.loadedItemNumber = loadedItemNumber;
        this.mListener = listener;
        this.newMediaHashMap = new LinkedHashMap<>();
    }

    @Override
    public void run() {
        int newDataSize = loadedItemNumber;
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
        Log.d("anh.dt2", "Restart query page with loaded items is " + loadedItemNumber + " ...");
        Cursor cursor = mContext.getContentResolver().query(
                queryUri,
                columns,
                selection,
                null, // Selection args (none).
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC LIMIT " + loadedItemNumber // Sort order.
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
                newMediaHashMap.put(mediaPath, item);
            }
            cursor.close();

            for (int i = 0; i < newDataSize; ) {
                Item item = itemList.get(i);
                if (!newMediaHashMap.containsKey(item.getPath())) {
                    itemList.remove(item);
                    selectedItems.remove(item);
                    newDataSize--;
                } else {
                    //if no changes
                    i++;
                }
            }

            String firstPath = null;
            if (itemList.size() > 0) {
                firstPath = itemList.get(0).getPath();
            }

            if (!newMediaHashMap.isEmpty()) {
                for (Map.Entry<String, Item> entry : newMediaHashMap.entrySet()) {
                    if (firstPath != null && !entry.getKey().equals(firstPath)) {
                        Log.d("anh.dt2", "New item is added.... Path = " + entry.getKey());
                        itemList.add(0, entry.getValue());
                    } else {
                        break;
                    }
                }
            }
            newMediaHashMap.clear();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // Code here will run in UI thread
                    if (mListener != null) mListener.onMediaUpdated();
                }
            });
        }
    }

    public interface OnMediaChangedListener {
        void onMediaUpdated();
    }

}
