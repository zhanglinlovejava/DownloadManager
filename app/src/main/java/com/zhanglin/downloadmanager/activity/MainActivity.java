package com.zhanglin.downloadmanager.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zhanglin.downloadmanager.R;
import com.zhanglin.downloadmanager.adapter.DownloadTaskAdapter;
import com.zhanglin.downloadmanager.entity.DownloadEntity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        ButterKnife.bind(this);
        DownloadTaskAdapter adapter = new DownloadTaskAdapter(this, getData());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    private List<DownloadEntity> getData() {
        List<DownloadEntity> list = new ArrayList<>();
        list.add(new DownloadEntity("任务1", "http://sw.bos.baidu.com/sw-search-sp/software/84d6cbfd4a092/QQ_mac_6.1.1.dmg"));
        list.add(new DownloadEntity("任务2", "http://sw.bos.baidu.com/sw-search-sp/software/76f6543a7be90/thunder_mac_3.1.7.3266.dmg"));
        list.add(new DownloadEntity("任务3", "http://sw.bos.baidu.com/sw-search-sp/software/a387d5b1124fb/youkumac_1.3.1.12078.dmg"));
        list.add(new DownloadEntity("任务4", "http://sw.bos.baidu.com/sw-search-sp/software/c354986350376/iQIYIMedia_002_4.15.8.dmg"));
        list.add(new DownloadEntity("任务5", "http://sw.bos.baidu.com/sw-search-sp/software/efdb3d826d174/TencentVideo_mac_1.1.0.dmg"));
        return list;
    }
}
