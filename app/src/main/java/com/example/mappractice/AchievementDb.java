package com.example.mappractice;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

public class AchievementDb extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "achievements.db";
    private static final int DATABASE_VERSION = 2;

    // 表结构
    public static class AchievementEntry {
        public static final String TABLE_NAME = "visited_locations";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PROVINCE = "province";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_CONTINENT = "continent";

//        下面这些有点复杂，暂时先不加入
//        public static final String COLUMN_LATITUDE = "latitude";
//        public static final String COLUMN_LONGITUDE = "longitude";
//        public static final String COLUMN_IS_NIGHT = "is_night";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + AchievementEntry.TABLE_NAME + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    AchievementEntry.COLUMN_USER_ID + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_PROVINCE + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_CITY + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_COUNTRY + " TEXT," +
                    AchievementEntry.COLUMN_CONTINENT + " TEXT," +
                    "UNIQUE (" + AchievementEntry.COLUMN_USER_ID + ", " +
                    AchievementEntry.COLUMN_CITY + ")" +
//                    AchievementEntry.COLUMN_LATITUDE + " REAL," +
//                    AchievementEntry.COLUMN_LONGITUDE + " REAL," +
//                    AchievementEntry.COLUMN_IS_NIGHT + " INTEGER DEFAULT 0," +
//                    AchievementEntry.COLUMN_TIMESTAMP + " INTEGER" +
                    ");";

    public AchievementDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 版本迁移
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS " + AchievementEntry.TABLE_NAME);
            onCreate(db);
        }
    }
    // 所有调用 getWritableDatabase() 或 getReadableDatabase() 时需传入统一密码，例如 "WanderTrack"
}
