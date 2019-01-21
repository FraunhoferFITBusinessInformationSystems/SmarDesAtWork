/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.viewelements.components.picturecomponent;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import de.vogler_engineering.smartdevicesapp.common.rx.SchedulersFacade;
import de.vogler_engineering.smartdevicesapp.viewelements.R;
import de.vogler_engineering.smartdevicesapp.viewelements.util.DisplayUtils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.PictureEntryViewHolder> implements Disposable {

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private static final String TAG = "PictureListAdapter";

    private final SchedulersFacade schedulersFacade;
    private final PictureListAdapterOnListChangedCallback onListChangedCallback;
    private ObservableArrayList<PictureEntry> list;
    private Drawable defaultImage = null;
    private Drawable errorImage = null;

    public PictureListAdapter(SchedulersFacade schedulersFacade) {
        this.schedulersFacade = schedulersFacade;
        this.onListChangedCallback = new PictureListAdapterOnListChangedCallback(this);
    }

    public void setItems(@Nullable ObservableArrayList<PictureEntry> items) {
        if (this.list == items) {
            return;
        }

        if (this.list != null) {
            this.list.removeOnListChangedCallback(onListChangedCallback);
            notifyItemRangeRemoved(0, this.list.size());
        }

        this.list = items;
        notifyItemRangeInserted(0, this.list.size());
        this.list.addOnListChangedCallback(onListChangedCallback);
    }

    @NonNull
    @Override
    public PictureEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final DisplayMetrics met = parent.getResources().getDisplayMetrics();
        int px;

        FrameLayout frameLayout = new FrameLayout(parent.getContext());
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        px = DisplayUtils.getFromDp(met, 10);
        frameLayoutParams.setMargins(px, px, px, px);
        frameLayout.setLayoutParams(frameLayoutParams);

        ImageView iv = new ImageView(parent.getContext());
        px = DisplayUtils.getFromDp(met, 100);
        FrameLayout.LayoutParams imageLayoutParams = new FrameLayout.LayoutParams(px, px);
        px = DisplayUtils.getFromDp(met, 4);
        imageLayoutParams.setMargins(px, px, px, px);
        iv.setLayoutParams(imageLayoutParams);

        if (defaultImage == null || errorImage == null) {
            defaultImage = parent.getResources().getDrawable(R.drawable.ic_photo_placeholder, null);
            errorImage = parent.getResources().getDrawable(R.drawable.ic_photo_error, null);
        }
        iv.setImageDrawable(defaultImage);

        frameLayout.addView(iv);
        return new PictureEntryViewHolder(frameLayout, iv);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureEntryViewHolder holder, int position) {
        boolean selected = list.get(position).isSelected();
        holder.setSelected(selected);

        holder.getFrameLayout().setOnClickListener((c) -> {
            PictureEntry e = list.get(position);
            e.setSelected(!e.isSelected());
            holder.setSelected(e.isSelected());
            notifyItemChanged(position);
//            notifyDataSetChanged();
        });

//        compositeDisposable.add(list.get(position).getThumbnailDrawable(schedulersFacade, )
//                .subscribeOn(schedulersFacade.ui())
//                .subscribe((drawable -> holder.getImageView().setImageDrawable(drawable)),
//                        error -> holder.getImageView().setImageDrawable(errorImage)));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void dispose() {
        compositeDisposable.dispose();
    }

    @Override
    public boolean isDisposed() {
        return compositeDisposable.isDisposed();
    }

    public class PictureEntryViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout mFrameLayout;
        private final ImageView mImageView;

        public PictureEntryViewHolder(FrameLayout mFrameLayout, ImageView mImageView) {
            super(mFrameLayout);
            this.mFrameLayout = mFrameLayout;
            this.mImageView = mImageView;
        }

        public FrameLayout getFrameLayout() {
            return mFrameLayout;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public void setSelected(boolean state) {
            if (state) {
                mFrameLayout.setBackgroundResource(R.drawable.bg_entry_selected);
            } else {
                mFrameLayout.setBackgroundResource(R.drawable.bg_entry_deselected);
            }
        }
    }

    private class PictureListAdapterOnListChangedCallback extends ObservableList.OnListChangedCallback {
        private final PictureListAdapter adapterReference;

        public PictureListAdapterOnListChangedCallback(PictureListAdapter adapter) {
            this.adapterReference = adapter;
        }

        @Override
        public void onChanged(ObservableList sender) {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null) {
                adapter.notifyItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null) {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null) {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference;
            if (adapter != null) {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}


