package org.jeson.reinforce.shell.$$$.application;

import android.app.Application;
import android.content.Context;
import org.jeson.reinforce.shell.$$$.processor.ApplicationDefaultProcessor;
import org.jeson.reinforce.shell.$$$.processor.ApplicationProcessor;

public class MainApplication extends Application {

    private final static ApplicationProcessor applicationProcessor = ApplicationProcessor.DEFAULT;

    static {
        applicationProcessor.onAppClassInit();
    }

    public MainApplication() {
        applicationProcessor.onAppConstructorInit();
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        applicationProcessor.onAppAttachBaseContext(context);
    }

    @Override
    public void onCreate() {
        applicationProcessor.onAppCreate(this);
    }

}
