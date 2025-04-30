package com.example.mappractice;

import java.io.Serializable;

public class TrackMeta implements Serializable {
    private String filePath;
    private long startTime;
    private long endTime;

    public TrackMeta(String filePath, long startTime, long endTime) {
        //这个类是对整条轨迹的摘要描述信息
        this.filePath = filePath; //GPX文件路径
        this.startTime = startTime; //轨迹开始时间
        this.endTime = endTime; //轨迹结束时间
    }

    public String getFilePath() {
        return filePath;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}