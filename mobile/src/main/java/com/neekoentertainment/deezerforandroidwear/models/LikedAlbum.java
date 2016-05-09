package com.neekoentertainment.deezerforandroidwear.models;

/**
 * Created by Nicolas on 5/6/2016.
 */
public class LikedAlbum {

    private long mId;
    private String mAlbumTitle;
    private String mCoverSmall;
    private long mArtistId;
    private String mArtistName;
    private String mArtistPictureSmall;

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getAlbumTitle() {
        return mAlbumTitle;
    }

    public void setAlbumTitle(String mAlbumTitle) {
        this.mAlbumTitle = mAlbumTitle;
    }

    public String getCoverSmall() {
        return mCoverSmall;
    }

    public void setCoverSmall(String mCoverSmall) {
        this.mCoverSmall = mCoverSmall;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(long mArtistId) {
        this.mArtistId = mArtistId;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public String getArtistPictureSmall() {
        return mArtistPictureSmall;
    }

    public void setArtistPictureSmall(String mArtistPictureSmall) {
        this.mArtistPictureSmall = mArtistPictureSmall;
    }
}
