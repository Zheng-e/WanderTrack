package com.example.mappractice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

public class ReverseGeocoder {

    private GeoCoder mGeoCoder;
    private Context context;
    private static final String CACHE_KEY = "LocationCache";

    public void initGeoCoder(Context context) {
        // 初始化百度SDK
        this.context = context.getApplicationContext();
        SDKInitializer.initialize(this.context);
        mGeoCoder = GeoCoder.newInstance();
    }

    //调用用这个
    public void getAddressFromLatLng(LatLng latLng, ReverseGeoCallback callback) {
        // 逆地理编码请求
        ReverseGeoCodeOption option = new ReverseGeoCodeOption();
        option.location(new com.baidu.mapapi.model.LatLng(latLng.latitude, latLng.longitude));

        // 设置结果监听器
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null) {
                    Log.e("ReverseGeo", "Result is null");
                    callback.onAddressResolved(null, null);
                    return;
                }
                if (result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.e("ReverseGeo", "错误码: " + result.error);
                }

                if (result == null || result.getAddress() == null) {
                    callback.onAddressResolved(null, null);
                    return;
                }

                String province = result.getAddressDetail().province;
                String city = result.getAddressDetail().city;

                // 缓存结果
                cacheLocation(province, city);
                callback.onAddressResolved(province, city);
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}
        });

        mGeoCoder.reverseGeoCode(option);
    }


    private void cacheLocation(String province, String city) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putString(CACHE_KEY, province + "|" + city)
                .apply();
    }

    public interface ReverseGeoCallback {
        void onAddressResolved(String province, String city);
    }
}

