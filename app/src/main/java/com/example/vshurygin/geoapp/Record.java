package com.example.vshurygin.geoapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.LayoutDirection;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by vshurygin on 11.07.2016.
 * 1) типы данных и их возваращение +
 * 2) добавить сохр ид+
 * 3) вынести логику+
 * 4)индекс на коммент
 * 5) зуум к точке
 * 6)плей пауз
 * 7)скрость прокрутки
 *
 */
public class Record
{
    public static final String LOCAL_PREFERENCES = "locRpef";

    private long mId;
    private long mTimestamp;
    private String mImei;
    private int mInterval;
    private String mComment;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private float mSpeed;
    private SharedPreferences mSharPref;


    public long getId()
    {
        return mId;
    }
    public long getTimestamp()
    {
        return mTimestamp;
    }
    public String getImei() {
        return mImei;
    }
    public int getInterval()
    {
        return mInterval;
    }
    public String getComment()
    {
        return mComment;
    }
    public double getLatitude()
    {
        return mLatitude;
    }
    public double getLongitude()
    {
        return mLongitude;
    }
    public float getRadius()
    {
        return mRadius;
    }
    public float getSpeed()
    {
        return mSpeed;
    }

    public void setId(long id)
    {
        mId = id;
    }



    //public Record(String, ...)
    //public Record(Location)

    public Record (long id, long timestamp,String imei, int interval, String comment, double latitude, double longitude, float radius, float speed)
    {
        mId = id;
        mTimestamp = timestamp;
        mImei = imei;
        mInterval = interval;
        mComment = comment;
        mLatitude = latitude;
        mLongitude = longitude;
        mRadius = radius;
        mSpeed = speed;
    }

    public Record(Location location, String imei)
    {
        if (location != null)
        {
            /*mSharPref = context.getSharedPreferences(LOCAL_PREFERENCES,context.MODE_PRIVATE);
            String lastId;
            if ((lastId = mSharPref.getString("LAST_ID","")).equals(""))
            {
                lastId = "1";
                Editor ed = mSharPref.edit();
                ed.putString("LAST_ID",lastId);
                ed.commit();
            }
            else
            {
                lastId = String.valueOf((Long.valueOf(lastId)+1));
                Editor ed = mSharPref.edit();
                ed.putString("LAST_ID",lastId);
                ed.commit();
            }*/

            /*mId = id;*/
            mTimestamp = location.getTime();
            mImei = imei;
            mInterval = 30;
            mComment = "";
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mRadius = location.getAccuracy();
            mSpeed = location.getSpeed();
        }
    }
    public Record(Location location,String imei, String comment)
    {
        if (location != null)
        {
            /*mSharPref = context.getSharedPreferences(LOCAL_PREFERENCES,context.MODE_PRIVATE);
            String lastId;
            if ((lastId = mSharPref.getString("LAST_ID","")).equals(""))
            {
                lastId = "1";
                Editor ed = mSharPref.edit();
                ed.putString("LAST_ID",lastId);
                ed.commit();
            }
            else
            {
                lastId = String.valueOf((Long.valueOf(lastId)+1));
                Editor ed = mSharPref.edit();
                ed.putString("LAST_ID",lastId);
                ed.commit();
            }*/

            /*mId = id;*/
            mTimestamp = location.getTime();
            mImei = imei;
            mInterval = 0;
            mComment = comment;
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            mRadius = location.getAccuracy();
            mSpeed = location.getSpeed();
        }
    }

    @Override
    public String toString()
    {
        SimpleDateFormat formating = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        StringBuilder str = new StringBuilder("<coordinate>\n");
        str.append("<Id>" + mId +"</Id>\n");
        str.append("<TimeStamp>" + formating.format(mTimestamp) +"</TimeStamp>\n");
        str.append("<IMEI>" + mImei + "</IMEI>\n");
        str.append("<Interval>" + mInterval + "</Interval>\n");
        str.append("<Comment>" + mComment + "</Comment>\n");
        str.append("<Latitude>" + mLatitude + "</Latitude>\n");
        str.append("<Longitude>" + mLongitude + "</Longitude>\n");
        str.append("<Radius>" + mRadius + "</Radius>\n");
        str.append("<Speed>" + mSpeed + "</Speed>\n");
        str.append("</coordinate>\n");

        return str.toString();
    }
    public ContentValues toContentValues()
    {
        ContentValues cv = new ContentValues();

        cv.put("timestamp",mTimestamp);
        cv.put("imei",mImei);
        cv.put("interval",mInterval);
        cv.put("comment",mComment);
        cv.put("latitude",mLatitude);
        cv.put("longitude",mLongitude);
        cv.put("radius",mRadius);
        cv.put("speed",mSpeed);

        return cv;
    }

    public static Record parse(String data)
    {
        long mId = 0;
        long mTimestamp = 0;
        String mImei = null;
        int mInterval = 0;
        String mComment = null;
        double mLatitude = 0;
        double mLongitude = 0;
        float mRadius = 0;
        float mSpeed = 0;


        String[] strMass;
        try {
            strMass = data.split("<|>");
            for(int i = 0; i < strMass.length; i++)
            {
                switch (strMass[i])
                {
                    case "Id":
                    {
                        mId = Long.valueOf(strMass[i+1]);
                        break;
                    }
                    case "TimeStamp":
                    {
                        SimpleDateFormat formating = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        mTimestamp = formating.parse(strMass[i+1]).getTime();
                        break;
                    }
                    case "IMEI":
                    {
                        mImei = strMass[i+1];
                        break;
                    }
                    case "Interval":
                    {
                        mInterval = Integer.valueOf(strMass[i+1]);
                        break;
                    }
                    case "Comment":
                    {
                        mComment = strMass[i+1];
                        break;
                    }
                    case "Latitude":
                    {
                        mLatitude = Double.valueOf(strMass[i+1]);
                        break;
                    }
                    case "Longitude":
                    {
                        mLongitude = Double.valueOf(strMass[i+1]);
                    }
                    case "Radius":
                    {
                        mRadius = Float.valueOf(strMass[i+1]);
                        break;
                    }
                    case "Speed":
                    {
                       mSpeed = Float.valueOf(strMass[i+1]);
                       break;
                    }
                }
            }
            Record record = new Record(mId,mTimestamp,mImei,mInterval,mComment,mLatitude,mLongitude,mRadius,mSpeed);
            return record;
        }
        catch (Exception e)
        {
            Log.d("ParseSplit","Input string are not valid");
            e.printStackTrace();
        }
        return null;
    }
    public static Record parse(Location location, String imei)
    {
        Record record = new Record(location,imei);
        return record;
    }
    public static Record parse(Location location, String imei, String comment)
    {
        Record record = new Record(location,imei,comment);
        return record;
    }
}
