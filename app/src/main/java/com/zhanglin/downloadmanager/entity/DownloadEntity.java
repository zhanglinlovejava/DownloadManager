package com.zhanglin.downloadmanager.entity;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by zhanglin on 2017/12/14.
 */

public class DownloadEntity {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "url")
    public String url;
    @DatabaseField(columnName = "fileName")
    public String fileName;
    @DatabaseField(columnName = "filePath")
    public String filePath;
    @DatabaseField(columnName = "taskStatus")
    public int taskStatus;
    @DatabaseField(columnName = "taskId")
    public String taskId;
    @DatabaseField(columnName = "totalSize")
    public long totalSize;
    @DatabaseField(columnName = "completedSize")
    public long completedSize;
    @DatabaseField(columnName = "title")
    public String title;

    public DownloadEntity(String title, String url) {
        this.url = url;
        this.title = title;
    }

    public DownloadEntity() {
    }
}
