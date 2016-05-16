package com.neekoentertainment.deezerforandroidwear.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.neekoentertainment.deezerforandroidwear.MainActivity;
import com.neekoentertainment.deezerforandroidwear.models.LikedAlbum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 5/15/2016.
 */
public class DeezerWearDataHandler {

    private static final String EQUAL = " = ";
    private SQLiteDatabase mSQLiteDatabase;
    private DeezerWearDatabaseHelper mDeezerWearDatabaseHelper;
    private String[] allColumns = {
            DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_ID,
            DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_TITLE,
            DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_COVER,
            DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ARTIST_NAME
    };

    public DeezerWearDataHandler(Context context) {
        mDeezerWearDatabaseHelper = DeezerWearDatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        mSQLiteDatabase = mDeezerWearDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDeezerWearDatabaseHelper.close();
    }

    public LikedAlbum createLikedAlbum(JSONObject album) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_ID, album.getLong(MainActivity.JSONOBJECT_KEY_ID));
        values.put(DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_TITLE, album.getString(MainActivity.JSONOBJECT_KEY_TITLE));
        values.put(DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_COVER, Base64.decode(album.getString(MainActivity.JSONOBJECT_KEY_COVER), Base64.DEFAULT));
        values.put(DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ARTIST_NAME, album.getString(MainActivity.JSONOBJECT_KEY_ARTIST));
        long insertId = mSQLiteDatabase.insert(DeezerWearContract.UserLikedAlbumsWear.TABLE_NAME, null, values);
        Cursor cursor = mSQLiteDatabase.query(DeezerWearContract.UserLikedAlbumsWear.TABLE_NAME, allColumns, DeezerWearContract.UserLikedAlbumsWear._ID
                + EQUAL + insertId, null, null, null, null);
        cursor.moveToFirst();
        LikedAlbum likedAlbum = cursorToLikeAlbum(cursor);
        cursor.close();
        return likedAlbum;
    }

    public void deleteLikedAlbum(long id) {
        mSQLiteDatabase.delete(DeezerWearContract.UserLikedAlbumsWear.TABLE_NAME, DeezerWearContract.UserLikedAlbumsWear.COLUMN_NAME_ALBUM_ID + EQUAL + id, null);
    }

    public List<LikedAlbum> getAllLikedAlbums() {
        List<LikedAlbum> likedAlbumsList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(DeezerWearContract.UserLikedAlbumsWear.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LikedAlbum likedAlbum = cursorToLikeAlbum(cursor);
            likedAlbumsList.add(likedAlbum);
            cursor.moveToNext();
        }
        cursor.close();
        return likedAlbumsList;
    }

    public long getAmountLikedAlbums() {
        return DatabaseUtils.queryNumEntries(mSQLiteDatabase, DeezerWearContract.UserLikedAlbumsWear.TABLE_NAME);
    }

    private LikedAlbum cursorToLikeAlbum(Cursor cursor) {
        LikedAlbum likedAlbum = new LikedAlbum();
        likedAlbum.setId(cursor.getLong(0));
        likedAlbum.setAlbumTitle(cursor.getString(1));
        likedAlbum.setAlbumCover(cursor.getBlob(2));
        likedAlbum.setArtistName(cursor.getString(3));
        return likedAlbum;
    }
}
