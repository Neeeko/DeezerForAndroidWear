package com.neekoentertainment.deezerforandroidwear.models;

import com.neekoentertainment.deezerforandroidwear.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nicolas on 5/15/2016.
 */
public class LikedAlbum {

    private long mId;
    private String mAlbumTitle;
    private String mArtistName;
    private byte[] mAlbumCover;

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public void setAlbumTitle(String mAlbumTitle) {
        this.mAlbumTitle = mAlbumTitle;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public byte[] getAlbumCover() {
        return mAlbumCover;
    }

    public void setAlbumCover(byte[] mAlbumCover) {
        this.mAlbumCover = mAlbumCover;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MainActivity.JSONOBJECT_KEY_ID, mId);
        jsonObject.put(MainActivity.JSONOBJECT_KEY_TITLE, mAlbumTitle);
        jsonObject.put(MainActivity.JSONOBJECT_KEY_ARTIST, mArtistName);
        return jsonObject;
    }
}
