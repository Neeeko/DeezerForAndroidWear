package com.neekoentertainment.deezerforandroidwear.database;

import android.provider.BaseColumns;

/**
 * Created by Nicolas on 4/24/2016.
 */
public class DeezerContract {

    public DeezerContract() {
    }

    public static abstract class UserLikedAlbums implements BaseColumns {
        public static final String TABLE_NAME = "userlikedalbums";
        public static final String COLUMN_NAME_ALBUM_ID = "id";
        public static final String COLUMN_NAME_ALBUM_TITLE = "title";
        public static final String COLUMN_NAME_ALBUM_COVER_SMALL = "cover_small";
        public static final String COLUMN_NAME_ALBUM_ARTIST_ID = "artist_id";
        public static final String COLUMN_NAME_ARTIST_NAME = "name";
        public static final String COLUMN_NAME_ARTIST_PICTURE_SMALL = "picture_small";
    }
}
