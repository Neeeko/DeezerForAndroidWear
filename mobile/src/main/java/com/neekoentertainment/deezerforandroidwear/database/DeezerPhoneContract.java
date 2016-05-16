package com.neekoentertainment.deezerforandroidwear.database;

import android.provider.BaseColumns;

/**
 * Created by Nicolas on 4/24/2016.
 */
public class DeezerPhoneContract {

    public DeezerPhoneContract() {
    }

    public static abstract class UserLikedAlbumsPhone implements BaseColumns {
        public static final String TABLE_NAME = "userlikedalbumsphone";
        public static final String COLUMN_NAME_ALBUM_ID = "album_id";
        public static final String COLUMN_NAME_ALBUM_TITLE = "album_title";
        public static final String COLUMN_NAME_ALBUM_COVER = "album_cover";
        public static final String COLUMN_NAME_ALBUM_ARTIST_ID = "artist_id";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
        public static final String COLUMN_NAME_ARTIST_PICTURE = "artist_picture";
    }
}
