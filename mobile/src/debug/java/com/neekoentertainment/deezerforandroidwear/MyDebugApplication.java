package com.neekoentertainment.deezerforandroidwear;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Nicolas on 5/9/2016.
 */
public class MyDebugApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
    }
}
