package com.example.mappractice;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.Context;

// 三个数据库：访问地点地理表、等级记录表、称号记录表
public class AchievementDb extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "achievements.db";
    private static final int DATABASE_VERSION = 3;

    public static class AchievementEntry {
        public static final String TABLE_NAME = "visited_locations";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_PROVINCE = "province";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_CONTINENT = "continent";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_IS_INTERNATIONAL = "is_international";
    }

    public static class LevelEntry {
        public static final String TABLE_NAME = "user_levels";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_LEVEL = "level";
        public static final String COLUMN_LEVEL_NAME = "level_name";
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + AchievementEntry.TABLE_NAME + " (" +
                    "_id INTEGER PRIMARY KEY," +
                    AchievementEntry.COLUMN_USER_ID + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_PROVINCE + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_CITY + " TEXT NOT NULL," +
                    AchievementEntry.COLUMN_COUNTRY + " TEXT," +
                    AchievementEntry.COLUMN_CONTINENT + " TEXT," +
                    AchievementEntry.COLUMN_LATITUDE + " REAL," +
                    AchievementEntry.COLUMN_LONGITUDE + " REAL," +
                    AchievementEntry.COLUMN_TIMESTAMP + " INTEGER," +
//                    AchievementEntry.COLUMN_IS_INTERNATIONAL + " INTEGER DEFAULT 0," +
                    "UNIQUE (" + AchievementEntry.COLUMN_USER_ID + ", " +
                    AchievementEntry.COLUMN_CITY + ")" +
                    ");";

    private static final String SQL_CREATE_LEVELS =
    "CREATE TABLE IF NOT EXISTS " + LevelEntry.TABLE_NAME + " (" +
            LevelEntry.COLUMN_USER_ID + " TEXT PRIMARY KEY," +
            LevelEntry.COLUMN_LEVEL + " INTEGER NOT NULL," +
            LevelEntry.COLUMN_LEVEL_NAME + " TEXT" +
            ");";
            
    private static final String SQL_CREATE_TITLES =
    "CREATE TABLE IF NOT EXISTS " + TitleEntry.TABLE_NAME + " (" +
            TitleEntry.COLUMN_USER_ID + " TEXT," +
            TitleEntry.COLUMN_TITLE_ID + " TEXT," +
            TitleEntry.COLUMN_TITLE_NAME + " TEXT," +
            "PRIMARY KEY (" + TitleEntry.COLUMN_USER_ID + ", " + TitleEntry.COLUMN_TITLE_ID + ")" +
            ");";

    public static class TitleEntry {
        public static final String TABLE_NAME = "user_titles";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE_ID = "title_id";
        public static final String COLUMN_TITLE_NAME = "title_name";
    }

    public AchievementDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_ENTRIES);
            db.execSQL(SQL_CREATE_LEVELS);
            db.execSQL(SQL_CREATE_TITLES);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 版本迁移
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + AchievementEntry.TABLE_NAME);
            onCreate(db);
        }
    }
    // 所有调用 getWritableDatabase() 或 getReadableDatabase() 时需传入统一密码，例如 "WanderTrack"
}
