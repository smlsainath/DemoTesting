package com.boma.appconfig;

import android.app.Application;
import android.content.Context;

/**
 * Created by sai on 23/7/16.
 */
public class ApplicationContext extends Application {

    public static Context mObjContext = null;

    public static Context getInstance() {
        if (mObjContext == null) {
            mObjContext = new ApplicationContext();
        }
        return mObjContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
    }
}
