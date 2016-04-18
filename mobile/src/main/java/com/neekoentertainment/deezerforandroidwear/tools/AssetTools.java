package com.neekoentertainment.deezerforandroidwear.tools;

import android.graphics.Bitmap;
import android.view.View;

import com.google.android.gms.wearable.Asset;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by Nicolas on 4/18/2016.
 */
public class AssetTools {

    public static void getBitmapAssetFromUrl(String mUrl, final OnBitmapLoaded mCallback, ImageLoader imageLoader) {
        imageLoader.loadImage(mUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                loadedImage.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                mCallback.onBitmapLoaded(Asset.createFromBytes(byteStream.toByteArray()));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    public static Asset getAssetFromJsonObject(JSONObject jsonObject) {
        return Asset.createFromBytes(jsonObject.toString().getBytes());
    }

    public interface OnBitmapLoaded {
        void onBitmapLoaded(Asset asset);
    }
}
