package com.neekoentertainment.deezerforandroidwear.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nicolas on 4/18/2016.
 */
public class BitmapTools {

    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        stream.close();
        return byteArray;
    }

    public static Bitmap loadBitmapFromAsset(Asset asset, GoogleApiClient googleApiClient) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                googleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                googleApiClient, asset).await().getInputStream();
        googleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w("LoadBitmap", "Requested an unknown Asset.");
            return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }
}
