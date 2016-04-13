package com.neekoentertainment.deezerforandroidwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableListView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.neekoentertainment.deezerforandroidwear.listener.ListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity {
    public static final String DEEZER_DATA_WATCH_REQUEST = "deezer_data_watch_request";
    private WearableListView mListView;
    private DataReceiver mDataReceiver;
    private MyAlbumAdapter myAlbumAdapter;
    private GoogleApiClient mGoogleApiClient;

    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        mDataReceiver = new DataReceiver();
        mDataReceiver.setListener(new DataReceiver.DataReceivedListener() {
            @Override
            public void onDataReceived(JSONObject jsonObject, Bitmap albumCover) {
                myAlbumAdapter.addAlbum(jsonObject);
                myAlbumAdapter.addCover(albumCover);
                myAlbumAdapter.notifyDataSetChanged();
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ListenerService.GET_PHONE_DATA_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mDataReceiver, intentFilter);
        Intent intent = new Intent(MainActivity.this, ListenerService.class);
        startService(intent);
    }

    private GoogleApiClient getGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("GoogleApiClient", "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("GoogleApiClient", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("GoogleApiClient", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    private void requestConnectedUserAlbums() {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), DEEZER_DATA_WATCH_REQUEST, null);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = getGoogleApiClient();
        requestConnectedUserAlbums();
        mListView = (WearableListView) findViewById(R.id.album_list);
        myAlbumAdapter = new MyAlbumAdapter(getApplicationContext());
        myAlbumAdapter.setHasStableIds(true);
        final ImageView header = (ImageView) findViewById(R.id.header);
        mListView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {

            }

            @Override
            public void onAbsoluteScrollChange(int i) {
                float newTranslation = Math.min(-i, 0);
                header.setTranslationY(newTranslation);
            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {

            }
        });
        mListView.setAdapter(myAlbumAdapter);
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
                        mDataReceivedListener.onDataReceived(new JSONObject(intent.getStringExtra(ListenerService.DEEZER_JSON_ARRAY)),
                                getBitmapFromByteArray(intent.getByteArrayExtra(ListenerService.DEEZER_JSON_ALBUM_SMALL_COVER)));
                    } catch (JSONException e) {
                        Log.e("DataReceiver", e.getMessage());
                    }
                }
            } else if (intent != null && intent.getStringExtra(ListenerService.DEEZER_STATUS) != null) {
                String deezerStatus = intent.getStringExtra(ListenerService.DEEZER_STATUS);
                switch (deezerStatus) {
                    case ListenerService.DEEZER_DISCONNECTED_MESSAGE:
                        Toast.makeText(context, "Please connect to Deezer from your phone.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }

        public interface DataReceivedListener {
            void onDataReceived(JSONObject jsonObject, Bitmap albumCover);
        }
    }

    public class MyAlbumAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;
        private List<JSONObject> mAlbumList;
        private List<Bitmap> mCoverList;

        public MyAlbumAdapter(Context context) {
            mAlbumList = new ArrayList<>();
            mCoverList = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
        }

        public void addAlbum(JSONObject album) {
            mAlbumList.add(album);
        }

        public void addCover(Bitmap bitmap) {
            mCoverList.add(bitmap);
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
                albumName.setMovementMethod(new ScrollingMovementMethod());
                albumName.setText(album.getString("title"));
                artistName.setMovementMethod(new ScrollingMovementMethod());
                artistName.setText(album.getString("artist"));
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

            public void setAlbumCover(Bitmap albumCover) {
                this.albumCover.setImageBitmap(albumCover);
            }
        }
    }
}
