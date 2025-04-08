package com.example.mappractice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AchievementActivity extends AppCompatActivity {
    //这个类表示成就页面的活动
    //在这个页面中，用户可以查看他们的成就和统计数据
    //例如，用户可以查看他们的总行程、总时间、平均速度等信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        //初始化界面元素和数据
    }
}
