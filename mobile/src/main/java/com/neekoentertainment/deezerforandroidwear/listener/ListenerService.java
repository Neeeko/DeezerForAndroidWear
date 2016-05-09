package com.neekoentertainment.deezerforandroidwear.listener;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.neekoentertainment.deezerforandroidwear.DeezerDataHandlerActivity;

/**
 * Created by Nicolas on 27/10/2015.
 */
public class ListenerService extends WearableListenerService {

    public static final String REQUESTED_CONTENT_EXTRA = "deezer_connected_intent";
    public static final String DEEZER_DATA_WATCH_REQUEST = "deezer_data_watch_request";
    public static final String DEEZER_DATA_WATCH_UPDATE = "deezer_data_watch_update";
    public static final String DEEZER_GET_FAVORITED_ALBUMS = "get_favorited_albums";
    public static final String DEEZER_UPDATE_FAVORITED_ALBUMS = "update_favorited_albums";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(DEEZER_DATA_WATCH_REQUEST)) {
            Intent startIntent = new Intent(this, DeezerDataHandlerActivity.class);
            startIntent.putExtra(REQUESTED_CONTENT_EXTRA, DEEZER_GET_FAVORITED_ALBUMS);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        } else if (messageEvent.getPath().equals(DEEZER_DATA_WATCH_UPDATE)) {
            Intent startIntent = new Intent(this, DeezerDataHandlerActivity.class);
            startIntent.putExtra(REQUESTED_CONTENT_EXTRA, DEEZER_UPDATE_FAVORITED_ALBUMS);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }
}
