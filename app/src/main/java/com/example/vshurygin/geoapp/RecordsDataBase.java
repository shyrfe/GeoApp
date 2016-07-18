package com.example.vshurygin.geoapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vshurygin on 18.07.2016.
 */
public class RecordsDataBase extends SQLiteOpenHelper {
    public RecordsDataBase(Context context) {
        super(context, "RecordsDataBase", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table records ("
        +"id integer primary key autoincrement,"
        +"timestamp integer,"
        +"imei text,"
        +"interval integer,"
        +"comment text,"
        +"latitude real,"
        + "longitude real,"
        +"radius real,"
        +"speed real"+");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
