package com.neekoentertainment.deezerforandroidwear.tools;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nicolas on 4/12/2016.
 */
public class JSONTools {

    public static JSONObject generateAlbumJson(JSONObject fullData) throws JSONException {
        JSONObject selectedData = new JSONObject();
        selectedData.put("id", fullData.getLong("id"));
        selectedData.put("artist", fullData.getJSONObject("artist").getString("name"));
        selectedData.put("title", fullData.getString("title"));
        return selectedData;
    }
}
