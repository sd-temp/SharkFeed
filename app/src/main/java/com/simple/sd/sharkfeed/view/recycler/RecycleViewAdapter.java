package com.simple.sd.sharkfeed.view.recycler;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.simple.sd.sharkfeed.R;
import com.simple.sd.sharkfeed.data.DataManager;
import com.simple.sd.sharkfeed.data.modal.Item;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class RecycleViewAdapter extends RecyclerView.Adapter {

    private ArrayList<Item> items;

    public RecycleViewAdapter(ArrayList<Item> items) {
        this.items = items;
    }

    private final PublishSubject<Item> onClickSubject = PublishSubject.create();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_item, viewGroup, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ItemHolder holder = (ItemHolder) viewHolder;
        holder.gridImage.setImageDrawable(null);
        holder.progressBar.setVisibility(View.VISIBLE);
        Item item = items.get(i);

        DataManager.getInstance().getImage(item.getImageLowURL())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((obj) -> {
                    Bitmap bitmap = (Bitmap) obj;
                    holder.gridImage.setImageBitmap(bitmap);
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }, error -> Log.e("saideep",error.getMessage()));

        holder.gridImage.setOnClickListener(v -> onClickSubject.onNext(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public PublishSubject<Item> getItemClicks() {
        return onClickSubject;
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.grid_image)
        ImageView gridImage;
        @BindView(R.id.progress)
        ProgressBar progressBar;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
