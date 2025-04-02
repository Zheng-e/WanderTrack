package com.example.mappractice;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TrackRecorder {
    private List<LocationPoint> trackPoints = new ArrayList<>();
    private long startTime = 0;
    private long endTime = 0;

    public void startTracking() {
        //开始记录轨迹
        trackPoints.clear();
        startTime = System.currentTimeMillis();
    }

    public void pauseTracking() {
        //暂停记录轨迹
        //后续有需要再添加
    }

    public void stopTrackingAndSaveGpx() {
        //停止记录轨迹，并将轨迹数据保存为GPX文件后存入数据库
        endTime = System.currentTimeMillis();
        saveToGpxFile(trackPoints);
        saveTrackMetaToDatabase(generateFileName(), startTime, endTime);
    }

    private void saveToGpxFile(List<LocationPoint> points) {
        //将记录的轨迹点保存为GPX文件
        String fileName = generateFileName();
        File gpxFile = new File(MyApplication.getContext().getExternalFilesDir(null), fileName);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        //GPX文件标准时区为UTC时区
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try (FileOutputStream fos = new FileOutputStream(gpxFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<gpx version=\"1.1\" creator=\"WanderTrack\">\n");
            writer.write("  <trk><name>Recorded Track</name><trkseg>\n");

            for (LocationPoint point : points) {
                writer.write(String.format(Locale.US,
                        "    <trkpt lat=\"%f\" lon=\"%f\"><time>%s</time></trkpt>\n",
                        point.latitude, point.longitude,
                        sdf.format(new Date(point.timestamp))));
            }

            writer.write("  </trkseg></trk>\n");
            writer.write("</gpx>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("TrackRecorder", "GPX saved to: " + gpxFile.getAbsolutePath());
    }

    private String generateFileName() {
        //生成文件名
        return "track_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".gpx";
    }

    private void saveTrackMetaToDatabase(String filePath, long startTime, long endTime) {
        //将轨迹数据存入数据库
        TrackDatabaseHelper dbHelper = new TrackDatabaseHelper(MyApplication.getContext());
        dbHelper.insertTrackMeta(filePath, startTime, endTime);
        Log.d("TrackRecorder", "Track metadata saved: " + filePath);
    }

    public void addLocationPoint(LocationPoint point) {
        trackPoints.add(point);
    }
}
