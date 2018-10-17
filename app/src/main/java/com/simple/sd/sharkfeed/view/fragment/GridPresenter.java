package com.simple.sd.sharkfeed.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.simple.sd.sharkfeed.data.DataManager;
import com.simple.sd.sharkfeed.data.modal.Item;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import nucleus5.presenter.RxPresenter;

public class GridPresenter extends RxPresenter<GridFragment> {

    private static final int GET_NEXT_ITEMS = 100;
    private static final int GET_HQ_IMAGE = 200;
    private static final int GET_ITEM_INFO = 300;
    private static final int GET_IMAGE_FILE = 400;

    private static final String PAGENUM_KEY = "pageNumKey";
    private int pageNum = 0;
    private String imageURL;
    private Item infoItem;


    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (savedState != null) {
            pageNum = savedState.getInt(PAGENUM_KEY);
        }

        restartableLatestCache(GET_NEXT_ITEMS,
                () -> DataManager.getInstance().getNextItems(pageNum)
                        .observeOn(AndroidSchedulers.mainThread()),
                GridFragment::updateItems,
                GridFragment::callError);

        restartableLatestCache(GET_HQ_IMAGE,
                () -> DataManager.getInstance().getImage(imageURL)
                        .observeOn(AndroidSchedulers.mainThread()),
                GridFragment::loadLargeImage,
                GridFragment::callError);

        restartableLatestCache(GET_ITEM_INFO,
                () -> DataManager.getInstance().getInfo(infoItem)
                        .observeOn(AndroidSchedulers.mainThread()),
                GridFragment::updatedItem,
                GridFragment::callError);

    }

    @Override
    protected void onSave(Bundle state) {
        state.putInt(PAGENUM_KEY, pageNum);
        super.onSave(state);
    }

    public void getNextItems(int pageNum) {
        this.pageNum = pageNum;
        start(GET_NEXT_ITEMS);
    }

    public void getLargeImage(String imageHighURL) {
        this.imageURL = imageHighURL;
        start(GET_HQ_IMAGE);
    }

    public void getInfo(Item item) {
        this.infoItem = item;
        start(GET_ITEM_INFO);
    }

    public void downloadFile(Bitmap bitmap, String title) {
        restartableLatestCache(GET_IMAGE_FILE,
                () -> DataManager.getInstance().getImageFile(bitmap, title)
                        .observeOn(AndroidSchedulers.mainThread()),
                (gridFragment, path) -> {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(path);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    gridFragment.addImageToGallery(mediaScanIntent);
                },
                GridFragment::callError);
        start(GET_IMAGE_FILE);
    }
}
