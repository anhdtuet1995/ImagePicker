package com.example.demogallery;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class MediaObserver extends ContentObserver {

    private OnMediaChangedListener mListener;

    MediaObserver(Handler mHandler) {
        super(mHandler);
    }

    public interface OnMediaChangedListener {
        void onMediaChanged();
    }

    void setMediaChangedListener(OnMediaChangedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("anh.dt2", "onMediaChanged selfChange = " + selfChange);
        if (mListener != null) {
            mListener.onMediaChanged();
        }
    }

}
