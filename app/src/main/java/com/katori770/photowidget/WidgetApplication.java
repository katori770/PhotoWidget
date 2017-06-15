package com.katori770.photowidget;

import android.app.Application;


public class WidgetApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.create(this);
    }
}
