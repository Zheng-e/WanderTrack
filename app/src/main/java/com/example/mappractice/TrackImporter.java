package com.example.mappractice;

import android.util.Xml;

import com.baidu.mapapi.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class TrackImporter {
    private byte[] encryptGPXContent(String content, String key) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content.getBytes("UTF-8"));
    }

    private void encryptAndSaveTrack(List<LocationPoint> points, File outputFile) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            StringBuilder gpxContent = new StringBuilder();
            gpxContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            gpxContent.append("<gpx version=\"1.1\" creator=\"WanderTrack\">\n");
            gpxContent.append("  <trk><name>Imported Track</name><trkseg>\n");

            for (LocationPoint point : points) {
                gpxContent.append(String.format(Locale.US,
                        "    <trkpt lat=\"%f\" lon=\"%f\"><time>%s</time></trkpt>\n",
                        point.latitude, point.longitude,
                        sdf.format(new Date(point.timestamp))));
            }

            gpxContent.append("  </trkseg></trk>\n");
            gpxContent.append("</gpx>\n");

            byte[] encryptedData = encryptGPXContent(gpxContent.toString(), "WanderTrack123456");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(encryptedData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importGpxFromFile(String filePath) {
        //导入GPX文件，加密后存储进数据库
        try {
            List<LocationPoint> points = parseGpx(filePath);
            File encryptedFile = new File(MyApplication.getContext().getExternalFilesDir(null), new File(filePath).getName() + ".enc");
            encryptAndSaveTrack(points, encryptedFile);
            if (encryptedFile != null) {
                insertImportedTrackToDatabase(points, encryptedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importKmlFromFile(String filePath) {
        //导入KML文件，加密后存储进数据库
        try {
            List<LocationPoint> points = parseKml(filePath);
            File encryptedFile = new File(MyApplication.getContext().getExternalFilesDir(null), new File(filePath).getName() + ".enc");
            encryptAndSaveTrack(points, encryptedFile);
            if (encryptedFile != null) {
                insertImportedTrackToDatabase(points, encryptedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * 传入gpx文件，仅读取其经纬度坐标，放入List<LatLng>对象中返回。作用是方便放入地图的overlay中展示，同时也由于parseGpx是类的私有方法，不能在外部调用
     *
     * @param filePath 文件路径
     * @return 文件内存放的坐标，用Google Maps Android API中的类返回
     * @throws IOException            文件读取错误
     * @throws XmlPullParserException XMLPullParser错误
     */
    public List<LatLng> gpxToList(String filePath) throws IOException, XmlPullParserException {
        List<LatLng> points = new ArrayList<>();
        InputStream inputStream = null;

        try {
            File gpxFile = new File(filePath);
            inputStream = new FileInputStream(gpxFile);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if ("trkpt".equals(parser.getName())) {
                        String lat = parser.getAttributeValue(null, "lat");
                        String lon = parser.getAttributeValue(null, "lon");
                        if (lat != null && lon != null) {
                            points.add(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
                        }
                    }
                }
                eventType = parser.next();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return points;
    }
}
