package com.example.mappractice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadPathActivity extends AppCompatActivity {
    //这个类表示加载轨迹页面的活动
    //在这个页面中，用户可以选择要加载的轨迹文件，并查看轨迹信息

    ListView trackListView;
    TrackDatabaseHelper trackDatabaseHelper;
    TrackAdapter trackAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        net.sqlcipher.database.SQLiteDatabase.loadLibs(this);
        setContentView(R.layout.activity_load_path);

        trackListView = findViewById(R.id.file_list);
        trackDatabaseHelper = new TrackDatabaseHelper(this);
        trackAdapter = new TrackAdapter(this, trackDatabaseHelper.getAllTracks());
        trackListView.setAdapter(trackAdapter);

        //用户点击某个轨迹后，可以打开地图页面并加载该轨迹
        trackListView.setOnItemClickListener((parent, view, position, id) -> {
            TrackMeta selectedTrack = trackAdapter.getItem(position);
//            String originName = getExternalFilesDir("gpx").getAbsolutePath() + selectedTrack.getFilePath();
            String originName = selectedTrack.getFilePath();
//            System.out.println("gpx file path is "+originName);
            Intent intent = new Intent(LoadPathActivity.this, MainActivity.class);
            intent.putExtra("selectedTrack", originName);
            startActivity(intent);

        });

    }
}
