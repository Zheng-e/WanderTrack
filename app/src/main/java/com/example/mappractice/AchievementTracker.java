package com.example.mappractice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;


// 查询数据库逻辑
public class AchievementTracker {
    private final AchievementDb dbHelper;
    private static final String DEFAULT_USER_ID = "test_user_001";  // test

    public AchievementTracker(Context context) {
        dbHelper = new AchievementDb(context);
    }

    // 测试用
    public void clearDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase("your_password");
        db.delete(AchievementDb.AchievementEntry.TABLE_NAME, null, null);
    }

    public void addLocation(String userID, String province, String city, String country, String continent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase("your_password");
        ContentValues values = new ContentValues();
        values.put(AchievementDb.AchievementEntry.COLUMN_USER_ID, userID);
        values.put(AchievementDb.AchievementEntry.COLUMN_PROVINCE, province);
        values.put(AchievementDb.AchievementEntry.COLUMN_CITY, city);
        values.put(AchievementDb.AchievementEntry.COLUMN_COUNTRY, country);
        values.put(AchievementDb.AchievementEntry.COLUMN_CONTINENT, continent);

        db.insertWithOnConflict(
                AchievementDb.AchievementEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );

    }


    public void addLocation(String userID, String province, String city,
                            String country, String continent, long timestamp,
                            double latitude, double longitude, boolean isInternational) {
        SQLiteDatabase db = dbHelper.getWritableDatabase("your_password");
        ContentValues values = new ContentValues();
        values.put(AchievementDb.AchievementEntry.COLUMN_USER_ID, userID);
        values.put(AchievementDb.AchievementEntry.COLUMN_PROVINCE, province);
        values.put(AchievementDb.AchievementEntry.COLUMN_CITY, city);
        values.put(AchievementDb.AchievementEntry.COLUMN_COUNTRY, country);
        values.put(AchievementDb.AchievementEntry.COLUMN_CONTINENT, continent);
        values.put(AchievementDb.AchievementEntry.COLUMN_TIMESTAMP, timestamp);
        values.put(AchievementDb.AchievementEntry.COLUMN_LATITUDE, latitude);
        values.put(AchievementDb.AchievementEntry.COLUMN_LONGITUDE, longitude);
        values.put(AchievementDb.AchievementEntry.COLUMN_IS_INTERNATIONAL, isInternational ? 1 : 0);

        db.insertWithOnConflict(
                AchievementDb.AchievementEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    public void addLocation(String userID, String province, String city,
                            String country, String continent) {
        addLocation(userID, province, city, country, continent,
                System.currentTimeMillis(), 0.0, 0.0);
    }

    private int queryCount(String column) {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT COUNT(DISTINCT " + column + ") FROM " +
                AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementDb.AchievementEntry.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{DEFAULT_USER_ID})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    // ==========核心统计方法==========

    public int getCityCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_CITY);
    }

    public int getProvinceCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_PROVINCE);
    }

    public int getCountryCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_COUNTRY);
    }

    public int getContinentCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_CONTINENT);
    }

    // 按大洲统计城市数量
    public int getCityCountByContinent(String continent) {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT COUNT(DISTINCT " + AchievementDb.AchievementEntry.COLUMN_CITY + ") " +
                "FROM " + AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementDb.AchievementEntry.COLUMN_CONTINENT + " = ?" +
                " AND " + AchievementDb.AchievementEntry.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{continent, DEFAULT_USER_ID})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    // 统计不来自homeCountry的city数量
    public int getInternationalCityCount(String homeCountry) {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT COUNT(DISTINCT " + AchievementDb.AchievementEntry.COLUMN_CITY + ") " +
                "FROM " + AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementDb.AchievementEntry.COLUMN_COUNTRY + " != ?" +
                " AND " + AchievementDb.AchievementEntry.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{homeCountry, DEFAULT_USER_ID})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    // ==========特殊成就统计==========

    public int getNightVisitCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT COUNT(*) FROM " + AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE (" + AchievementDb.AchievementEntry.COLUMN_TIMESTAMP + " % 86400) " +
                "BETWEEN 79200 AND 21600"; // 22:00-6:00
        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public boolean checkPolarRegionVisit() {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT 1 FROM " + AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE ABS(" + AchievementDb.AchievementEntry.COLUMN_LATITUDE + ") >= 66.5 LIMIT 1";
        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.getCount() > 0;
        }
    }

    // ==========等级/称号操作==========

    public void updateUserLevel(int level, String levelName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase("your_password");
        ContentValues values = new ContentValues();
        values.put(AchievementDb.LevelEntry.COLUMN_USER_ID, DEFAULT_USER_ID);
        values.put(AchievementDb.LevelEntry.COLUMN_LEVEL, level);
        values.put(AchievementDb.LevelEntry.COLUMN_LEVEL_NAME, levelName);

        db.insertWithOnConflict(
                AchievementDb.LevelEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public void unlockTitle(String titleId, String titleName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase("your_password");
        ContentValues values = new ContentValues();
        values.put(AchievementDb.TitleEntry.COLUMN_USER_ID, DEFAULT_USER_ID);
        values.put(AchievementDb.TitleEntry.COLUMN_TITLE_ID, titleId);
        values.put(AchievementDb.TitleEntry.COLUMN_TITLE_NAME, titleName);

        db.insertWithOnConflict(
                AchievementDb.TitleEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        );
    }

    // ==========数据查询接口==========

    public int getUserCurrentLevel() {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT " + AchievementDb.LevelEntry.COLUMN_LEVEL +
                " FROM " + AchievementDb.LevelEntry.TABLE_NAME +
                " WHERE " + AchievementDb.LevelEntry.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{DEFAULT_USER_ID})) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public String getCurrentLevelName() {
        SQLiteDatabase db = dbHelper.getReadableDatabase("your_password");
        String query = "SELECT " + AchievementDb.LevelEntry.COLUMN_LEVEL_NAME +
                " FROM " + AchievementDb.LevelEntry.TABLE_NAME +
                " WHERE " + AchievementDb.LevelEntry.COLUMN_USER_ID + " = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{DEFAULT_USER_ID})) {
            return cursor.moveToFirst() ? cursor.getString(0) : "未解锁";
        }
    }

    public AchievementDb getDbHelper() {
        return this.dbHelper;
    }

}
