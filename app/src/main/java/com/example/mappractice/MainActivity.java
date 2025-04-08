package com.example.mappractice;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

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
    MapView mapView;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    Button btnPoint;
    Button btnLine;
    Button btnPoly;
    Button btnTest;
    String my_device_id;
    Double my_longitude;
    Double my_latitude;
    List<LatLng> line = new ArrayList<>();
    Stack<LatLng> linePoints = new Stack<>();
    PolygonOptions polygonOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        SDKInitializer.setAgreePrivacy(this.getApplicationContext(), true);
        SDKInitializer.initialize(this.getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.showZoomControls(true);
        BaiduMap mBaiduMap = mapView.getMap();

        btnPoint = findViewById(R.id.button_point);
        btnLine = findViewById(R.id.button_line);
        btnPoly = findViewById(R.id.button_poly);
        btnTest = findViewById(R.id.button_test);

        //设置是否显示比例尺控件
        mapView.showScaleControl(false);
        //设置是否显示缩放控件
        mapView.showZoomControls(false);
        // 删除百度地图LoGo
        mapView.removeViewAt(1);

        btnPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBaiduMap.clear();
                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;

                        //添加点

//                        LatLng position = new LatLng(latitude, longitude);
//                        DotOptions dotOptions = new DotOptions()
//                                .center(position)
//                                .color(0xaaaaaacc)
//                                .radius(10);
//                        mBaiduMap.addOverlay(dotOptions);
                        setMyMarker("0", latitude, longitude, mBaiduMap);
                    }

                    @Override
                    public void onMapPoiClick(MapPoi mapPoi) {

                    }
                });
            }
        });

        btnLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBaiduMap.clear();
                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;

                        //添加线
                        LatLng position = new LatLng(latitude, longitude);
                        linePoints.push(position);
                        if ((linePoints.size() % 2 == 0) && (!linePoints.isEmpty())) {
                            line.add(linePoints.pop());
                            line.add(linePoints.pop());
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .points(line)
                                    .width(10)
                                    .zIndex(5);
                            mBaiduMap.addOverlay(polylineOptions);
                            line.clear();
                        }
                    }

                    @Override
                    public void onMapPoiClick(MapPoi mapPoi) {

                    }
                });
            }
        });

        btnPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBaiduMap.clear();
                mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        double latitude = latLng.latitude;
                        double longitude = latLng.longitude;

                        //添加多边形
                        LatLng position = new LatLng(latitude, longitude);
                        line.add(position);
                        if (line.size() >= 3) {
                            mBaiduMap.clear();
                            polygonOptions = new PolygonOptions()
                                    .points(line)
                                    .fillColor(0xaaaaaacc)
                                    .zIndex(5);
                            mBaiduMap.addOverlay(polygonOptions);
                        }
                    }

                    @Override
                    public void onMapPoiClick(MapPoi mapPoi) {

                    }
                });
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceInfo(mBaiduMap);
            }
        });

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
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

//                System.out.println("信息："+deviceId+" "+Lon+" "+Lat+" "+Altitude+" "+Direction+" "+locationTime);
//                Toast.makeText(this, "这是一个Toast示例", Toast.LENGTH_SHORT).show();
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
        System.out.println("running dialog");
        LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_edittext,
                (ViewGroup) findViewById(R.id.item_lin_ed));
        EditText inputText = layout.findViewById(R.id.item_ed);
//        AlertDialog.Builder inputDialog = new AlertDialog.Builder(this).setTitle("详情").setView(layout).setMessage(msg)
//                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        String editInfo = inputText.getText().toString();
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                saveMarkerInDB(editInfo,longitude,latitude);
//                            }
//                        }).start();
//                    }
//                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {}
//                });
//        inputDialog.create().show();

        final AlertDialog inputDialog = new AlertDialog.Builder(this).setTitle("详情")
                .setView(layout)
                .setMessage(msg)
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        System.out.println("basic settings done");
        inputDialog.setOnShowListener(d -> {
            System.out.println("fade in ready");
            View dialogDecorView = inputDialog.getWindow().getDecorView();
            UIAnimationTool.dialogFadeIn(dialogDecorView, 300);
            Button positiveBtn = inputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//            Button negativeBtn = inputDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            positiveBtn.setOnClickListener(view -> {
                System.out.println("fade out ready");
                new Thread(() -> {
                    System.out.println("code test");
                    runOnUiThread(() -> {
                        if (inputDialog.isShowing()){
                            inputDialog.dismiss();
                        }else {
                            System.out.println("error occurred in dialog dismissing");
                        }
                    });
                }).start();
                System.out.println("saved in database");
//                animateDialogOut(inputDialog); // 封装淡出逻辑
            });
        });

        System.out.println("before showing");
//        inputDialog.getWindow().getDecorView().setVisibility(View.INVISIBLE);
        inputDialog.show();
    }

//    private void animateDialogOut(AlertDialog dialog) {
//        View decorView = dialog.getWindow().getDecorView();
//        UIAnimationTool.dialogFadeOut(decorView, 300, dialog);
//    }

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