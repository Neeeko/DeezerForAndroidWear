package com.neekoentertainment.deezerforandroidwear.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Nicolas on 12/10/2015.
 */
public class ServicesAuthentication {

    private static final String APP_ID = "166645";

    private static final String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY};

    public static GoogleApiClient getGoogleApiClient(Context mContext) {
        return new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("GoogleApiClient", "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("GoogleApiClient", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Log.d("GoogleApiClient", "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    public static void getDeezerConnect(final Activity mContext, final DeezerConnection mCallback) {
        final DeezerConnect deezerConnect = new DeezerConnect(mContext.getApplicationContext(), APP_ID);
        SessionStore sessionStore = new SessionStore();
        if (!sessionStore.restore(deezerConnect, mContext.getApplicationContext())) {
            // No value on the Session Store, or invalid token
            mCallback.onDeezerDisconnected();
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, mContext.getApplicationContext());
                    mCallback.onDeezerConnected(deezerConnect);
                }

                public void onCancel() {
                    mCallback.onDeezerFailed("Connection process canceled");
                }

                public void onException(Exception e) {
                    mCallback.onDeezerFailed(e.getMessage());
                }
            };
            deezerConnect.authorize(mContext, permissions, listener);
        }
        // Retrieved from the Session Store
        if (deezerConnect.isSessionValid())
            mCallback.onDeezerConnected(deezerConnect);
    }

    public interface DeezerConnection {
        void onDeezerConnected(DeezerConnect deezerConnect);

        void onDeezerDisconnected();

        void onDeezerFailed(String e);
    }
}
