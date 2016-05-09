package com.neekoentertainment.deezerforandroidwear;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.PaginatedList;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.RequestListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.neekoentertainment.deezerforandroidwear.database.DeezerDataHandler;
import com.neekoentertainment.deezerforandroidwear.listener.ListenerService;
import com.neekoentertainment.deezerforandroidwear.tools.AssetTools;
import com.neekoentertainment.deezerforandroidwear.tools.JSONTools;
import com.neekoentertainment.deezerforandroidwear.tools.ServicesAuthentication;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nicolas on 12/10/2015.
 */
public class DeezerDataHandlerActivity extends AppCompatActivity {

    public static final String DATA_ITEM_PATH = "/deezer_data";
    public static final String DEEZER_ALBUM_URL = "user/me/albums";
    public static final String DEEZER_JSON_ARRAY = "data";
    public static final String TIMESTAMP = "timestamp";
    public static final String DEEZER_JSON_ALBUM_SMALL_COVER = "cover_small";
    public static final String DEEZER_DISCONNECTED_MESSAGE = "deezer_disconnected";
    public static final String DEEZER_START_MESSAGE = "deezer_start";


    private GoogleApiClient mGoogleApiClient;

    private ImageLoader mImageLoader;

    private DeezerConnect mDeezerConnect;

    private DeezerDataHandler mDeezerDataHandler;
    private List<Album> mAlbumList;

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mDeezerDataHandler != null) {
            mDeezerDataHandler.close();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mDeezerDataHandler != null) {
            mDeezerDataHandler.open();
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(ListenerService.REQUESTED_CONTENT_EXTRA) != null) {
            String requestedContent = getIntent().getExtras().getString(ListenerService.REQUESTED_CONTENT_EXTRA);
            init(requestedContent);
        }
    }

    private void init(final String requestedContent) {
        mAlbumList = new ArrayList<>();
        mImageLoader = ImageLoader.getInstance();
        mDeezerDataHandler = new DeezerDataHandler(this);
        mDeezerDataHandler.open();
        mImageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));
        mGoogleApiClient = ServicesAuthentication.getGoogleApiClient(this);
        ServicesAuthentication.DeezerConnection mCallback = new ServicesAuthentication.DeezerConnection() {
            @Override
            public void onDeezerConnected(DeezerConnect deezerConnect) {
                mDeezerConnect = deezerConnect;
                switch (requestedContent) {
                    case ListenerService.DEEZER_GET_FAVORITED_ALBUMS:
                        getCurrentUserAlbums(DEEZER_ALBUM_URL, new OnDataRetrieved() {
                            @Override
                            public void onDataRetrieved() {
                                for (Album album : mAlbumList) {
                                    mDeezerDataHandler.createLikedAlbum(album);
                                }
                                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                    @Override
                                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                                        for (Node node : nodes.getNodes()) {
                                            Wearable.MessageApi.sendMessage(
                                                    mGoogleApiClient, node.getId(), DEEZER_START_MESSAGE + ":" + mAlbumList.size(), null);
                                        }
                                    }
                                });
                                retrieveDeviceNode();
                            }
                        });
                        break;
                    case ListenerService.DEEZER_UPDATE_FAVORITED_ALBUMS:
                        updateCurrentUserAlbums();
                        break;
                }
            }

            @Override
            public void onDeezerDisconnected() {
                // Notifies the watch that the user is disconnected from Deezer
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            Wearable.MessageApi.sendMessage(
                                    mGoogleApiClient, node.getId(), DEEZER_DISCONNECTED_MESSAGE, null);
                        }
                    }
                });
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onDeezerFailed(String e) {
                Log.e("DataHandler", e);
            }
        };
        ServicesAuthentication.getDeezerConnect(this, mCallback);
    }

    private void getCurrentUserAlbums(String url, final OnDataRetrieved onDataRetrieved) {
        RequestListener requestListener = new RequestListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onComplete(String result, Object o) {
                try {
                    PaginatedList<Album> userAlbums;
                    userAlbums = (PaginatedList<Album>) JsonUtils.deserializeJson(result);
                    for (Album album : userAlbums) {
                        mAlbumList.add(album);
                    }
                    if (userAlbums.getNextUrl() != null && !userAlbums.getNextUrl().isEmpty()) {
                        getCurrentUserAlbums(userAlbums.getNextUrl(), onDataRetrieved);
                    } else {
                        onDataRetrieved.onDataRetrieved();
                    }
                } catch (JSONException e) {
                    Log.e("DeezerConnect", e.getMessage());
                }
            }

            public void onException(Exception e, Object requestId) {
            }
        };
        DeezerRequest request = new DeezerRequest(url);
        request.setId("getAlbums");
        mDeezerConnect.requestAsync(request, requestListener);
    }

    private void updateCurrentUserAlbums() {
        getCurrentUserAlbums(DEEZER_ALBUM_URL, new OnDataRetrieved() {
            @Override
            public void onDataRetrieved() {
                List<Long> deezerIdList = new ArrayList<>();
                for (Album album : mAlbumList) {
                    deezerIdList.add(album.getId());
                }
                Collections.sort(deezerIdList);
                List<Long> likedAlbumsList = mDeezerDataHandler.getAllLikedAlbumsId();

                //List<Long> tmpList = likedAlbumsList;
                //tmpList.removeAll(deezerIdList);
                Log.d("Test", "DEEZER ID ===== size = " + deezerIdList.size());
                for (Long l : deezerIdList) {
                    Log.d("Test", "val = " + l);
                }
                Log.d("Test", "BDD ID ===== size = " + likedAlbumsList.size());
                for (Long l2 : likedAlbumsList) {
                    Log.d("Test", "vala = " + l2);
                }
                Log.d("Test", "DEEZER ID ADD =====");
                /*for (Long l : tmpList) {
                    Log.d("Test", "val = " + l);
                }*/
            }
        });
    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        try {
                            getUserAlbumsCovers(mAlbumList);
                        } catch (JSONException e) {
                            Log.e("RetrieveDeviceNode", e.getMessage());
                        }
                    }
                });
            }
        }).start();
    }

    private void getUserAlbumsCovers(List<Album> albums) throws JSONException {
        if (mGoogleApiClient.isConnected()) {
            Collections.sort(albums, new Comparator<Album>() {
                @Override
                public int compare(Album album1, Album album2) {
                    return album1.getArtist().getName().toLowerCase().compareTo(album2.getArtist().getName().toLowerCase());
                }
            });
            // TODO: a rm
            //SharedPreferencesTools.putAlbumsListToSharedPreferences(albums, this.getApplicationContext());
            final PutDataMapRequest dataMapRequest = PutDataMapRequest.create(DATA_ITEM_PATH);
            for (final Album album : albums) {
                AssetTools.OnBitmapLoaded onBitmapLoaded = new AssetTools.OnBitmapLoaded() {
                    @Override
                    public void onBitmapLoaded(Asset asset) {
                        try {
                            DataMap dataMap = dataMapRequest.getDataMap();
                            dataMap.putAsset(DEEZER_JSON_ARRAY, AssetTools.getAssetFromJsonObject(JSONTools.generateAlbumJson(album.toJson())));
                            dataMap.putLong(TIMESTAMP, System.currentTimeMillis());
                            dataMap.putAsset(DEEZER_JSON_ALBUM_SMALL_COVER, asset);
                            PutDataRequest request = dataMapRequest.asPutDataRequest();
                            request.setUrgent();
                            Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                        } catch (JSONException e) {
                            Log.e("onBitmapLoaded", e.getMessage());
                        }
                    }
                };
                AssetTools.getBitmapAssetFromUrl(album.getSmallImageUrl(), onBitmapLoaded, mImageLoader);
            }
        } else {
            Log.e("GoogleApiClient", "No connection to a wearable available.");
        }
    }

    public interface OnDataRetrieved {
        void onDataRetrieved();
    }
}