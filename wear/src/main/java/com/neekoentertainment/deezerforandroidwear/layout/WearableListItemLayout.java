package com.neekoentertainment.deezerforandroidwear.layout;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neekoentertainment.deezerforandroidwear.R;

/**
 * Created by Nicolas on 19/11/2015.
 */
public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private final float mFadedTextAlpha;
    private TextView mAlbumName;
    private TextView mArtistName;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFadedTextAlpha = 40 / 100f;
    }

    /**
     * Finalize inflating a view from XML. This is called as the last phase of inflation,
     * after all child views have been added. Even if the subclass overrides onFinishInflate,
     * they should always be sure to call the super method, so that we get called.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAlbumName = (TextView) findViewById(R.id.album_name);
        mArtistName = (TextView) findViewById(R.id.artist_name);
    }

    /**
     * Called when this view becomes central item of the WearableListView.
     */
    @Override
    public void onCenterPosition(boolean animate) {
        mAlbumName.setAlpha(1f);
        mArtistName.setAlpha(1f);
    }

    /**
     * Called when this view stops being the central item of the WearableListView.     *
     */

    @Override
    public void onNonCenterPosition(boolean animate) {
        mAlbumName.setAlpha(mFadedTextAlpha);
        mArtistName.setAlpha(mFadedTextAlpha);
    }
}
