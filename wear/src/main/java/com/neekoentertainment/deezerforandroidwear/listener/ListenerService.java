package com.neekoentertainment.deezerforandroidwear.listener;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.neekoentertainment.deezerforandroidwear.MainActivity;
import com.neekoentertainment.deezerforandroidwear.tools.AssetTools;
import com.neekoentertainment.deezerforandroidwear.tools.BitmapTools;
import com.neekoentertainment.deezerforandroidwear.tools.GoogleApiTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nicolas on 27/10/2015.
 */
public class ListenerService extends WearableListenerService {

    public static final String GET_PHONE_DATA_ACTION = "get_phone_data_action";
    public static final String DATA_ITEM_PATH = "/deezer_data";
    public static final String DEEZER_JSON_ARRAY = "data";
    public static final String DEEZER_JSON_ARRAY_SIZE = "data_size";
    public static final String DEEZER_JSON_ALBUM_SMALL_COVER = "cover_small";
    public static final String DEEZER_DISCONNECTED_MESSAGE = "deezer_disconnected";
    public static final String DEEZER_START_MESSAGE = "deezer_start";
    public static final String DEEZER_STATUS = "deezer_status";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = GoogleApiTools.getGoogleApiClient(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String messageEventPath = messageEvent.getPath();
        if (messageEventPath.contains(DEEZER_START_MESSAGE)) {
            String[] splitMessage = messageEventPath.split(":");
            int numberOfAlbums = Integer.valueOf(splitMessage[1]);
            Log.d("ListenerService", "Preparing to receive " + numberOfAlbums + " albums.");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(DEEZER_STATUS, DEEZER_START_MESSAGE);
            intent.putExtra(DEEZER_JSON_ARRAY_SIZE, numberOfAlbums);
            intent.setAction(GET_PHONE_DATA_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if (messageEventPath.equals(DEEZER_DISCONNECTED_MESSAGE)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(DEEZER_STATUS, DEEZER_DISCONNECTED_MESSAGE);
            intent.setAction(GET_PHONE_DATA_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                switch (event.getDataItem().getUri().getPath()) {
                    case DATA_ITEM_PATH:
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        DataMap dataMap = dataMapItem.getDataMap();
                        Asset profileAsset = dataMap.getAsset(DEEZER_JSON_ARRAY);
                        Asset bitmapAsset = dataMap.getAsset(DEEZER_JSON_ALBUM_SMALL_COVER);
                        try {
                            JSONObject albumData = AssetTools.loadJsonFromAsset(profileAsset, mGoogleApiClient);
                            if (albumData != null) {
                                Intent intent = new Intent(this, MainActivity.class);
                                intent.putExtra(DEEZER_JSON_ARRAY, albumData.toString());
                                Bitmap albumCover = BitmapTools.loadBitmapFromAsset(bitmapAsset, mGoogleApiClient);
                                intent.putExtra(DEEZER_JSON_ALBUM_SMALL_COVER, BitmapTools.getByteArrayFromBitmap(albumCover));
                                intent.setAction(GET_PHONE_DATA_ACTION);
                                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                            }
                        } catch (IOException | JSONException e) {
                            Log.e("ListenerService", e.getMessage());
                        }
                        break;
                }
            }
        }
    }
}
