package com.neekoentertainment.deezerforandroidwear.database;

import android.provider.BaseColumns;

/**
 * Created by Nicolas on 5/15/2016.
 */
public class DeezerWearContract {

    public DeezerWearContract() {
    }

    public static abstract class UserLikedAlbumsWear implements BaseColumns {
        public static final String TABLE_NAME = "userlikedalbumswear";
        public static final String COLUMN_NAME_ALBUM_ID = "album_id";
        public static final String COLUMN_NAME_ALBUM_TITLE = "album_title";
        public static final String COLUMN_NAME_ALBUM_COVER = "album_cover";
        public static final String COLUMN_NAME_ARTIST_NAME = "artist_name";
    }
}
