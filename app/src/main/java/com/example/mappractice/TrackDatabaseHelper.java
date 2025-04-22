package com.example.mappractice;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
public class TrackDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "tracks.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_TRACKS = "tracks";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FILE_PATH = "filePath";
    public static final String COLUMN_START_TIME = "startTime";
    public static final String COLUMN_END_TIME = "endTime";

    public TrackDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据库
        String createTable = "CREATE TABLE " + TABLE_TRACKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILE_PATH + " TEXT, " +
                COLUMN_START_TIME + " LONG, " +
                COLUMN_END_TIME + " LONG)";
        db.execSQL(createTable);
        Log.d("TrackDatabaseHelper", "Database and table created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKS);
        onCreate(db);
    }
    public void insertTrackMeta(String filePath, long startTime, long endTime) {
        //往 tracks 表中插入一条轨迹元数据
        SQLiteDatabase db = this.getWritableDatabase("WanderTrack");
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILE_PATH, filePath);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        db.insert(TABLE_TRACKS, null, values);
        db.close();
        Log.d("TrackDatabaseHelper", "Inserted track: " + filePath + " from " + startTime + " to " + endTime);
    }

    public List<TrackMeta> getAllTracks() {
        //查询数据库中所有的轨迹元数据
        List<TrackMeta> trackList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase("WanderTrack");
        Cursor cursor = db.query(TABLE_TRACKS, null, null, null, null, null, COLUMN_START_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                String filePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FILE_PATH));
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME));
                trackList.add(new TrackMeta(filePath, startTime, endTime));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return trackList;
    }
}
