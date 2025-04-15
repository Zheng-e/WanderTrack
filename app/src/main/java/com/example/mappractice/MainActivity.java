package com.example.mappractice;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.HeatMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.google.android.material.navigation.NavigationView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    MapView mapView;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private LocationListener locationListener;
//    Button btnPoint;
//    Button btnLine;
//    Button btnPoly;
//    Button btnTest;
    Button btnStart;
    Button btnStop;
    Button btnSave;
    String my_device_id;
    Double my_longitude;
    Double my_latitude;
    List<LatLng> line = new ArrayList<>();
    Stack<LatLng> linePoints = new Stack<>();
    PolygonOptions polygonOptions;
    private TrackRecorder trackRecorder;
    PolylineOptions visibleTrack;
    List<LatLng> pointSets = new ArrayList<>();
    Polyline visibleLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SDKInitializer.setAgreePrivacy(this.getApplicationContext(), true);
        SDKInitializer.initialize(this.getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_load_path) {
                    startActivity(new Intent(MainActivity.this, LoadPathActivity.class));
                } else if (id == R.id.nav_achievements) {
                    startActivity(new Intent(MainActivity.this, AchievementActivity.class));
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.showZoomControls(true);
        trackRecorder = new TrackRecorder();
        BaiduMap mBaiduMap = mapView.getMap();

//        btnPoint = findViewById(R.id.button_point);
//        btnLine = findViewById(R.id.button_line);
//        btnPoly = findViewById(R.id.button_poly);
//        btnTest = findViewById(R.id.button_test);
        btnStart = findViewById(R.id.button_start);
        btnStop = findViewById(R.id.button_stop);
        btnSave = findViewById(R.id.button_save);

        //设置是否显示比例尺控件
        mapView.showScaleControl(false);
        //设置是否显示缩放控件
        mapView.showZoomControls(false);
        // 删除百度地图LoGo
        mapView.removeViewAt(1);


        //开始记录轨迹
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mBaiduMap.clear();
//                visibleLine = null;
//                visibleTrack = null;
                pointSets.clear();
                resetOverlay(mBaiduMap);
                trackRecorder.startTracking();
                //startTracking测试中，不确定有无Bug
//                startTracking(mBaiduMap);
                LatLng point1 = new LatLng(31.2304, 121.4737);
                LatLng point2 = new LatLng(31.2310, 121.4745);
                LatLng point3 = new LatLng(31.2320, 121.4755);
                updateTrack(point1, mBaiduMap);
                updateTrack(point2, mBaiduMap);
                updateTrack(point3, mBaiduMap);
                toFirstLocation(mBaiduMap);
                Toast.makeText(MainActivity.this, "开始记录轨迹", Toast.LENGTH_SHORT).show();
            }
        });

        //停止记录轨迹
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackRecorder.pauseTracking();
                //模拟加入了一个点
                LatLng testPoint = new LatLng(31.2500, 121.4920);
                updateTrack(testPoint, mBaiduMap);
                Toast.makeText(MainActivity.this, "停止记录轨迹", Toast.LENGTH_SHORT).show();
            }
        });

        //保存轨迹
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackRecorder.stopTrackingAndSaveGpx();
                stopRecording();
                Toast.makeText(MainActivity.this, "保存轨迹", Toast.LENGTH_SHORT).show();
            }
        });

//        btnPoint.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBaiduMap.clear();
//                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng latLng) {
//                        double latitude = latLng.latitude;
//                        double longitude = latLng.longitude;

        //添加点

//                        LatLng position = new LatLng(latitude, longitude);
//                        DotOptions dotOptions = new DotOptions()
//                                .center(position)
//                                .color(0xaaaaaacc)
//                                .radius(10);
//                        mBaiduMap.addOverlay(dotOptions);
//                        setMyMarker("0",latitude,longitude,mBaiduMap);
//                    }
//
//                    @Override
//                    public void onMapPoiClick(MapPoi mapPoi) {
//
//                    }
//                });
//            }
//        });

//        btnLine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBaiduMap.clear();
//                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng latLng) {
//                        double latitude = latLng.latitude;
//                        double longitude = latLng.longitude;
//
//                        //添加线
//                        LatLng position = new LatLng(latitude, longitude);
//                        linePoints.push(position);
//                        if ((linePoints.size() % 2 == 0) && (!linePoints.isEmpty())) {
//                            line.add(linePoints.pop());
//                            line.add(linePoints.pop());
//                            PolylineOptions polylineOptions = new PolylineOptions()
//                                    .points(line)
//                                    .width(10)
//                                    .zIndex(5);
//                            mBaiduMap.addOverlay(polylineOptions);
//                            line.clear();
//                        }
//                    }
//
//                    @Override
//                    public void onMapPoiClick(MapPoi mapPoi) {
//
//                    }
//                });
//            }
//        });
//
//        btnPoly.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mBaiduMap.clear();
//                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
//                    @Override
//                    public void onMapClick(LatLng latLng) {
//                        double latitude = latLng.latitude;
//                        double longitude = latLng.longitude;
//
//                        //添加多边形
//                        LatLng position = new LatLng(latitude, longitude);
//                        line.add(position);
//                        if (line.size() >= 3) {
//                            mBaiduMap.clear();
//                            polygonOptions = new PolygonOptions()
//                                    .points(line)
//                                    .fillColor(0xaaaaaacc)
//                                    .zIndex(5);
//                            mBaiduMap.addOverlay(polygonOptions);
//                        }
//                    }
//
//                    @Override
//                    public void onMapPoiClick(MapPoi mapPoi) {
//
//                    }
//                });
//            }
//        });
//
//        btnTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getDeviceInfo(mBaiduMap);
//            }
//        });

//        new Thread(() -> {
//            try {
//                trackRecorder.startTracking();
//                Thread.sleep(1000);
//                trackRecorder.addLocationPoint(new LocationPoint(31.2304, 121.4737, System.currentTimeMillis()));
//                Thread.sleep(1000);
//                trackRecorder.addLocationPoint(new LocationPoint(31.2310, 121.4745, System.currentTimeMillis()));
//                Thread.sleep(1000);
//                trackRecorder.addLocationPoint(new LocationPoint(31.2320, 121.4755, System.currentTimeMillis()));
//                Thread.sleep(1000);
//                trackRecorder.addLocationPoint(new LocationPoint(31.2500, 121.4920, System.currentTimeMillis()));
//                trackRecorder.stopTrackingAndSaveGpx();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String info = "longitude:" + marker.getExtraInfo().getString("longitude")
                        + "\nlatitude:" + marker.getExtraInfo().getString("latitude")
                        + "\n可添加备注并保存到DB";
                showInputDialog(info, marker.getExtraInfo().getString("longitude"), marker.getExtraInfo().getString("latitude"));
                return false;
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            String selectedTrack = intent.getStringExtra("selectedTrack");
            if (selectedTrack != null) {
                //加载轨迹
                TrackImporter trackImporter = new TrackImporter();
                System.out.println("try to load " + selectedTrack);
                String gpxDirectory = getExternalFilesDir(null).getAbsolutePath();
                File gpxFile = new File(gpxDirectory, selectedTrack);//使用file对象智能拼接路径和处理分隔符
                try {
                    pointSets = trackImporter.gpxToList(gpxFile.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (XmlPullParserException e) {
                    throw new RuntimeException(e);
                }
                resetOverlay(mBaiduMap);
                toFirstLocation(mBaiduMap);
                System.out.println("load finished");
                Toast.makeText(this, "加载轨迹: " + selectedTrack, Toast.LENGTH_SHORT).show();
            }
        }

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                //根据地图的缩放状态调整路径
                float zoomLevel = mapStatus.zoom;
                //热力图方案废案
//                HeatMap heatMap;
//                if(zoomLevel < 6){
//                    List<LatLng> heatmapPoints = new ArrayList<>();
//                    for (LatLng point : pointSets) {
//                        // 在路径点周围生成密集虚拟点
//                        for (int i=0; i<20; i++) {
//                            double offsetLat = point.latitude + (Math.random()-0.5)*0.2;
//                            double offsetLng = point.longitude + (Math.random()-0.5)*0.2;
//                            heatmapPoints.add(new LatLng(offsetLat, offsetLng));
//                        }
//                    }
//                    HeatMap.Builder builder = new HeatMap.Builder()
//                            .data(heatmapPoints)
//                            .gradient(HeatMap.DEFAULT_GRADIENT);
//                    heatMap = builder.build();
//                    mBaiduMap.addHeatMap(heatMap);
//                }else{
//                    resetOverlay(mBaiduMap);
//                }
                float dynamicWidth = zoomLevel * 1.5f;
                visibleLine.setWidth((int) dynamicWidth);
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        });
    }

    /**
     * 为当前已有的路径加入一个新的路径点
     *
     * @param newPoint 需要加入的点
     * @param mBaiduMap 地图控件
     */
    public void updateTrack(LatLng newPoint, BaiduMap mBaiduMap) {
        pointSets.add(newPoint);
        LocationPoint point = new LocationPoint(newPoint.latitude, newPoint.longitude, System.currentTimeMillis());
        trackRecorder.addLocationPoint(point);
        resetOverlay(mBaiduMap);
    }

    /**
     * 重置所有的地图覆盖物，同时只保留最原始的路径
     *
     * @param mBaiduMap 地图控件
     */
    public void resetOverlay(BaiduMap mBaiduMap) {
        mBaiduMap.clear();
        if(visibleLine != null){
            visibleLine.remove();
        }
        if(pointSets.size() >= 2){
            visibleTrack = new PolylineOptions()
                    .points(pointSets) // 添加坐标点
                    .color(Color.RED) // 设置颜色
                    .width(10) // 线宽
                    .dottedLine(true); // 是否虚线（可选）
            visibleLine = (Polyline) mBaiduMap.addOverlay(visibleTrack);
        }
    }

    /**
     * 孩子们，我才是真正的startTracking
     *
     * @param mBaiduMap 地图控件
     */
    public void startTracking(BaiduMap mBaiduMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng myLatPoint = trackRecorder.convertToBD09(location);
                updateTrack(myLatPoint, mBaiduMap);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
    }

    /**
     * 孩子们，我才是真正的stopRecording
     */
    public void stopRecording() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    /**
     * 将地图视角移动到pointSets中第一个点的位置。用途是导入地图和地图定位时移动视角到导入的位置或实际所在位置
     *
     * @param mBaiduMap 地图控件
     */
    public void toFirstLocation(BaiduMap mBaiduMap){
        if(!pointSets.isEmpty()){
            float currentZoom = 16;
            MapStatus mapStatus = mBaiduMap.getMapStatus();
            if(mapStatus != null){
                currentZoom = mapStatus.zoom;
            }
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLngZoom(pointSets.get(0),currentZoom);
            mBaiduMap.animateMapStatus(update);
        }
    }

//
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(!(my_longitude == 0 && my_latitude == 0)){
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            sendInfo(my_device_id,my_latitude,my_longitude);
//                        }
//                    }).start();
//                }else {
//                    System.out.println("未设置设备信息，无法发送");
//                }
//            }
//        });


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                JSONObject resultJson = infoGet();
//                try {
//                    for (int i = 1; i <= Integer.parseInt(resultJson.getString("count")); i++){
//                        LatLng point = new LatLng(resultJson.getDouble("latitude"+i), resultJson.getDouble("longitude"+i));
//                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.marker_a);
//                        OverlayOptions overlayOptions = new MarkerOptions().position(point).icon(bitmapDescriptor);
//                        Marker marker=(Marker)mBaiduMap.addOverlay(overlayOptions);
//
//                        Bundle bundle = new Bundle();
//                        bundle.putString("device_id",resultJson.getString("device_id"+i));
//                        bundle.putString("speed",resultJson.getString("speed"+i));
//                        bundle.putString("direction",resultJson.getString("direction"+i));
//                        bundle.putString("location",resultJson.getString("location"+i));
//                        bundle.putString("gps_time",resultJson.getString("gps_time"+i));
//                        bundle.putString("recv_time",resultJson.getString("recv_time"+i));
//                        marker.setExtraInfo(bundle);
//                    }
//
//                    LatLng point = new LatLng(resultJson.getDouble("latitude1"), resultJson.getDouble("longitude1"));
//                    MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
//                    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
//                    mBaiduMap.setMapStatus(mMapStatusUpdate);
//
//                }catch (JSONException jsonException){
//                    jsonException.printStackTrace();
//                }
//            }
//        }).start();

//        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                String s = String.valueOf(marker.getExtraInfo().get("device_id"));
//                Toast.makeText(getApplicationContext(), s + "被点击了！", Toast.LENGTH_SHORT).show();
//                Toast toast=new Toast(getApplicationContext());
//                toast.setText("点击了："+s);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.TOP, 200, 190);
//                toast.show();
//                String info = "device_id: " + marker.getExtraInfo().getString("device_id")
//                        + "\nspeed: " + marker.getExtraInfo().getString("speed")
//                        + "\ndirection: " + marker.getExtraInfo().getString("direction")
//                        + "\nlocation: " + marker.getExtraInfo().getString("location")
//                        + "\ngps_time: " + marker.getExtraInfo().getString("gps_time")
//                        + "\nrecv_time: " + marker.getExtraInfo().getString("recv_time");
//                new AlertDialog.Builder(MainActivity.this).setTitle("详细信息")
//                        .setMessage(info)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        }).show();
//                return false;
//            }
//        });

    //        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                String s = String.valueOf(marker.getExtraInfo().get("device_id"));
//                Toast.makeText(getApplicationContext(), s + "被点击了！", Toast.LENGTH_SHORT).show();
//                Toast toast=new Toast(getApplicationContext());
//                toast.setText("点击了："+s);
//                toast.setDuration(Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.TOP, 200, 190);
//                toast.show();
//                String info = "device_id: " + marker.getExtraInfo().getString("device_id")
//                        + "\nlongitude: " + marker.getExtraInfo().getString("longitude")
//                        + "\nlatitude: " + marker.getExtraInfo().getString("latitude");
//                new AlertDialog.Builder(MainActivity.this).setTitle("详细信息")
//                        .setMessage(info)
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        }).show();
//                return false;
//            }
//        });
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//
    private void getDeviceInfo(BaiduMap mBaiduMap) {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Double Lon = location.getLongitude();
                Double Lat = location.getLatitude();
                String Altitude = location.getAltitude() + "";
                String Direction = location.getBearing() + "";
                String locationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(location.getTime()));

                String info = "long:" + Lon + " lat:" + Lat;
                runOnUiThread(() -> Toast.makeText(MainActivity.this, info, Toast.LENGTH_SHORT).show());
                setMyMarker(deviceId, Lat, Lon, mBaiduMap);
                my_device_id = deviceId;
                my_longitude = Lon;
                my_latitude = Lat;

//                locationManager.removeUpdates(this); // 更新后停止获取
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        });
    }

    //
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                BaiduMap map = null;
//                getDeviceInfo(map);
//            } else {
//                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private JSONObject infoGet(){
//        Connection connection;
//        Statement statement;
//        JSONObject resultJson;
//        try{
//            Class.forName("com.mysql.jdbc.Driver");
//            Log.d("调试输出", "加载了数据库驱动");
//
//            Log.d("调试输出", "尝试操作数据库");
//            String connStr = "jdbc:mysql://1.14.20.210:3366/ydbc2024?user=Administrator&password=XWClassroom20202023&userUnicode=true&characterEncoding=UTF-8";
//            connection = DriverManager.getConnection(connStr);
//            Log.d("调试输出", "准备statement, connection是"+connStr);
//            statement = connection.createStatement();
//            Log.d("调试输出", "已连接上数据库");
//
//            String sql = "select * from gps_realtime";
//            ResultSet resultSet = statement.executeQuery(sql);
//            resultJson = new JSONObject();
//            int count = 0;
//            while (resultSet.next()){
//                count++;
//                resultJson.put("id"+count,resultSet.getString("id"));
//                resultJson.put("device_id"+count,resultSet.getString("device_id"));
//                resultJson.put("longitude"+count,resultSet.getString("longitude"));
//                resultJson.put("latitude"+count,resultSet.getString("latitude"));
//                resultJson.put("speed"+count,resultSet.getString("speed"));
//                resultJson.put("direction"+count,resultSet.getString("direction"));
//                resultJson.put("location"+count,resultSet.getString("location"));
//                resultJson.put("gps_time"+count,resultSet.getString("gps_time"));
//                resultJson.put("recv_time"+count,resultSet.getString("recv_time"));
//            }
//            resultJson.put("count",count);
//            statement.close();
//            connection.close();
//            return resultJson;
//        } catch (SQLException sqlException){
//            Log.d("调试输出", "操作数据库出错");
//            sqlException.printStackTrace();
//        } catch (JSONException jsonException) {
//            jsonException.printStackTrace();
//        } catch (ClassNotFoundException classNotFoundException){
//            classNotFoundException.printStackTrace();
//        }
//        resultJson = new JSONObject();
//        return resultJson;
//    }
//
    private void setMyMarker(String deviceId, Double latitude, Double longitude, BaiduMap mBaiduMap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LatLng point = new LatLng(latitude, longitude);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.dot_red_icon);
                OverlayOptions overlayOptions = new MarkerOptions().position(point).icon(bitmapDescriptor);
                Marker marker = (Marker) mBaiduMap.addOverlay(overlayOptions);

                Bundle bundle = new Bundle();
                bundle.putString("device_id", deviceId);
                bundle.putString("longitude", String.valueOf(longitude));
                bundle.putString("latitude", String.valueOf(latitude));
                marker.setExtraInfo(bundle);

//                MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
//                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
//                mBaiduMap.setMapStatus(mMapStatusUpdate);
            }
        }).start();
    }

    private void showInputDialog(String msg, String longitude, String latitude) {
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_edittext,
                (ViewGroup) findViewById(R.id.item_lin_ed));
        EditText inputText = layout.findViewById(R.id.item_ed);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this).setTitle("详情").setView(layout).setMessage(msg)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editInfo = inputText.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                saveMarkerInDB(editInfo, longitude, latitude);
                            }
                        }).start();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        inputDialog.create().show();
    }

    private void saveMarkerInDB(String msg, String longitude, String latitude) {
        String sqlAdd = String.format("insert into test(longitude, latitude, info) values('%s', '%s', '%s')", longitude, latitude, msg);
        Connection connection;
        Statement statement;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connStr = "jdbc:mysql://1.14.20.210:3366/demo?user=Administrator&password=XWClassroom20202023&userUnicode=true&characterEncoding=UTF-8";
            connection = DriverManager.getConnection(connStr);
            statement = connection.createStatement();
            System.out.println("连接数据库成功");

            statement.execute(sqlAdd);
            statement.close();
            connection.close();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
//
//    private void sendInfo(String deviceId, Double latitude,Double longitude){
//        Connection connection;
//        Statement statement;
//        try{
//            Class.forName("com.mysql.jdbc.Driver");
//            Log.d("调试输出", "加载了数据库驱动");
//
//            Log.d("调试输出", "尝试操作数据库");
//            String connStr = "jdbc:mysql://1.14.20.210:3366/test?user=Administrator&password=XWClassroom20202023&userUnicode=true&characterEncoding=UTF-8";
//            connection = DriverManager.getConnection(connStr);
//            Log.d("调试输出", "准备statement, connection是"+connStr);
//            statement = connection.createStatement();
//            Log.d("调试输出", "已连接上数据库");
//
//            String sql = String.format("insert into cd_db(device_id, longitude, latitude) values('%s', %f, %f)",deviceId, latitude, longitude);
//            statement.execute(sql);
//
//            statement.close();
//            connection.close();
//        } catch (SQLException sqlException){
//            Log.d("调试输出", "操作数据库出错");
//            sqlException.printStackTrace();
//        } catch (ClassNotFoundException classNotFoundException){
//            classNotFoundException.printStackTrace();
//        }


//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        System.out.println("onTouch");
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//            Log.d("touch","at "+x+" "+y);
//        }
//        return super.onTouchEvent(event);
//    }

}