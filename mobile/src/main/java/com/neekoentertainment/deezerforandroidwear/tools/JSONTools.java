package com.neekoentertainment.deezerforandroidwear.tools;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nicolas on 4/12/2016.
 */
public class JSONTools {

    public static final String KEY_ID = "id";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_TITLE = "title";

    public static JSONObject generateAlbumJson(JSONObject fullData) throws JSONException {
        JSONObject selectedData = new JSONObject();
        selectedData.put(KEY_ID, fullData.getLong("id"));
        selectedData.put(KEY_ARTIST, fullData.getJSONObject("artist").getString("name"));
        selectedData.put(KEY_TITLE, fullData.getString("title"));
        return selectedData;
    }
}
