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
public class DeezerPhoneDataHandler {

    private static final String EQUAL = " = ";
    private SQLiteDatabase mSQLiteDatabase;
    private DeezerPhoneDatabaseHelper mDeezerPhoneDatabaseHelper;
    private String[] allColumns = {
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_ID,
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_TITLE,
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_COVER,
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_ARTIST_ID,
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ARTIST_NAME,
            DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ARTIST_PICTURE
    };

    public DeezerPhoneDataHandler(Context context) {
        mDeezerPhoneDatabaseHelper = DeezerPhoneDatabaseHelper.getInstance(context);
    }

    public void open() throws SQLException {
        mSQLiteDatabase = mDeezerPhoneDatabaseHelper.getWritableDatabase();
    }

    public void close() {
        mDeezerPhoneDatabaseHelper.close();
    }

    public LikedAlbum createLikedAlbum(Album album) {
        ContentValues values = new ContentValues();
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_ID, album.getId());
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_TITLE, album.getTitle());
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_COVER, album.getSmallImageUrl());
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_ARTIST_ID, album.getArtist().getId());
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ARTIST_NAME, album.getArtist().getName());
        values.put(DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ARTIST_PICTURE, album.getArtist().getSmallImageUrl());
        long insertId = mSQLiteDatabase.insert(DeezerPhoneContract.UserLikedAlbumsPhone.TABLE_NAME, null, values);
        Cursor cursor = mSQLiteDatabase.query(DeezerPhoneContract.UserLikedAlbumsPhone.TABLE_NAME, allColumns, DeezerPhoneContract.UserLikedAlbumsPhone._ID
                + EQUAL + insertId, null, null, null, null);
        cursor.moveToFirst();
        LikedAlbum likedAlbum = cursorToLikeAlbum(cursor);
        cursor.close();
        return likedAlbum;
    }

    public void deleteLikedAlbum(Album album) {
        long id = album.getId();
        mSQLiteDatabase.delete(DeezerPhoneContract.UserLikedAlbumsPhone.TABLE_NAME, DeezerPhoneContract.UserLikedAlbumsPhone.COLUMN_NAME_ALBUM_ID + EQUAL + id, null);
    }

    public List<LikedAlbum> getAllLikedAlbums() {
        List<LikedAlbum> likedAlbumsList = new ArrayList<>();
        Cursor cursor = mSQLiteDatabase.query(DeezerPhoneContract.UserLikedAlbumsPhone.TABLE_NAME, allColumns, null, null, null, null, null);
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
        Cursor cursor = mSQLiteDatabase.query(DeezerPhoneContract.UserLikedAlbumsPhone.TABLE_NAME, allColumns, null, null, null, null, null);
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
