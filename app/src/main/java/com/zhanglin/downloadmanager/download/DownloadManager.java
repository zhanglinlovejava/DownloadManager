package com.zhanglin.downloadmanager.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zhanglin.downloadmanager.db.DownloadDao;
import com.zhanglin.downloadmanager.entity.DownloadEntity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by zhanglin on 2017/12/14.
 */

public class DownloadManager {
    private static DownloadManager instance = null;
    private int mThreadCount = 1;
    public static final int MAX_THREAD_COUNT = 15;
    private LinkedBlockingDeque<Runnable> mQueue;
    private ThreadPoolExecutor mExecutor;
    private Map<String, DownloadTask> mCurrentTaskList;
    private OkHttpClient mClient;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * @param context Application
     */
    public void init(@NonNull Context context) {
        init(context, getAppropriateThreadCount());
    }

    /**
     * @param context     Application
     * @param threadCount the max download count
     */
    public void init(@NonNull Context context, int threadCount) {
        init(context, threadCount, getOkHttpClient());
    }

    public void init(Context context, int threadCount, @NonNull OkHttpClient okHttpClient) {
        recoveryTaskState();
        mThreadCount = threadCount < 1 ? 1 : threadCount <= MAX_THREAD_COUNT ? threadCount : MAX_THREAD_COUNT;
        mExecutor = new ThreadPoolExecutor(mThreadCount, mThreadCount, 20, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
        mQueue = (LinkedBlockingDeque<Runnable>) mExecutor.getQueue();
        mCurrentTaskList = new HashMap<>();
        this.mClient = okHttpClient;
    }


    public void addTask(DownloadTask task) {
        DownloadEntity downloadEntity = task.getDownloadEntity();
        if (downloadEntity != null && downloadEntity.taskStatus != TaskStatus.TASK_STATUS_DOWNLOADING) {
            task.setClient(mClient);
            mCurrentTaskList.put(downloadEntity.taskId, task);
            if (!mQueue.contains(task)) {
                mExecutor.execute(task);
            }
            if (mExecutor.getTaskCount() > mThreadCount) {
                task.queue();
            }
        }
    }

    public void pauseTask(DownloadTask task) {
        if (mQueue.contains(task)) {
            mQueue.remove(task);
        }
        task.pause();
    }

    public void resumeTask(DownloadTask task) {
        addTask(task);
    }

    public void cancelTask(DownloadTask task) {
        if (task == null) return;
        DownloadEntity entity = task.getDownloadEntity();
        if (entity != null) {
            if (entity.taskStatus == TaskStatus.TASK_STATUS_DOWNLOADING) {
                pauseTask(task);
                mExecutor.remove(task);
            }
            if (mQueue.contains(task)) {
                mQueue.remove(task);
            }
            mCurrentTaskList.remove(task);
            task.cancel();
            if (!TextUtils.isEmpty(entity.filePath) && !TextUtils.isEmpty(entity.fileName)) {
                File tempFile = new File(entity.filePath, entity.fileName);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }


        }
    }

    /**
     * @return task
     */
    public DownloadTask getTask(String id) {
        DownloadTask currTask = mCurrentTaskList.get(id);
        if (currTask == null) {
            DownloadEntity entity = DownloadDao.getInstance().queryWithId(id);
            if (entity != null) {
                int status = entity.taskStatus;
                currTask = new DownloadTask(entity);
                if (status != TaskStatus.TASK_STATUS_FINISH) {
                    mCurrentTaskList.put(id, currTask);
                }
            }
        }
        return currTask;
    }

    public boolean isPauseTask(String id) {
        DownloadEntity entity = DownloadDao.getInstance().queryWithId(id);
        if (entity != null) {
            File file = new File(entity.filePath, entity.fileName);
            if (file.exists()) {
                long totalSize = entity.totalSize;
                return totalSize > 0 && file.length() < totalSize;
            }
        }
        return false;
    }

    public boolean isFinishTask(String id) {
        DownloadEntity entity = DownloadDao.getInstance().queryWithId(id);
        if (entity != null) {
            File file = new File(entity.filePath, entity.fileName);
            if (file.exists()) {
                return file.length() == entity.totalSize;
            }
        }
        return false;
    }

    private void recoveryTaskState() {
        List<DownloadEntity> entities = DownloadDao.getInstance().queryAllContent();
        for (DownloadEntity entity : entities) {
            long completedSize = entity.completedSize;
            long totalSize = entity.totalSize;
            if (completedSize > 0 && completedSize != totalSize && entity.taskStatus != TaskStatus.TASK_STATUS_PAUSE) {
                entity.taskStatus = TaskStatus.TASK_STATUS_PAUSE;
            }
            DownloadDao.getInstance().update(entity);
        }
    }

    /**
     * @return generate the appropriate thread count.
     */
    private int getAppropriateThreadCount() {
        return Runtime.getRuntime().availableProcessors() * 2 + 1;
    }


    /**
     * generate default client
     */
    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
    }
}
