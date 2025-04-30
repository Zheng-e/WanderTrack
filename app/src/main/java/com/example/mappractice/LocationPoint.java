package com.example.mappractice;

public class LocationPoint {
    //这个类表示 一个具体的轨迹点信息，也就是设备在某个时间点所处的经纬度位置。
    public double latitude; //纬度
    public double longitude; //经度
    public long timestamp; //时间戳

    public LocationPoint(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}