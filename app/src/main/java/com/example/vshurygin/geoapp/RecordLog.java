package com.example.vshurygin.geoapp;

import android.content.Context;
import android.content.SharedPreferences;
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
    private File mSdFile;
    private int mLastRead = 0;
    private String mPath;
    private BufferedWriter mWriter;
    private boolean mIsOpen = false;

    RecordLog(String path, Context context)
    {
            mPath = path;
            mContext = context;

            File sdPath = Environment.getExternalStorageDirectory();
            sdPath = new File(sdPath.getAbsolutePath()+"/"+mPath);
            sdPath.mkdirs();

            mSdFile = new File(sdPath,FILE_NAME);
            mSharPref = context.getSharedPreferences(Record.LOCAL_PREFERENCES,context.MODE_PRIVATE);

            try
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

        try
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
        }
    }
    public List<Record> readAll()
    {
        WriterSwitch(false);
        ArrayList<Record> AllRecords = new ArrayList<Record>();
        try
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
        }
        return AllRecords;
    }

    public int count()
    {
        int length = mSharPref.getInt("RECORD_COUNT",0);
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
        return length;

    }

    public Record read()
    {
        Record record;
        WriterSwitch(false);
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
