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
import android.util.Base64;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends WearableActivity {
    public static final String DEEZER_DATA_WATCH_REQUEST = "deezer_data_watch_request";
    public static final String ALBUM_DATA_FILENAME = "/album_data.dd";
    public static final String ALBUM_COVER_FILENAME = "/album_cover.dd";
    private WearableListView mListView;
    private DataReceiver mDataReceiver;
    private AlbumAdapter mAlbumAdapter;
    private GoogleApiClient mGoogleApiClient;
    private int mAlbumsAmount;
    private int mReceivedAlbumsAmount;

    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        if (!areDataFileUpToDate()) {
            mDataReceiver = new DataReceiver();
            mDataReceiver.setListener(new DataReceiver.DataReceivedListener() {
                @Override
                public void onAlbumDataReceived(JSONObject jsonObject, Bitmap albumCover) {
                    mAlbumAdapter.addAlbum(jsonObject, albumCover);
                    mAlbumAdapter.notifyDataSetChanged();
                    mReceivedAlbumsAmount++;
                    Log.d("Test", "mReceivedAlbumsAmount = " + mReceivedAlbumsAmount);
                    if (mReceivedAlbumsAmount == mAlbumsAmount) {
                        saveAlbumToFile(ALBUM_DATA_FILENAME, mAlbumAdapter.getAlbumsJsonArray());
                        saveAlbumToFile(ALBUM_COVER_FILENAME, mAlbumAdapter.getAlbumsCoverJsonArray());
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
        } else {
            JSONArray albums = getAlbumsFromFile(ALBUM_DATA_FILENAME);
            JSONArray covers = getAlbumsFromFile(ALBUM_COVER_FILENAME);
            for (int i = 0; i < albums.length(); i++) {
                try {
                    byte[] byteArray = Base64.decode(covers.getString(i), Base64.DEFAULT);
                    Log.d("Test", "salut  = " + Arrays.toString(byteArray));
                    Bitmap albumCover = getBitmapFromByteArray(byteArray);
                    if (albumCover != null)
                        Log.d("Test", "album not null");
                    else
                        Log.d("Test", "album null fuck");
                    mAlbumAdapter.addAlbum(albums.getJSONObject(i), albumCover);
                    mAlbumAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("MainActivity", e.getMessage());
                }
            }
        }
    }

    private JSONArray getAlbumsFromFile(String fileName) {
        JSONArray albums = new JSONArray();
        File albumData = new File(getApplicationContext().getExternalCacheDir(), fileName);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(albumData));
            String line = bufferedReader.readLine(); // Only one line
            albums = new JSONArray(line);
        } catch (IOException | JSONException e) {
            Log.e("MainActivity", e.getMessage());
        }
        return albums;
    }

    private boolean areDataFileUpToDate() {
        File albumData = new File(getApplicationContext().getExternalCacheDir(), ALBUM_DATA_FILENAME);
        File coverData = new File(getApplicationContext().getExternalCacheDir(), ALBUM_DATA_FILENAME);
        if (albumData.length() > 0 && coverData.length() > 0) {
            Log.d("Test", "both files are existing and with data");
            return true;
        } else {
            Log.d("Test", "files doesn't exist or are not full");
            return false;
        }
    }

    private void saveAlbumToFile(String fileName, JSONArray album) {
        try {
            File file = new File(getApplicationContext().getExternalCacheDir(), fileName);
            BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, true));
            outputStream.write(album.toString());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            Log.e("MainActivity", e.getMessage());
        }
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
        mAlbumAdapter = new AlbumAdapter(getApplicationContext());
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
        mListView.setAdapter(mAlbumAdapter);
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

        private Context mContext;

        public void setListener(DataReceivedListener dataReceivedListener) {
            mDataReceivedListener = dataReceivedListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext = context;
            if (intent != null && intent.getStringExtra(ListenerService.DEEZER_JSON_ARRAY) != null && intent.getByteArrayExtra(ListenerService.DEEZER_JSON_ALBUM_SMALL_COVER) != null) {
                if (mDataReceivedListener != null) {
                    try {
                        JSONObject album = new JSONObject(intent.getStringExtra(ListenerService.DEEZER_JSON_ARRAY));
                        //saveAlbumToFile(album);
                        byte[] albumCover = intent.getByteArrayExtra(ListenerService.DEEZER_JSON_ALBUM_SMALL_COVER);
                        //saveAlbumCoverToFile(album.getLong("id"), albumCover);
                        mDataReceivedListener.onAlbumDataReceived(album, getBitmapFromByteArray(albumCover));
                    } catch (JSONException /*| IOException */e) {
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

        private void saveAlbumCoverToFile(long id, byte[] albumCover) throws IOException {
            File file = new File(mContext.getExternalCacheDir(), id + ".dd");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(albumCover);
            fileOutputStream.close();
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
        private JSONArray mAlbumsJsonArray;
        private JSONArray mAlbumsCoverJsonArray;

        public AlbumAdapter(Context context) {
            mAlbumList = new ArrayList<>();
            mCoverList = new ArrayList<>();
            mAlbumsJsonArray = new JSONArray();
            mAlbumsCoverJsonArray = new JSONArray();
            mInflater = LayoutInflater.from(context);
        }

        public void addAlbum(JSONObject album, Bitmap albumCover) {
            int index = getInsertIndex(album);
            mAlbumList.add(index, album);
            mCoverList.add(index, albumCover);
            try {
                mAlbumsJsonArray.put(index, album);
                mAlbumsCoverJsonArray.put(index, Base64.encodeToString(getByteArrayFromBitmap(albumCover), Base64.DEFAULT));
            } catch (JSONException | IOException e) {
                Log.e("AlbumAdapter", e.getMessage());
            }
        }

        public JSONArray getAlbumsCoverJsonArray() {
            return mAlbumsCoverJsonArray;
        }

        public JSONArray getAlbumsJsonArray() {
            return mAlbumsJsonArray;
        }

        private int getInsertIndex(JSONObject newAlbum) {
            int index = Collections.binarySearch(mAlbumList, newAlbum, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject album1, JSONObject album2) {
                    try {
                        return album1.getString("title").toLowerCase().compareTo(album2.getString("title").toLowerCase());
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

        private byte[] getByteArrayFromBitmap(Bitmap bitmap) throws IOException {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            stream.close();
            return byteArray;
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
        }
    }
}
