package com.neekoentertainment.deezerforandroidwear.listener;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.neekoentertainment.deezerforandroidwear.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nicolas on 27/10/2015.
 */
public class ListenerService extends WearableListenerService {

    public static final String GET_PHONE_DATA_ACTION = "get_phone_data_action";
    public static final String DATA_ITEM_PATH = "/deezer_data";
    public static final String DEEZER_JSON_ARRAY = "data";
    public static final String DEEZER_JSON_ALBUM_SMALL_COVER = "cover_small";
    public static final String DEEZER_DISCONNECTED_MESSAGE = "deezer_disconnected";
    public static final String DEEZER_STATUS = "deezer_status";

    private GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = getGoogleApiClient(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(DEEZER_DISCONNECTED_MESSAGE)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(DEEZER_STATUS, DEEZER_DISCONNECTED_MESSAGE);
            intent.setAction(GET_PHONE_DATA_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals(DATA_ITEM_PATH)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = dataMapItem.getDataMap();
                Asset profileAsset = dataMap.getAsset(DEEZER_JSON_ARRAY);
                try {
                    JSONObject albumData = loadJsonFromAsset(profileAsset);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(DEEZER_JSON_ARRAY, albumData.toString());
                    intent.putExtra(DEEZER_JSON_ALBUM_SMALL_COVER, dataMap.getByteArray(DEEZER_JSON_ALBUM_SMALL_COVER));
                    intent.setAction(GET_PHONE_DATA_ACTION);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } catch (IOException | JSONException e) {
                    Log.e("ListenerService", e.getMessage());
                }
            }
        }
    }

    private InputStream getAssetInputStream(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();
        return assetInputStream;
    }

    public JSONObject loadJsonFromAsset(Asset asset) throws IOException, JSONException {
        InputStream assetInputStream = getAssetInputStream(asset);
        if (assetInputStream == null) {
            Log.e("LoadJson", "Requested an unknown Asset.");
            return null;
        }
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(assetInputStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JSONObject(responseStrBuilder.toString());
    }
}
