package com.example.demogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryItemHolder> {

    private int priority = Integer.MAX_VALUE;

    private List<Item> itemList;
    private List<Item> itemSelectedList;
    private Context mContext;
    private ImageCache mImageCache;
    private PriorityThreadPoolExecutor mExecutor;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnBottomReachedListener {
        void onBottomReached();
    }

    public GalleryAdapter(Context context, List<Item> items, List<Item> itemSelectedList, PriorityThreadPoolExecutor mExecutor) {
        this.mContext = context.getApplicationContext();
        this.itemList = items;
        this.mImageCache = new ImageCache();
        this.mExecutor = mExecutor;
        this.itemSelectedList = itemSelectedList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public GalleryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
        return new GalleryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryItemHolder holder, final int position) {
        final Item item = itemList.get(position);
        updateItemChosen(holder, item);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.setSelected(!item.isSelected());
                if (item.isSelected()) itemSelectedList.add(item);
                else {
                    itemSelectedList.remove(item);
                    notifyItemChosenChanged();
                }
                notifyItemChanged(position);
                if (mListener != null) {
                    mListener.onItemClick(position);
                }
            }
        });

        if (item.isVideo()) {
            holder.playIcon.setVisibility(View.VISIBLE);
            holder.tvDuration.setVisibility(View.VISIBLE);
            holder.tvDuration.setText(item.getDuration() + "");
        }
        else {
            holder.playIcon.setVisibility(View.GONE);
            holder.tvDuration.setVisibility(View.GONE);
        }

        final String imageKey = itemList.get(position).getPath();
        Bitmap bitmap = mImageCache.getBitmapFromMemCache(imageKey);
        if (bitmap != null) {
            Log.d("anh.dt2", "Gallery Adapter Get " + imageKey + " from Memory cache");
            holder.galleryImage.setImageBitmap(bitmap);
        } else {
            Log.d("anh.dt2", "Gallery Adapter Get " + imageKey + " from Content Resolver");
            ThumbnailUpdateRunnable thumbnailUpdater;
            thumbnailUpdater = new ThumbnailUpdateRunnable(mContext, mImageCache, holder.galleryImage, itemList.get(position), priority--);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), thumbnailUpdater);
            holder.galleryImage.setImageDrawable(asyncDrawable);
            mExecutor.submit(thumbnailUpdater);
        }

    }

    private void updateItemChosen(@NonNull GalleryItemHolder holder, Item item) {
        if (item.isSelected()) {
            holder.itemChosen.setVisibility(View.VISIBLE);
            holder.numberChosen.setVisibility(View.VISIBLE);
            holder.numberChosen.setText((itemSelectedList.indexOf(item) + 1) + "");
        } else {
            holder.itemChosen.setVisibility(View.GONE);
            holder.numberChosen.setVisibility(View.GONE);
        }
    }

    private void notifyItemChosenChanged() {
        for (Item item : itemSelectedList) {
            int realPos = itemList.indexOf(item);
            if (realPos > -1) {
                notifyItemChanged(realPos);
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class GalleryItemHolder extends RecyclerView.ViewHolder {

        ConstraintLayout parentLayout;
        ImageView galleryImage;
        ImageView playIcon;
        TextView tvDuration;
        ImageView itemChosen;
        TextView numberChosen;

        GalleryItemHolder(View view) {
            super(view);
            parentLayout = view.findViewById(R.id.parent_layout);
            galleryImage = view.findViewById(R.id.gallery_image);
            playIcon = view.findViewById(R.id.play_icon);
            tvDuration = view.findViewById(R.id.tvDuration);
            itemChosen = view.findViewById(R.id.item_chosen);
            numberChosen = view.findViewById(R.id.number_chosen);
        }

    }
}
