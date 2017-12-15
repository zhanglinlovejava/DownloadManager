package com.zhanglin.downloadmanager.download;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.zhanglin.downloadmanager.BuildConfig;

import java.io.File;


/**
 * Created by zhanglin on 2017/12/14.
 */

public class FileUtils {

    /**
     * 从url获取 如果url为空，则文件名为当前时间毫秒值
     *
     * @param url download file url
     * @return file name
     */
    public static String getFileNameFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            return url.substring(url.lastIndexOf("/") + 1);
        }
        return System.currentTimeMillis() + "";
    }

    public static String getDefaultFilePath() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/okhttp/download/";
        File file = new File(filePath);
        if (!file.exists()) {
            boolean createDir = file.mkdirs();
            if (createDir) {
                if (BuildConfig.DEBUG) Log.d("DownloadTask", "create file dir success");
            }
        }
        return filePath;
    }
}
