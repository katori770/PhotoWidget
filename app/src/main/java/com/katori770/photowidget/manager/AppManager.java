package com.katori770.photowidget.manager;


import android.content.Context;

public class AppManager {
    private static AppManager instance;
    private Context context;
    private GPhotoManager googlePhotoManager;

    public AppManager(Context context) {
        this.context = context;
        this.googlePhotoManager = new GPhotoManager(context);
    }

    public static synchronized void create(Context context) {
        instance = new AppManager(context);
    }

    public static AppManager getInstance() {
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public GPhotoManager getGooglePhotoManager() {
        return googlePhotoManager;
    }
}
