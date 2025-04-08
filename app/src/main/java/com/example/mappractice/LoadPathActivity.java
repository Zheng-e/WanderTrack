package com.example.mappractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadPathActivity extends AppCompatActivity {
    //这个类表示加载轨迹页面的活动
    //在这个页面中，用户可以选择要加载的轨迹文件，并查看轨迹信息
    //例如，用户可以查看轨迹的起点、终点、总距离等信息

    ListView trackListView;
    TrackDatabaseHelper trackDatabaseHelper;
    TrackAdapter trackAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_path);

        trackListView = findViewById(R.id.file_list);
        trackDatabaseHelper = new TrackDatabaseHelper(this);
        trackAdapter = new TrackAdapter(this, trackDatabaseHelper.getAllTracks());
        trackListView.setAdapter(trackAdapter);
        //在这里可以添加点击事件处理
        //例如，用户点击某个轨迹后，可以打开地图页面并加载该轨迹
        //或者显示轨迹的详细信息
        trackListView.setOnItemClickListener((parent, view, position, id) -> {
            TrackMeta selectedTrack = trackAdapter.getItem(position);
            Intent intent = new Intent(LoadPathActivity.this, MainActivity.class);
            intent.putExtra("selectedTrack", selectedTrack);
            startActivity(intent);
        });

    }
}
