package com.neekoentertainment.deezerforandroidwear.tools;

import android.content.Context;
import android.content.SharedPreferences;

import com.deezer.sdk.model.Album;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Nicolas on 4/21/2016.
 */
public class SharedPreferencesTools {

    public static final String PREFS_FILE = "deezer_data";
    public static final String PREFS_ALBUMS_LIST = "preferences_albums_list";

    public static void putAlbumsListToSharedPreferences(List<Album> albumList, Context context) {
        JSONArray jsonArray = new JSONArray(albumList);
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREFS_ALBUMS_LIST, jsonArray.toString());
        editor.apply();
    }

    public static String getAlbumsListToSharedPreferences(Context context) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREFS_ALBUMS_LIST, null);
    }
}
