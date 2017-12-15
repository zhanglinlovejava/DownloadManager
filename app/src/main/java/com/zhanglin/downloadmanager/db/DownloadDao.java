package com.zhanglin.downloadmanager.db;

import com.j256.ormlite.dao.Dao;
import com.zhanglin.downloadmanager.AppApplication;
import com.zhanglin.downloadmanager.entity.DownloadEntity;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglin on 2017/12/14.
 */
public class DownloadDao {

    private Dao<DownloadEntity, Integer> downloadDaos;
    private DBHelper dbHelper;
    private volatile static DownloadDao instance = null;

    /**
     * 构造方法
     * 获得数据库帮助类实例，通过传入Class对象得到相应的Dao
     */
    private DownloadDao() {
        try {
            dbHelper = DBHelper.getHelper(AppApplication.getContext());
            downloadDaos = dbHelper.getDao(DownloadEntity.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单例模式，使用applicationContext 不使用activity的context，避免内存泄漏
     *
     * @return
     */
    public static DownloadDao getInstance() {
        if (instance == null) {
            synchronized (DownloadDao.class) {
                if (instance == null) {
                    instance = new DownloadDao();
                }
            }
        }
        return instance;
    }

    /**
     * 添加一条记录
     *
     * @param contentFile
     */
    public void insert(DownloadEntity contentFile) {
        try {
            downloadDaos.createOrUpdate(contentFile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(DownloadEntity downloadEntity) {
        try {
            downloadDaos.update(downloadEntity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除一条记录
     *
     * @param contentFile
     */
    public void delete(DownloadEntity contentFile) {
        try {
            downloadDaos.delete(contentFile);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 查询所有记录
     *
     * @return
     */
    public List<DownloadEntity> queryAllContent() {
        List<DownloadEntity> contentFiles = new ArrayList<DownloadEntity>();
        try {
            contentFiles = downloadDaos.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contentFiles;
    }

    /**
     * 根据id 查询
     *
     * @param taskId
     * @return
     */
    public DownloadEntity queryWithId(String taskId) {
        List<DownloadEntity> contentFiles = new ArrayList<>();
        try {
            contentFiles = downloadDaos.queryBuilder().where().eq("taskId", taskId).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contentFiles.size() == 0 ? null : contentFiles.get(0);
    }

}
