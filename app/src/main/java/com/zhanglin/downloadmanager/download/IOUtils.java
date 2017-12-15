package com.zhanglin.downloadmanager.download;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

    /**
     * 关闭流
     * @param closeables io
     */
    public static void close(Closeable... closeables) {
        for (Closeable io : closeables) {
            if (io != null) {
                try {
                    io.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
