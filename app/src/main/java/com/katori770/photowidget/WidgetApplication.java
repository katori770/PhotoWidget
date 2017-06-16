package com.katori770.photowidget;

import android.app.Application;

import com.katori770.photowidget.manager.AppManager;


public class WidgetApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppManager.create(this);
    }
}
