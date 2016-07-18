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
            /*mPath = path;*/
        mContext = context;

        mRecordsDataBase = new RecordsDataBase(mContext);
        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        //db.delete("records",null,null);
        //db.close();
        //SQLiteDatabase db = dataBase.getWritableDatabase();
        //ContentValues cv = new ContentValues();
        //cv.put("timestamp",10);
        //db.insert("records",null,cv);
        //Cursor c = db.query("records",null,null,null,null,null,null);
       // if (c.moveToFirst())
        //{
            //int time = c.getColumnIndex("timestamp");
            //Log.d("DATABASE",String.valueOf(c.getInt(time)));

        //}
        //dataBase.close();

            /*File sdPath = Environment.getExternalStorageDirectory();
            sdPath = new File(sdPath.getAbsolutePath()+"/"+mPath);
            sdPath.mkdirs();*/

            /*mSdFile = new File(sdPath,FILE_NAME);
            mSharPref = context.getSharedPreferences(Record.LOCAL_PREFERENCES,context.MODE_PRIVATE);*/

            /*try
            {
                    if (mSdFile.exists() && mSdFile.isFile())
                    {
                        Log.d("RecordFile", "found");
                        WriterSwitch(true);
                    }
                    else
                    {
                        Log.d("RecordFile", "notfound");

                        SharedPreferences.Editor ed = mSharPref.edit();
                        ed.putInt("RECORD_COUNT",0);
                        ed.commit();

                        WriterSwitch(true);
                        mWriter.write("<root>\n");
                        mWriter.write("</root>");
                    }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (SecurityException se)
            {
                se.printStackTrace();
            }
*/
    }

    public void add(Record r)
    {
        /*
        if (stream not opened)
        {
            if (file not exists)
            {
                create file
                write xml header
            }

            open file stream
            open buf stream
        }
         */

        SQLiteDatabase db = mRecordsDataBase.getWritableDatabase();
        db.insert("records",null,r.toContentValues());
        db.close();
        /*try
        {
            clearLastLine(mSdFile);
            if (!mIsOpen && (!mSdFile.exists()))
            {

                File sdPath = Environment.getExternalStorageDirectory();
                sdPath = new File(sdPath.getAbsolutePath()+"/"+mPath);
                sdPath.mkdirs();
                mSdFile = new File(sdPath,FILE_NAME);

                WriterSwitch(true);
                mWriter.write("<root>"+ System.getProperty("line.separator"));
                mWriter.write("</root>");

                SharedPreferences.Editor ed = mSharPref.edit();
                ed.putInt("RECORD_COUNT",0);
                ed.commit();
            }
            else if (!mIsOpen)
            {
                WriterSwitch(true);
            }

            long lastId;

            if ((lastId = mSharPref.getLong("LAST_ID",0)) == 0)
            {
                lastId = 1;
                SharedPreferences.Editor ed = mSharPref.edit();
                ed.putLong("LAST_ID",lastId);
                ed.commit();
                Log.d("LastId","0");
            }
            else
            {
                lastId++;
                SharedPreferences.Editor ed = mSharPref.edit();
                ed.putLong("LAST_ID",lastId);
                ed.commit();
                Log.d("LastId",String.valueOf(lastId));
            }

            r.setId(lastId);
            mWriter.write(r.toString());
            mWriter.write("</root>");


            int count = mSharPref.getInt("RECORD_COUNT",0);
            count++;
            SharedPreferences.Editor ed = mSharPref.edit();
            ed.putInt("RECORD_COUNT",count);
            ed.commit();

            Log.d("SD", "Файл записан");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }*/


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


        /*try
        {
            BufferedReader reader = new BufferedReader(new FileReader(mSdFile));
            String tmp;
            StringBuilder recordString;
            while((tmp = reader.readLine()) != null)
            {
                if (tmp.equals("<coordinate>"))
                {
                    recordString = new StringBuilder(tmp);
                    while (!tmp.equals("</coordinate>") )
                    {
                        tmp = reader.readLine();
                        recordString.append(tmp);
                    }
                    AllRecords.add(Record.parse(recordString.toString()));
                    recordString = null;
                }
            }
            reader.close();
        }
        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            WriterSwitch(true);
        }*/
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
        //int length = mSharPref.getInt("RECORD_COUNT",0);
        /*try
        {
            BufferedReader reader = new BufferedReader(new FileReader(mSdFile));
            String tmp;
            while((tmp = reader.readLine()) != null)
            {
                //Log.d("count",tmp);
                if (tmp.equals("<coordinate>"))
                {
                    length++;
                }
            }
            reader.close();
        }
        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        */

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

        /*WriterSwitch(false);
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(mSdFile));
            String tmp;
            StringBuilder recordString;
            for (int i = 0; i < mLastRead; i++){reader.readLine();}
            while((tmp = reader.readLine()) != null)
            {
                mLastRead++;
                if (tmp.equals("<coordinate>"))
                {
                    recordString = new StringBuilder(tmp);
                    while (!tmp.equals("</coordinate>") )
                    {
                        tmp = reader.readLine();
                        mLastRead++;
                        recordString.append(tmp);
                    }
                    record = Record.parse(recordString.toString());
                    return record;
                }
            }
            reader.close();
        }
        catch (FileNotFoundException exception)
        {
            exception.printStackTrace();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            WriterSwitch(true);
        }*/

        //return null;
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
