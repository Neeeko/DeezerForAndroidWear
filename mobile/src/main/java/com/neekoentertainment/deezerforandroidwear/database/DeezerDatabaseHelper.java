package com.neekoentertainment.deezerforandroidwear.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Nicolas on 4/24/2016.
 */
public class DeezerDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DeezerDb.db";
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    private static final String PRIMARY_KEY = " PRIMARY KEY";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String PARENTHESIS_OPEN = "(";
    private static final String PARENTHESIS_CLOSE = ")";
    private static final String COMMA_SEP = ",";
    private static final String CREATE_TABLE_USER_LIKED_ALBUMS =
            CREATE_TABLE + DeezerContract.UserLikedAlbums.TABLE_NAME + PARENTHESIS_OPEN +
                    DeezerContract.UserLikedAlbums._ID + INT_TYPE + PRIMARY_KEY + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ID + INT_TYPE + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_TITLE + TEXT_TYPE + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_COVER_SMALL + TEXT_TYPE + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ALBUM_ARTIST_ID + INT_TYPE + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_NAME + TEXT_TYPE + COMMA_SEP +
                    DeezerContract.UserLikedAlbums.COLUMN_NAME_ARTIST_PICTURE_SMALL + TEXT_TYPE +
                    PARENTHESIS_CLOSE;
    private static final String DROP_TABLE_USER_LIKE_ALBUMS =
            DROP_TABLE + DeezerContract.UserLikedAlbums.TABLE_NAME;
    private static DeezerDatabaseHelper mInstance;

    public DeezerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DeezerDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DeezerDatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_LIKED_ALBUMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_USER_LIKE_ALBUMS);
    }
}
