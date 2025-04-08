package com.example.mappractice;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TrackImporter {

    public void importGpxFromFile(String filePath) {
        //从指定的 GPX 文件路径读取轨迹点数据，并将其保存到数据库中
        List<LocationPoint> points = parseGpx(filePath);
        insertImportedTrackToDatabase(points, filePath);
    }

    public void importKmlFromFile(String filePath) {
        //与 importGpxFromFile() 类似，从指定的 GPX 文件路径读取轨迹点数据，并将其保存到数据库中
        List<LocationPoint> points = parseKml(filePath);
        insertImportedTrackToDatabase(points, filePath);
    }

    private List<LocationPoint> parseGpx(String filePath) {
        //解析GPX文件，解析结果输出为轨迹点
        List<LocationPoint> points = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            int eventType = parser.getEventType();
            LocationPoint currentPoint = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    if ("trkpt".equals(tagName)) {
                        double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        currentPoint = new LocationPoint(lat, lon, System.currentTimeMillis());
                    } else if ("time".equals(tagName) && currentPoint != null) {
                        String timeText = parser.nextText();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        currentPoint.timestamp = sdf.parse(timeText).getTime();
                    }
                } else if (eventType == XmlPullParser.END_TAG && "trkpt".equals(tagName)) {
                    if (currentPoint != null) {
                        points.add(currentPoint);
                        currentPoint = null;
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }

    private List<LocationPoint> parseKml(String filePath) {
        //解析KML文件，解析结果输出为轨迹点
        List<LocationPoint> points = new ArrayList<>();
        try {
            InputStream inputStream = new FileInputStream(filePath);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            int eventType = parser.getEventType();
            boolean insideCoordinates = false;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                if (eventType == XmlPullParser.START_TAG && "coordinates".equals(tagName)) {
                    insideCoordinates = true;
                    String coordText = parser.nextText().trim();
                    String[] coordLines = coordText.split("\\s+");
                    for (String line : coordLines) {
                        String[] parts = line.split(",");
                        if (parts.length >= 2) {
                            double lon = Double.parseDouble(parts[0]);
                            double lat = Double.parseDouble(parts[1]);
                            long timestamp = System.currentTimeMillis(); // Optional: replace with actual time if available
                            points.add(new LocationPoint(lat, lon, timestamp));
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && "coordinates".equals(tagName)) {
                    insideCoordinates = false;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return points;
    }

    private void insertImportedTrackToDatabase(List<LocationPoint> points, String fileName) {
        //将解析出来的轨迹数据作为一个轨迹记录（只记录文件名 + 起始时间 + 结束时间）插入数据库表
        if (points.isEmpty()) return;

        long startTime = points.get(0).timestamp;
        long endTime = points.get(points.size() - 1).timestamp;

        String filePath = fileName; // optional full path or just the name
        TrackDatabaseHelper dbHelper = new TrackDatabaseHelper(MyApplication.getContext());
        dbHelper.insertTrackMeta(filePath, startTime, endTime);
    }
}
