package com.neekoentertainment.deezerforandroidwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.neekoentertainment.deezerforandroidwear.database.DeezerWearDataHandler;
import com.neekoentertainment.deezerforandroidwear.listener.ListenerService;
import com.neekoentertainment.deezerforandroidwear.models.LikedAlbum;
import com.neekoentertainment.deezerforandroidwear.tools.BitmapTools;
import com.neekoentertainment.deezerforandroidwear.tools.GoogleApiTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends WearableActivity {
    public static final String DEEZER_DATA_WATCH_REQUEST = "deezer_data_watch_request";
    public static final String DEEZER_DATA_WATCH_UPDATE = "deezer_data_watch_update";
    public static final String JSONOBJECT_KEY_ID = "id";
    public static final String JSONOBJECT_KEY_ARTIST = "artist";
    public static final String JSONOBJECT_KEY_TITLE = "title";
    public static final String JSONOBJECT_KEY_COVER = "cover";
    private WearableListView mListView;
    private DataReceiver mDataReceiver;
    private AlbumAdapter mAlbumAdapter;
    private GoogleApiClient mGoogleApiClient;
    private int mAlbumsAmount;
    private int mReceivedAlbumsAmount = 1;
    private boolean alreadySaved = false;
    private DeezerWearDataHandler mDeezerWearDataHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDeezerWearDataHandler = new DeezerWearDataHandler(this);
        mDeezerWearDataHandler.open();
        mGoogleApiClient = GoogleApiTools.getGoogleApiClient(this);
        requestConnectedUserAlbums();
        mListView = (WearableListView) findViewById(R.id.album_list);
        mAlbumAdapter = new AlbumAdapter(getApplicationContext());
        final ImageView header = (ImageView) findViewById(R.id.header);
        mListView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {
                header.setY(header.getY() - i);
            }

            @Override
            public void onAbsoluteScrollChange(int i) {

            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {
                mListView.getChildAt(i).setSelected(true);
            }
        });
        mListView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                v.setSelected(true);
            }
        });
        mListView.setAdapter(mAlbumAdapter);
        if (areDataFileUpToDate()) {
            retrieveDataFromDatabase();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final List<JSONObject> mAlbumsList = new ArrayList<>();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        if (!alreadySaved) {
            mDataReceiver = new DataReceiver();
            mDataReceiver.setListener(new DataReceiver.DataReceivedListener() {
                @Override
                public void onAlbumDataReceived(JSONObject jsonObject, Bitmap albumCover) {
                    mAlbumAdapter.addAlbum(jsonObject, albumCover);
                    mAlbumAdapter.notifyDataSetChanged();
                    mReceivedAlbumsAmount++;
                    try {
                        jsonObject.put(JSONOBJECT_KEY_COVER, Base64.encodeToString(BitmapTools.getByteArrayFromBitmap(albumCover), Base64.DEFAULT));
                        mAlbumsList.add(jsonObject);
                    } catch (IOException | JSONException e) {
                        Log.e("AlbumAdapter", e.getMessage());
                    }
                    if (mReceivedAlbumsAmount == mAlbumsAmount) {
                        saveAlbumsToDatabase(mAlbumsList);
                    }
                }

                @Override
                public void onStartDataReceived(int albumsAmount) {
                    mAlbumsAmount = albumsAmount;
                }
            });
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ListenerService.GET_PHONE_DATA_ACTION);
            LocalBroadcastManager.getInstance(this).registerReceiver(mDataReceiver, intentFilter);
            Intent intent = new Intent(MainActivity.this, ListenerService.class);
            startService(intent);
        }
    }

    @Override
    protected void onPause() {
        if (mDeezerWearDataHandler != null) {
            mDeezerWearDataHandler.close();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mDeezerWearDataHandler != null) {
            mDeezerWearDataHandler.open();
        }
        super.onResume();
    }

    private boolean areDataFileUpToDate() {
        long amountLikedAlbums = mDeezerWearDataHandler.getAmountLikedAlbums();
        return amountLikedAlbums > 0;
    }

    private void saveAlbumsToDatabase(List<JSONObject> albums) {
        for (JSONObject album : albums) {
            try {
                mDeezerWearDataHandler.createLikedAlbum(album);
            } catch (JSONException e) {
                Log.e("MainActivity", e.getMessage());
            }
        }
    }

    private void requestConnectedUserAlbums() {
        final String message;
        if (areDataFileUpToDate()) {
            message = DEEZER_DATA_WATCH_UPDATE;
        } else {
            message = DEEZER_DATA_WATCH_REQUEST;
        }
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), message, null);
                }
            }
        });
    }

    private void retrieveDataFromDatabase() {
        alreadySaved = true;
        List<LikedAlbum> likedAlbumsList = mDeezerWearDataHandler.getAllLikedAlbums();
        for (LikedAlbum likedAlbum : likedAlbumsList) {
            try {
                Bitmap albumCover = BitmapTools.getBitmapFromByteArray(likedAlbum.getAlbumCover());
                mAlbumAdapter.addAlbum(likedAlbum.toJSONObject(), albumCover);
                mAlbumAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("MainActivity", e.getMessage());
            }
        }
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataReceiver);
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public static class DataReceiver extends BroadcastReceiver {
        private DataReceivedListener mDataReceivedListener;

        public void setListener(DataReceivedListener dataReceivedListener) {
            mDataReceivedListener = dataReceivedListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getStringExtra(ListenerService.DEEZER_JSON_ARRAY) != null && intent.getByteArrayExtra(ListenerService.DEEZER_JSON_ALBUM_SMALL_COVER) != null) {
                if (mDataReceivedListener != null) {
                    try {
                        JSONObject album = new JSONObject(intent.getStringExtra(ListenerService.DEEZER_JSON_ARRAY));
                        byte[] albumCover = intent.getByteArrayExtra(ListenerService.DEEZER_JSON_ALBUM_SMALL_COVER);
                        mDataReceivedListener.onAlbumDataReceived(album, BitmapTools.getBitmapFromByteArray(albumCover));
                    } catch (JSONException e) {
                        Log.e("DataReceiver", e.getMessage());
                    }
                }
            } else if (intent != null && intent.getStringExtra(ListenerService.DEEZER_STATUS) != null) {
                String deezerStatus = intent.getStringExtra(ListenerService.DEEZER_STATUS);
                switch (deezerStatus) {
                    case ListenerService.DEEZER_START_MESSAGE:
                        if (mDataReceivedListener != null && intent.getIntExtra(ListenerService.DEEZER_JSON_ARRAY_SIZE, -1) != -1) {
                            mDataReceivedListener.onStartDataReceived(intent.getIntExtra(ListenerService.DEEZER_JSON_ARRAY_SIZE, -1));
                        }
                        break;
                    case ListenerService.DEEZER_DISCONNECTED_MESSAGE:
                        Toast.makeText(context, "Please connect to Deezer from your phone.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }

        public interface DataReceivedListener {
            void onAlbumDataReceived(JSONObject jsonObject, Bitmap albumCover);

            void onStartDataReceived(int albumsAmount);
        }
    }

    public class AlbumAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;
        private List<JSONObject> mAlbumList;
        private List<Bitmap> mCoverList;

        public AlbumAdapter(Context context) {
            mAlbumList = new ArrayList<>();
            mCoverList = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
        }

        public void addAlbum(JSONObject album, Bitmap albumCover) {
            int index = getInsertIndex(album);
            mAlbumList.add(index, album);
            mCoverList.add(index, albumCover);
        }

        private int getInsertIndex(JSONObject newAlbum) {
            int index = Collections.binarySearch(mAlbumList, newAlbum, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject album1, JSONObject album2) {
                    try {
                        return album1.getString(JSONOBJECT_KEY_ARTIST).toLowerCase().compareTo(album2.getString(JSONOBJECT_KEY_ARTIST).toLowerCase());
                    } catch (JSONException e) {
                        Log.e("AlbumAdapter", e.getMessage());
                    }
                    return 0;
                }
            });
            if (index < 0) {
                index = (index * -1) - 1;
            }
            return index;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(mInflater.inflate(R.layout.album_view, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView artistName = itemHolder.artistName;
            TextView albumName = itemHolder.albumName;
            ImageView albumCover = itemHolder.albumCover;
            JSONObject album;
            try {
                album = mAlbumList.get(position);
                albumName.setText(album.getString(JSONOBJECT_KEY_TITLE));
                artistName.setText(album.getString(JSONOBJECT_KEY_ARTIST));
                albumCover.setImageBitmap(mCoverList.get(position));
            } catch (JSONException e) {
                Log.e("AlbumAdapter", e.getMessage());
            }
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mAlbumList.size();
        }

        public class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView albumName;
            private TextView artistName;
            private ImageView albumCover;

            public ItemViewHolder(View itemView) {
                super(itemView);
                albumName = (TextView) itemView.findViewById(R.id.album_name);
                artistName = (TextView) itemView.findViewById(R.id.artist_name);
                albumCover = (ImageView) itemView.findViewById(R.id.album_cover);
            }
        }
    }
}
