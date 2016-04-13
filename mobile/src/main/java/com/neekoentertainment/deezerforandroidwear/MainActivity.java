package com.neekoentertainment.deezerforandroidwear;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.google.android.gms.common.api.GoogleApiClient;
import com.neekoentertainment.deezerforandroidwear.tools.ServicesAuthentication;

public class MainActivity extends AppCompatActivity {

    private static GoogleApiClient mGoogleApiClient;

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = ServicesAuthentication.getGoogleApiClient(this);
        setContentView(R.layout.deezer_connect_activity);
        checkDeezerConnection();
    }

    private void checkDeezerConnection() {
        final RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.container);
        final Button connectButton = (Button) findViewById(R.id.deezer_connect_button);
        ServicesAuthentication.DeezerConnection mCallback = new ServicesAuthentication.DeezerConnection() {
            @Override
            public void onDeezerConnected(DeezerConnect deezerConnect) {
                if (connectButton != null) {
                    connectButton.setVisibility(View.GONE);
                }
                ImageView androidWearIcon = (ImageView) findViewById(R.id.android_wear_imageview);
                TextView deezerConnectedTextView = (TextView) findViewById(R.id.deezer_connected_textview);
                if (androidWearIcon != null) {
                    androidWearIcon.setVisibility(View.VISIBLE);
                }
                if (deezerConnectedTextView != null) {
                    deezerConnectedTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onDeezerDisconnected() {
            }

            @Override
            public void onDeezerFailed(String e) {
                if (connectButton != null) {
                    connectButton.setVisibility(View.VISIBLE);
                    connectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkDeezerConnection();
                        }
                    });
                }
                if (relativeLayout != null) {
                    Snackbar.make(relativeLayout, "Connection to Deezer Failed.", Snackbar.LENGTH_SHORT);
                }
            }
        };
        ServicesAuthentication.getDeezerConnect(this, mCallback);
    }
}