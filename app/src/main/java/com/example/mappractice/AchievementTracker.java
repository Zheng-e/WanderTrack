package com.example.mappractice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.util.Log;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;

public class AchievementTracker {
    private final AchievementDb dbHelper;

    public AchievementTracker(Context context) {
        dbHelper = new AchievementDb(context);
    }

    // 测试用
    public void clearDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(AchievementDb.AchievementEntry.TABLE_NAME, null, null);
    }

    public void addLocation(String userID, String province, String city, String country, String continent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
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

//        测试用
//        long result = db.insertWithOnConflict(
//                AchievementDb.AchievementEntry.TABLE_NAME,
//                null,
//                values,
//                SQLiteDatabase.CONFLICT_IGNORE
//        );
//
//        if (result == -1) {
//            Log.w("DB", "唯一约束冲突: user=" + userID +
//                    ", city=" + city + ", province=" + province);
//            // 检查数据库中是否已有该记录
//            checkExistingRecord(userID, city);
//        }


    }
//    private void checkExistingRecord(String userId, String city) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
//        String query = "SELECT * FROM " + AchievementDb.AchievementEntry.TABLE_NAME +
//                " WHERE " + AchievementDb.AchievementEntry.COLUMN_USER_ID + " = ?" +
//                " AND " + AchievementDb.AchievementEntry.COLUMN_CITY + " = ?";
//        try (Cursor cursor = db.rawQuery(query, new String[]{userId, city})) {
//            if (cursor.moveToFirst()) {
//                Log.w("DB", "冲突记录存在: user=" + userId +
//                        ", city=" + cursor.getString(3));
//            } else {
//                Log.e("DB", "冲突记录不存在（此处不应该发生）");
//            }
//        }
//    }


    public int getCityCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_CITY);
    }

    public int getProvinceCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_PROVINCE);
    }
    public int getCountryCount() {
        return queryCount(AchievementDb.AchievementEntry.COLUMN_COUNTRY);
    }

    private int queryCount(String column) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(DISTINCT " + column + ") FROM " +
                AchievementDb.AchievementEntry.TABLE_NAME +
                " WHERE " + AchievementDb.AchievementEntry.COLUMN_USER_ID + " = 'test_user_001'";

        try (Cursor cursor = db.rawQuery(query, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

}
