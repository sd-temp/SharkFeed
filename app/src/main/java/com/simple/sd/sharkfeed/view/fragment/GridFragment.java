package com.simple.sd.sharkfeed.view.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.simple.sd.sharkfeed.R;
import com.simple.sd.sharkfeed.data.DataManager;
import com.simple.sd.sharkfeed.data.modal.Item;
import com.simple.sd.sharkfeed.view.recycler.CustomLayoutManager;
import com.simple.sd.sharkfeed.view.recycler.EndlessRecyclerViewScrollListener;
import com.simple.sd.sharkfeed.view.recycler.RecycleViewAdapter;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nucleus5.factory.RequiresPresenter;
import nucleus5.view.NucleusSupportFragment;


@RequiresPresenter(GridPresenter.class)
public class GridFragment extends NucleusSupportFragment<GridPresenter> {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.image_root)
    RelativeLayout rootLayout;
    @BindView(R.id.image_HQ)
    ImageView largeImage;
    @BindView(R.id.go_back)
    ImageView backButton;
    @BindView(R.id.download_button)
    Button downLoadButton;
    @BindView(R.id.open_button)
    Button openInAppButton;
    @BindView(R.id.progress)
    ProgressBar progressBar;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.pull_to_refresh)
    SwipeRefreshLayout pullToRefresh;

    private ArrayList<Item> items;
    private EndlessRecyclerViewScrollListener listener;
    private RecycleViewAdapter adapter;
    private CustomLayoutManager layoutManager;
    private boolean isLargeImageShowing = false;
    private Item itemShowing;
    private Bitmap imageShowing;
    private boolean refreshData = false;

    public GridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getPresenter().getNextItems(1);
        items = new ArrayList<>();
        setUpRecyclerView();
        setUpViews();
    }

    private void setUpViews() {
        backButton.setOnClickListener(v -> allowBackPressed());
        downLoadButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            } else {
                getPresenter().downloadFile(imageShowing, itemShowing.getTitle());
            }
        });
        openInAppButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(itemShowing.getOriginalURL()));
            startActivity(i);
        });
        pullToRefresh.setOnRefreshListener(() -> {
            getPresenter().getNextItems(1);
            refreshData = true;
            DataManager.getInstance().refreshData();
        });
    }

    @SuppressLint("CheckResult")
    private void setUpRecyclerView() {
        layoutManager = new CustomLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter = new RecycleViewAdapter(items));
        recyclerView.addOnScrollListener(listener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getPresenter().getNextItems(page + 1);
            }
        });

        adapter.getItemClicks().subscribe(item -> {
            if (!isLargeImageShowing)
                loadFullImage(item);
        });
    }

    private void loadFullImage(Item item) {
        itemShowing = item;
        isLargeImageShowing = true;
        layoutManager.setScrollable(false);
        rootLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.INVISIBLE);
        description.setVisibility(View.INVISIBLE);
        downLoadButton.setVisibility(View.INVISIBLE);
        openInAppButton.setVisibility(View.INVISIBLE);

        getPresenter().getLargeImage(item.getImageHighURL());
        getPresenter().getInfo(item);
    }

    public void callError(Throwable error) {
        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    public void updateItems(Object o) {
        ArrayList<Item> items = (ArrayList<Item>) o;
        if (refreshData) {
            this.items.clear();
            refreshData = false;
            pullToRefresh.setRefreshing(false);
        }
        this.items.addAll(items);
        listener.resetState();
        adapter.notifyDataSetChanged();
    }

    public boolean allowBackPressed() {
        if (isLargeImageShowing) {
            isLargeImageShowing = false;
            layoutManager.setScrollable(true);
            largeImage.setImageDrawable(null);
            rootLayout.setVisibility(View.GONE);
            return false;
        }
        return true;

    }

    public void loadLargeImage(Object largeImage) {
        imageShowing = (Bitmap) largeImage;
        progressBar.setVisibility(View.INVISIBLE);
        downLoadButton.setVisibility(View.VISIBLE);
        this.largeImage.setImageBitmap(imageShowing);
    }

    public void updatedItem(Object updatedItem) {
        itemShowing = (Item) updatedItem;
        title.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
        title.setText(itemShowing.getTitle());
        description.setText(Html.fromHtml(itemShowing.getDescription()));
        openInAppButton.setVisibility(View.VISIBLE);
    }

    public void addImageToGallery(Intent mediaScanIntent) {
        Objects.requireNonNull(getActivity()).sendBroadcast(mediaScanIntent);
        callError(new Throwable("Download Complete! Find picture in camera roll"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPresenter().downloadFile(imageShowing, itemShowing.getTitle());
            } else {
                callError(new Throwable("Permission Denied"));
            }
        }
    }
}
