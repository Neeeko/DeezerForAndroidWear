package com.neekoentertainment.deezerforandroidwear.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.deezer.sdk.model.Album;
import com.neekoentertainment.deezerforandroidwear.models.LikedAlbum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nicolas on 4/24/2016.
 */
public class DeezerDataHandler {

    private static final String EQUAL = " = ";
    private SQLiteDatabase mSQLiteDatabase;
    private DeezerDatabaseHelper mDeezerDatabaseHelper;
    private String[] allColumns = {
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ID,
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_TITLE,
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_COVER_SMALL,
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ARTIST_ID,
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_NAME,
            DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_PICTURE_SMALL
    };

    public DeezerDataHandler(Context context) {
        mDeezerDatabaseHelper = DeezerDatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        mSQLiteDatabase = mDeezerDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDeezerDatabaseHelper.close();
    }

    public LikedAlbum createLikedAlbum(Album album) {
        ContentValues values = new ContentValues();
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ID, album.getId());
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_TITLE, album.getTitle());
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_COVER_SMALL, album.getSmallImageUrl());
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ARTIST_ID, album.getArtist().getId());
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_NAME, album.getArtist().getName());
        values.put(DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_PICTURE_SMALL, album.getArtist().getSmallImageUrl());
        long insertId = mSQLiteDatabase.insert(DeezerContract.UserLikedAlbums.TABLE_NAME, null, values);
        Cursor cursor = mSQLiteDatabase.query(DeezerContract.UserLikedAlbums.TABLE_NAME, allColumns, DeezerContract.UserLikedAlbums._ID
                + EQUAL + insertId, null, null, null, null);
        cursor.moveToFirst();
        LikedAlbum likedAlbum = cursorToLikeAlbum(cursor);
        cursor.close();
        return likedAlbum;
    }

    public void deleteLikedAlbum(Album album) {
        long id = album.getId();
        mSQLiteDatabase.delete(DeezerContract.UserLikedAlbums.TABLE_NAME, DeezerContract.UserLikedAlbums._ID + EQUAL + id, null);
    }

    public List<LikedAlbum> getAllLikedAlbums() {
        List<LikedAlbum> likedAlbumsList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(DeezerContract.UserLikedAlbums.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            LikedAlbum likedAlbum = cursorToLikeAlbum(cursor);
            likedAlbumsList.add(likedAlbum);
            cursor.moveToNext();
        }
        cursor.close();
        return likedAlbumsList;
    }

    public List<Long> getAllLikedAlbumsId() {
        List<Long> likedAlbumsIdList = new ArrayList<>();
        String[] idColumn = {DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ID};
        Cursor cursor = mSQLiteDatabase.query(DeezerContract.UserLikedAlbums.TABLE_NAME, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Long likedAlbumId = cursor.getLong(0);
            likedAlbumsIdList.add(likedAlbumId);
            cursor.moveToNext();
        }
        cursor.close();
        return likedAlbumsIdList;
    }

    private LikedAlbum cursorToLikeAlbum(Cursor cursor) {
        LikedAlbum likedAlbum = new LikedAlbum();
        likedAlbum.setId(cursor.getLong(0));
        likedAlbum.setAlbumTitle(cursor.getString(1));
        likedAlbum.setCoverSmall(cursor.getString(2));
        likedAlbum.setArtistId(cursor.getLong(3));
        likedAlbum.setArtistName(cursor.getString(4));
        likedAlbum.setArtistPictureSmall(cursor.getString(5));
        return likedAlbum;
    }
}
