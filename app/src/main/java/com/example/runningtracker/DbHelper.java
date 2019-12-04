package com.example.runningtracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import  com.example.runningtracker.RunningSession;

import com.example.runningtracker.Provider.MyContentProvider;

import java.util.ArrayList;
import java.util.List;


public class DbHelper extends SQLiteOpenHelper {

    //boiler plate code extracted from previous coursework.

    private ContentResolver myCR;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "sessionDB.db";
    public static final String TABLE_PRODUCTS = "sessions";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_AVGSPEED = "avgSpeed";
    public static final String COLUMN_LOCATION_LIST = "locationList";
    public static final String COLUMN_EXERCISE_MODE = "mode";


    public DbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        myCR = context.getContentResolver();



    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " +
                TABLE_PRODUCTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_DATE
                + " INTEGER," + COLUMN_DISTANCE + " REAL," + COLUMN_TIME + " INTEGER," + COLUMN_AVGSPEED + " REAL," + COLUMN_LOCATION_LIST + " TEXT," + COLUMN_EXERCISE_MODE + " TEXT" +")";
        sqLiteDatabase.execSQL(CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(sqLiteDatabase);
    }

    public void add(RunningSession runningSession) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, runningSession.getDate());
        values.put(COLUMN_TIME, runningSession.getTotalTime());
        values.put(COLUMN_DISTANCE,runningSession.getTotalDistance());
        values.put(COLUMN_AVGSPEED,runningSession.getAvgSpeed());
        values.put(COLUMN_LOCATION_LIST,runningSession.getStringList());
        values.put(COLUMN_EXERCISE_MODE,runningSession.getMode());
        myCR.insert(MyContentProvider.CONTENT_URI, values);
    }


    public List<RunningSession> find(long timestamp) {
        String selection = "";
        List<RunningSession> sessionList = new ArrayList<>();
        String[] projection = {COLUMN_ID,COLUMN_LOCATION_LIST,COLUMN_DATE,COLUMN_DISTANCE, COLUMN_TIME,COLUMN_AVGSPEED,COLUMN_EXERCISE_MODE};
        if(timestamp != 0){
                selection = "date >= \"" + timestamp + "\"";
        }

        Cursor cursor = myCR.query(MyContentProvider.CONTENT_URI,
                projection, selection.isEmpty() ? null : selection, null,
                "date DESC");


        if (cursor.moveToFirst()) {
            do {
                RunningSession session = new RunningSession(cursor.getString(1),cursor.getLong(2),cursor.getFloat(3),cursor.getInt(4),cursor.getFloat(5),cursor.getString(6));
                session.setId(Integer.parseInt(cursor.getString(0)));
                sessionList.add(session);
            } while (cursor.moveToNext());
        }


        return sessionList;
    }

    public RunningSession findById(int id){
        RunningSession session = null;
        String selection = "";
        String[] projection = {COLUMN_ID,COLUMN_LOCATION_LIST,COLUMN_DATE,COLUMN_DISTANCE, COLUMN_TIME,COLUMN_AVGSPEED,COLUMN_EXERCISE_MODE};
        if(id != 0){
            selection = "id = \"" + id + "\"";
        }

        Cursor cursor = myCR.query(MyContentProvider.CONTENT_URI,
                projection, selection.isEmpty() ? null : selection, null,
                null);



        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            session = new RunningSession(cursor.getString(1),cursor.getLong(2),cursor.getFloat(3),cursor.getInt(4),cursor.getFloat(5),cursor.getString(6));
            session.setId(Integer.parseInt(cursor.getString(0)));
        }

        return session;
    }

    public boolean deleteSession(int id){
        boolean result = false;
        String selection = "id = \"" + id + "\"";
        int rowsDeleted = myCR.delete(MyContentProvider.CONTENT_URI,
                selection, null);
        if (rowsDeleted>0){
            result = true;
        }

        return  result;
    }



}
