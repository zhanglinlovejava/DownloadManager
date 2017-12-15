package com.zhanglin.downloadmanager.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhanglin.downloadmanager.db.DownloadDao;
import com.zhanglin.downloadmanager.entity.DownloadEntity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhanglin on 2017/12/14.
 */

public class DownloadTask implements Runnable {
    private DownloadEntity downloadEntity;
    private DownloadTaskListener mListener;
    private OkHttpClient mClient;


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int code = msg.what;
            switch (code) {
                case TaskStatus.TASK_STATUS_QUEUE:
                    mListener.onQueue(DownloadTask.this);
                    break;
                case TaskStatus.TASK_STATUS_CONNECTING:
                    mListener.onConnecting(DownloadTask.this);
                    break;
                case TaskStatus.TASK_STATUS_DOWNLOADING:
                    mListener.onStart(DownloadTask.this);
                    break;
                case TaskStatus.TASK_STATUS_PAUSE:
                    mListener.onPause(DownloadTask.this);
                    break;
                case TaskStatus.TASK_STATUS_CANCEL:
                    mListener.onCancel(DownloadTask.this);
                    break;
                case TaskStatus.TASK_STATUS_REQUEST_ERROR:
                    mListener.onError(DownloadTask.this, TaskStatus.TASK_STATUS_REQUEST_ERROR);
                    break;
                case TaskStatus.TASK_STATUS_STORAGE_ERROR:
                    mListener.onError(DownloadTask.this, TaskStatus.TASK_STATUS_STORAGE_ERROR);
                    break;
                case TaskStatus.TASK_STATUS_FINISH:
                    mListener.onFinish(DownloadTask.this);
                    break;

            }
        }
    };

    public DownloadTask(DownloadEntity downloadEntity) {
        this.downloadEntity = downloadEntity;
    }

    @Override
    public void run() {

        InputStream inputStream = null;
        BufferedInputStream bis = null;
        RandomAccessFile tempFile = null;

        try {
            String fileName = TextUtils.isEmpty(downloadEntity.fileName) ? FileUtils.getFileNameFromUrl(downloadEntity.url) : downloadEntity.fileName;
            String filePath = TextUtils.isEmpty(downloadEntity.filePath) ? FileUtils.getDefaultFilePath() : downloadEntity.filePath;

            downloadEntity.fileName = fileName;
            downloadEntity.filePath = filePath;

            tempFile = new RandomAccessFile(new File(filePath, fileName), "rwd");
            downloadEntity.taskStatus = TaskStatus.TASK_STATUS_CONNECTING;
            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_CONNECTING);

            if (DownloadDao.getInstance().queryWithId(downloadEntity.taskId) != null) {
                DownloadDao.getInstance().update(downloadEntity);
            }

            long completeSize = downloadEntity.completedSize;

            Request request;
            try {
                request = new Request.Builder().url(downloadEntity.url).header("RANGE", "bytes=" + completeSize + "-").build();
            } catch (Exception e) {
                e.printStackTrace();
                downloadEntity.taskStatus = TaskStatus.TASK_STATUS_REQUEST_ERROR;
                handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
                Log.d("DownloadTask", e.getMessage());
                return;
            }
            if (tempFile.length() == 0) {
                completeSize = 0;
            }
            tempFile.seek(completeSize);

            Response response = mClient.newCall(request).execute();
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    if (DownloadDao.getInstance().queryWithId(downloadEntity.taskId) == null) {
                        DownloadDao.getInstance().insert(downloadEntity);
                        downloadEntity.totalSize = body.contentLength();
                    }
                    downloadEntity.taskStatus = TaskStatus.TASK_STATUS_DOWNLOADING;
                    double updateSize = downloadEntity.totalSize / 100;
                    inputStream = body.byteStream();
                    bis = new BufferedInputStream(inputStream);
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    int bufferOffset = 0;
                    while ((length = bis.read(buffer)) > 0 && downloadEntity.taskStatus != TaskStatus.TASK_STATUS_CANCEL && downloadEntity.taskStatus != TaskStatus.TASK_STATUS_PAUSE) {
                        tempFile.write(buffer, 0, length);
                        completeSize += length;
                        bufferOffset += length;
                        downloadEntity.completedSize = completeSize;
                        // 避免一直调用数据库
                        if (bufferOffset >= updateSize) {
                            bufferOffset = 0;
                            DownloadDao.getInstance().update(downloadEntity);
                            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_DOWNLOADING);
                        }

                        if (completeSize == downloadEntity.totalSize) {
                            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_DOWNLOADING);
                            downloadEntity.taskStatus = TaskStatus.TASK_STATUS_FINISH;
                            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_FINISH);
                            DownloadDao.getInstance().update(downloadEntity);
                        }
                    }
                }
            } else {
                downloadEntity.taskStatus = TaskStatus.TASK_STATUS_REQUEST_ERROR;
                handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
            }


        } catch (FileNotFoundException e) {
            downloadEntity.taskStatus = TaskStatus.TASK_STATUS_STORAGE_ERROR;
            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_STORAGE_ERROR);
        } catch (SocketTimeoutException | ConnectException e) {
            downloadEntity.taskStatus = TaskStatus.TASK_STATUS_REQUEST_ERROR;
            handler.sendEmptyMessage(TaskStatus.TASK_STATUS_REQUEST_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bis, inputStream, tempFile);
        }

    }

    public DownloadEntity getDownloadEntity() {
        return downloadEntity;
    }

    void pause() {
        downloadEntity.taskStatus = TaskStatus.TASK_STATUS_PAUSE;
        DownloadDao.getInstance().update(downloadEntity);
        handler.sendEmptyMessage(TaskStatus.TASK_STATUS_PAUSE);
    }

    void queue() {
        downloadEntity.taskStatus = TaskStatus.TASK_STATUS_QUEUE;
        handler.sendEmptyMessage(TaskStatus.TASK_STATUS_QUEUE);
    }

    void cancel() {
        downloadEntity.taskStatus = TaskStatus.TASK_STATUS_CANCEL;
        DownloadDao.getInstance().delete(downloadEntity);
        handler.sendEmptyMessage(TaskStatus.TASK_STATUS_CANCEL);
    }

    void setClient(OkHttpClient mClient) {
        this.mClient = mClient;
    }

    public void setListener(DownloadTaskListener listener) {
        mListener = listener;
    }
}
