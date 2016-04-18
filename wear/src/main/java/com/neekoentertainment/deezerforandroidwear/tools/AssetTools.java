package com.neekoentertainment.deezerforandroidwear.tools;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nicolas on 4/18/2016.
 */
public class AssetTools {

    public static InputStream getAssetInputStream(Asset asset, GoogleApiClient googleApiClient) {
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
        return assetInputStream;
    }

    public static JSONObject loadJsonFromAsset(Asset asset, GoogleApiClient googleApiClient) throws IOException, JSONException {
        InputStream assetInputStream = getAssetInputStream(asset, googleApiClient);
        if (assetInputStream == null) {
            Log.e("LoadJson", "Requested an unknown Asset.");
            return null;
        }
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(assetInputStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();

        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JSONObject(responseStrBuilder.toString());
    }
}
