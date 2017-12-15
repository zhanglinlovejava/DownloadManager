package com.zhanglin.downloadmanager;

import android.app.Application;
import android.content.Context;

import com.zhanglin.downloadmanager.download.DownloadManager;

/**
 * Created by zhanglin on 2017/12/14.
 */

public class AppApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        DownloadManager.getInstance().init(this, 3);
    }

    public static Context getContext() {
        return context;
    }
}
