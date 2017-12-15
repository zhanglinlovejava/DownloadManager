package com.zhanglin.downloadmanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanglin.downloadmanager.R;
import com.zhanglin.downloadmanager.download.DownloadManager;
import com.zhanglin.downloadmanager.download.DownloadTask;
import com.zhanglin.downloadmanager.download.DownloadTaskListener;
import com.zhanglin.downloadmanager.download.TaskStatus;
import com.zhanglin.downloadmanager.entity.DownloadEntity;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by zhanglin on 2017/12/14.
 */

public class DownloadTaskAdapter extends RecyclerView.Adapter<DownloadTaskAdapter.DownViewHolder> {
    private Context mContext;
    private List<DownloadEntity> mList;
    private DownloadManager downloadManager;

    public DownloadTaskAdapter(Context mContext, List<DownloadEntity> mList) {
        this.mContext = mContext;
        this.mList = mList;
        downloadManager = DownloadManager.getInstance();
    }

    @Override
    public DownloadTaskAdapter.DownViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_layout, null);
        return new DownViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DownloadTaskAdapter.DownViewHolder holder, int position) {
        final DownloadEntity entity = mList.get(position);
        holder.tvTitle.setText(entity.title);
        holder.tvStart.setText("开始");
        if (TextUtils.isEmpty(entity.url)) {
            entity.url = "the item of " + holder.getAdapterPosition() + " is empty url...";
        }
        holder.itemView.setTag(entity.url);
        String taskId = String.valueOf(entity.url.hashCode());
        final DownloadTask itemTask = downloadManager.getTask(taskId);

        if (itemTask == null) {
            holder.tvStart.setText("下载");
            holder.pb.setProgress(0);
            holder.tvProgress.setText("0");
        } else {
            final DownloadEntity downloadEntity = itemTask.getDownloadEntity();
            int taskStatus = downloadEntity.taskStatus;
            setListener(holder, itemTask);
            String progress = getPercent(downloadEntity.completedSize, downloadEntity.totalSize);
            switch (taskStatus) {
                case TaskStatus.TASK_STATUS_INIT:
                    boolean isPause = downloadManager.isPauseTask(downloadEntity.taskId);
                    boolean isFinish = downloadManager.isFinishTask(downloadEntity.taskId);
                    holder.tvStart.setText(isFinish ? "删除" : !isPause ? "下载" : "继续");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_QUEUE:
                    holder.tvStart.setText("等待中");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_CONNECTING:
                    holder.tvStart.setText("连接中");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_DOWNLOADING:
                    holder.tvStart.setText("暂停");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_PAUSE:
                    holder.tvStart.setText("继续");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_FINISH:
                    holder.tvStart.setText("删除");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
                case TaskStatus.TASK_STATUS_REQUEST_ERROR:
                    holder.tvStart.setText("重试");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                case TaskStatus.TASK_STATUS_STORAGE_ERROR:
                    holder.tvStart.setText("重试");
                    holder.pb.setProgress(Integer.parseInt(progress));
                    holder.tvProgress.setText(progress);
                    break;
            }
        }
        holder.tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = entity.url;
                String taskId = String.valueOf(url.hashCode());
                DownloadTask itemTask = downloadManager.getTask(taskId);

                if (itemTask == null) {
                    DownloadEntity de = new DownloadEntity("新任务", entity.url);
                    de.taskId = taskId;
                    itemTask = new DownloadTask(de);
                    setListener(holder, itemTask);
                    downloadManager.addTask(itemTask);
                } else {
                    setListener(holder, itemTask);
                    DownloadEntity taskEntity = itemTask.getDownloadEntity();
                    int status = taskEntity.taskStatus;
                    switch (status) {
                        case TaskStatus.TASK_STATUS_QUEUE:
                            downloadManager.pauseTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_INIT:
                            downloadManager.addTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_CONNECTING:
                            downloadManager.pauseTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_DOWNLOADING:
                            downloadManager.pauseTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_CANCEL:
                            downloadManager.addTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_PAUSE:
                            downloadManager.resumeTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_FINISH:
                            downloadManager.cancelTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_REQUEST_ERROR:
                            downloadManager.addTask(itemTask);
                            break;
                        case TaskStatus.TASK_STATUS_STORAGE_ERROR:
                            downloadManager.addTask(itemTask);
                            break;
                    }
                }
            }
        });

    }

    private void setListener(final DownViewHolder holder, DownloadTask itemTask) {
        final DownloadEntity downloadEntity = itemTask.getDownloadEntity();
        itemTask.setListener(new DownloadTaskListener() {
            @Override
            public void onQueue(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("等待中");
                }
            }

            @Override
            public void onConnecting(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("连接中");
                }
            }

            @Override
            public void onStart(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("暂停");
                    holder.pb.setProgress(Integer.parseInt(getPercent(downloadEntity.completedSize, downloadEntity.totalSize)));
                    holder.tvProgress.setText(getPercent(downloadEntity.completedSize, downloadEntity.totalSize));
                }
            }

            @Override
            public void onPause(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("继续");
                }
            }

            @Override
            public void onCancel(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("下载");
                    holder.tvProgress.setText("0");
                    holder.pb.setProgress(0);
                }
            }

            @Override
            public void onFinish(DownloadTask downloadTask) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {
                    holder.tvStart.setText("删除");
                }
            }

            @Override
            public void onError(DownloadTask downloadTask, int code) {
                if (holder.itemView.getTag().equals(downloadEntity.url)) {

                    holder.tvStart.setText("重试");
                    switch (code) {
                        case TaskStatus.TASK_STATUS_REQUEST_ERROR:
                            Toast.makeText(mContext, "请求出错", Toast.LENGTH_SHORT).show();
                            break;
                        case TaskStatus.TASK_STATUS_STORAGE_ERROR:
                            Toast.makeText(mContext, "存储出错", Toast.LENGTH_SHORT).show();
                            break;

                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private String getPercent(long completed, long total) {

        if (total > 0) {
            double fen = ((double) completed / (double) total) * 100;
            DecimalFormat df1 = new DecimalFormat("0");
            return df1.format(fen);
        }
        return "0";
    }

    class DownViewHolder extends RecyclerView.ViewHolder {
        public DownViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.pbDownload)
        ProgressBar pb;
        @BindView(R.id.tvStart)
        TextView tvStart;
        @BindView(R.id.tvProgress)
        TextView tvProgress;

    }

}
