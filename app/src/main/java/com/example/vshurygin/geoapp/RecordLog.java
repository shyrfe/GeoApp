package com.example.vshurygin.geoapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * 1) переделать count+
 * Created by vshurygin on 11.07.2016.
 */
public class RecordLog {

    final String FILE_NAME = "location.txt";

    private Context mContext;
    private SharedPreferences mSharPref;
    private RecordsDataBase mRecordsDataBase;
    private File mSdFile;
    private int mLastRead = 0;
    private String mPath;
    private BufferedWriter mWriter;
    private boolean mIsOpen = false;

    RecordLog(String path, Context context)
    {
        mContext = context;

        mRecordsDataBase = new RecordsDataBase(mContext);
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        //Cursor c = db.rawQuery("DELETE FROM records",new String[]{});
        //db.delete("records",null,null);
    }

    public void add(Record r)
    {
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        db.insert("records",null,r.toContentValues());
        db.close();
    }

    public List<Record> readAll()
    {
        ArrayList<Record> AllRecords = new ArrayList<Record>();
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        Cursor c = db.query("records",null,null,null,null,null,null);

        try{
            if (c.moveToFirst())
            {
                int idColIndex = c.getColumnIndex("id");
                int timeStampColIndex = c.getColumnIndex("timestamp");
                int imeiColIndex = c.getColumnIndex("imei");
                int intervalColIndex = c.getColumnIndex("interval");
                int commentColIndex = c.getColumnIndex("comment");
                int latitudeColIndex = c.getColumnIndex("latitude");
                int longitudeColIndex = c.getColumnIndex("longitude");
                int radiusColIndex = c.getColumnIndex("radius");
                int speedColIndex = c.getColumnIndex("speed");

                do
                {
                    Record r = new Record(
                            c.getLong(idColIndex),
                            c.getLong(timeStampColIndex),
                            c.getString(imeiColIndex),
                            c.getInt(intervalColIndex),
                            c.getString(commentColIndex),
                            c.getDouble(latitudeColIndex),
                            c.getDouble(longitudeColIndex),
                            c.getFloat(radiusColIndex),
                            c.getFloat(speedColIndex)
                    );
                    AllRecords.add(r);
                }
                while(c.moveToNext());
            }
            db.close();
        }
        catch (Exception e)
        {e.printStackTrace();}

        return AllRecords;
    }

    public int count()
    {
        try {
            SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
            String[] com = new String[]{"count(*) as Count"};
            Cursor c = db.query("records", com, null, null, null, null, null);
            String str;
            str = "";
            if (c != null) {
                if (c.moveToFirst()) {

                    do {
                        for (String cn : c.getColumnNames()) {
                            str = str.concat(c.getString(c.getColumnIndex(cn)));
                        }
                    } while (c.moveToNext());
                }
                c.close();
            } else
                Log.d("Count", "Cursor is null");

            db.close();
            return Integer.valueOf(str);
        }
        catch (Exception e)
        {e.printStackTrace();}

        return 0;
    }

    public Record read()
    {
        Record record;
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        Cursor c = db.query("records",null,null,null,null,null,null);

        if (mLastRead == c.getCount())
        {return null;}
        else
        {
            c.move(mLastRead);

            int idColIndex = c.getColumnIndex("id");
            int timeStampColIndex = c.getColumnIndex("timestamp");
            int imeiColIndex = c.getColumnIndex("imei");
            int intervalColIndex = c.getColumnIndex("interval");
            int commentColIndex = c.getColumnIndex("comment");
            int latitudeColIndex = c.getColumnIndex("latitude");
            int longitudeColIndex = c.getColumnIndex("longitude");
            int radiusColIndex = c.getColumnIndex("radius");
            int speedColIndex = c.getColumnIndex("speed");

            Record r = new Record(
                    c.getLong(idColIndex),
                    c.getLong(timeStampColIndex),
                    c.getString(imeiColIndex),
                    c.getInt(intervalColIndex),
                    c.getString(commentColIndex),
                    c.getDouble(latitudeColIndex),
                    c.getDouble(longitudeColIndex),
                    c.getFloat(radiusColIndex),
                    c.getFloat(speedColIndex)
            );
            mLastRead = c.getPosition()+1;

            return r;
        }
    }

    public Record getLastRecord()
    {
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        Cursor c = db.query("records",null,null,null,null,null,null);
        try
        {
            c.moveToLast();
            int idColIndex = c.getColumnIndex("id");
            int timeStampColIndex = c.getColumnIndex("timestamp");
            int imeiColIndex = c.getColumnIndex("imei");
            int intervalColIndex = c.getColumnIndex("interval");
            int commentColIndex = c.getColumnIndex("comment");
            int latitudeColIndex = c.getColumnIndex("latitude");
            int longitudeColIndex = c.getColumnIndex("longitude");
            int radiusColIndex = c.getColumnIndex("radius");
            int speedColIndex = c.getColumnIndex("speed");

            Record r = new Record(
                    c.getLong(idColIndex),
                    c.getLong(timeStampColIndex),
                    c.getString(imeiColIndex),
                    c.getInt(intervalColIndex),
                    c.getString(commentColIndex),
                    c.getDouble(latitudeColIndex),
                    c.getDouble(longitudeColIndex),
                    c.getFloat(radiusColIndex),
                    c.getFloat(speedColIndex)
            );
            return r;
        }
        catch (Exception e){e.printStackTrace();}
        finally {
            db.close();
        }

        return null;
    }

    private void clearLastLine(File file)
    {
        String tmp = null;
        WriterSwitch(false);
        try
        {
            File tempFile = new File(Environment.getExternalStorageDirectory()+"/"+"GeoAppDir"+"/"+"tempfile.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            while ((tmp = reader.readLine()) != null)
            {
                if (tmp.equals("</root>"))
                {
                    continue;
                }
                else
                {
                    writer.write(tmp + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
            tempFile.renameTo(file);

        }
        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        WriterSwitch(true);
    }

    public void WriterSwitch(boolean wrswitch)
    {
        if(wrswitch == true)
        {
            try
            {
                if (mIsOpen != true)
                {
                    mWriter = new BufferedWriter(new FileWriter(mSdFile, true));
                    mIsOpen = true;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                if (mIsOpen == true) {
                    mWriter.close();
                    mIsOpen = false;
                }
            }
            catch (Exception e)
            {e.printStackTrace();}

        }
    }
}
