package com.simple.sd.sharkfeed.data;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.simple.sd.sharkfeed.data.modal.Item;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataManager {
    private static DataManager dataManager = null;
    private OkHttpClient client;

    private int lastPageCalled = 0;

    private DataManager() {
        client = new OkHttpClient();
        //Singleton
    }

    public static DataManager getInstance() {
        return dataManager != null ? dataManager : (dataManager = new DataManager());
    }


    public Observable<Object> getNextItems(int pageNum) {
        return Observable.create(emitter -> {
            if (pageNum > lastPageCalled) {
                Request request = new Request.Builder()
                        .url("https://api.flickr.com/services/rest/?method=flickr.photos.search" +
                                "&api_key=949e98778755d1982f537d56236bbb42" +
                                "&text=shark&format=json" +
                                "&nojsoncallback=1&page=" + pageNum + "&extras=url_t,url_l,url_o")
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        emitter.onError(e);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            emitter.onError(new Throwable("Unexpected Error"));
                        } else {
                            ArrayList<Item> items = new ArrayList<>();
                            try {
                                assert response.body() != null;
                                JSONObject root = new JSONObject(response.body().string());
                                JSONArray photos = root.getJSONObject("photos").getJSONArray("photo");
                                for (int i = 0; i < photos.length(); i++) {
                                    Item item = new Item();
                                    JSONObject pic = photos.getJSONObject(i);

                                    item.setTitle(pic.getString("title"));
                                    item.setItemId(Long.parseLong(pic.getString("id")));
                                    if (pic.has("url_l"))
                                        item.setImageLowURL(pic.getString("url_l"));
                                    else item.setImageLowURL(pic.getString("url_t"));

                                    if (pic.has("url_o"))
                                        item.setImageHighURL(pic.getString("url_o"));
                                    else if (pic.has("url_l"))
                                        item.setImageHighURL(pic.getString("url_l"));
                                    else item.setImageHighURL(pic.getString("url_t"));

                                    items.add(item);
                                }
                                emitter.onNext(items);
                            } catch (JSONException e) {
                                emitter.onError(e);
                            }
                        }
                    }
                });
                lastPageCalled = pageNum;
            } else {
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Object> getImage(String imageLowURL) {
        return Observable.create(emitter -> {
            emitter.onNext(Picasso.get()
                    .load(imageLowURL)
                    .get());
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Object> getInfo(Item infoItem) {
        return Observable.create(emitter -> {
            Request request = new Request.Builder()
                    .url("https://api.flickr.com/services/rest/?method=flickr.photos.getInfo" +
                            "&api_key=949e98778755d1982f537d56236bbb42" +
                            "&photo_id=" + infoItem.getItemId() +
                            "&format=json&nojsoncallback=1")
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    emitter.onError(e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        emitter.onError(new Throwable("Unexpected Error"));
                    } else {
                        try {
                            assert response.body() != null;
                            JSONObject root = new JSONObject(response.body().string());
                            JSONObject photo = root.getJSONObject("photo");
                            JSONObject description = photo.getJSONObject("description");
                            if (description.has("_content") && !description.getString("_content").equals("")) {
                                infoItem.setDescription(description.getString("_content"));
                            }
                            JSONArray url = photo.getJSONObject("urls").getJSONArray("url");
                            for (int i = 0; i < url.length(); i++) {
                                JSONObject j = url.getJSONObject(i);
                                if (j.getString("type").equals("photopage")) {
                                    infoItem.setOriginalURL(j.getString("_content"));
                                }
                            }
                            emitter.onNext(infoItem);
                        } catch (JSONException e) {
                            emitter.onError(e);
                        }
                    }
                }
            });
        });
    }

    public Observable<String> getImageFile(Bitmap bitmap, String title) {
        return Observable.create(emitter -> {
            try {
                File storageDir = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/");
                if (!storageDir.exists())
                    storageDir.mkdirs();
                File file = File.createTempFile(
                        title,
                        ".jpeg",
                        storageDir);
                FileOutputStream ostream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                ostream.close();
                emitter.onNext(file.getAbsolutePath());
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    public void refreshData() {
        lastPageCalled = 0;
    }
}
