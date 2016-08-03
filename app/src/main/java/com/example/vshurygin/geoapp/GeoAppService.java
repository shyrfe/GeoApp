package com.example.vshurygin.geoapp;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.os.Binder;
import android.os.IBinder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Xml;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;


public class GeoAppService extends Service {
    final private String FILE_PATH = "GeoAppDir";

    private IBinder mBinder = new MyBinder();
    private LocationManager mLocationManager;
    private int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private Location mLocalLocation;
    private Timer mTimer;
    private boolean mProvideEnabledStatus = true;
    private ArrayList<String> mCommentCache = new ArrayList<String>();
    private TelephonyManager mTelephonyManager;

    public  RecordLog mRecordLog;
    public MapManipulation mapManipulation;

    public GeoAppService() {
    }

    @Override
    public void onCreate()
    {

        mLocationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions( (Activity)this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    //MY_PERMISSION_ACCESS_FINE_LOCATION );
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,1, locationListener);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1, locationListener);

        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        mRecordLog = new RecordLog(FILE_PATH,GeoAppService.this);

        if (MainActivity.sGoogleMap != null)
        {
            mapManipulation = new MapManipulation(MainActivity.sGoogleMap,mRecordLog,MainActivity.mSurfaceRenderer);
        }
        mTimer = new Timer();
        mTimer.schedule(new XMLpackTimer(),0,30000);

    }

    @Override
    public void onDestroy()
    {
        if (MainActivity.mSurfaceRenderer != null)
        {
            MainActivity.mSurfaceRenderer.deleteMarkers();
        }
        mRecordLog.WriterSwitch(false);
        Log.d("Service","Destroy");
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {}
        mLocationManager.removeUpdates(locationListener);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        if (mapManipulation != null)
        {
            mapManipulation.removeAllMarkers();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("BindStatus","BindON");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent)
    {
        Log.d("BindStatus","Rebind");
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        Log.d("BindStatus","BindOFF");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        return true;
    }

    public void setComment(String comment)
    {
        if(mLocalLocation != null)
        {
            Record record = Record.parse(mLocalLocation,mTelephonyManager.getDeviceId(),comment);
            mRecordLog.add(record);

            if (mapManipulation != null)
            {mapManipulation.addRecord(record);}

            Log.d("Comment","OK");
        }
        else
        {
            Log.d("Comment","NotOK");
            mCommentCache.add(comment);
        }
    }

    public void timerSwitch(boolean isOn)
    {
        if(isOn)
        {
            if (mTimer != null)
            {
                mTimer.schedule(new XMLpackTimer(),0,30000);
            }
            else
            {
                mTimer = new Timer();
                mTimer.schedule(new XMLpackTimer(),0,30000);
            }
        }
        else
        {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer.purge();
            }
        }
    }

    public boolean isProvideEnabled()
    {
        return mProvideEnabledStatus;
    }

    public void addAllCommentCache()
    {
        for (String str : mCommentCache)
        {
            Record record = Record.parse(mLocalLocation,mTelephonyManager.getDeviceId(),str);

            mRecordLog.add(record);
            if (mapManipulation != null)
            {mapManipulation.addRecord(record);}

            Log.d("commentCache","\""+str+"\" comment add");
        }

        mCommentCache.clear();
    }
///////////////////////////////////////////////////////////////////////////////////
    public class MyBinder extends Binder
    {
        GeoAppService getService()
        {
            return GeoAppService.this;
        }
    }
///////////////////////////////////////////////////////////////////////////////////
    public LocationListener locationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            Log.d("Coordinate","LAT: "+String.valueOf(location.getLatitude()) + " LONG: "+String.valueOf(location.getLongitude()));
            mLocalLocation = location;
            addAllCommentCache();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        mProvideEnabledStatus = true;
        Log.d("Provider","ON");
    }

    @Override
    public void onProviderDisabled(String provider) {
        mProvideEnabledStatus = false;
        Log.d("Provider","OFF");
    }
};
///////////////////////////////////////////////////////////////////////////////////
    class XMLpackTimer extends TimerTask
    {
        @Override
        public void run()
        {
            if (mLocalLocation != null)
            {
                Record record = Record.parse(mLocalLocation,mTelephonyManager.getDeviceId());
                mRecordLog.add(record);
                if (mapManipulation != null)
                {
                    mapManipulation.addRecord(record);
                    //mapManipulation.showAllMarkers();
                }
                Log.d("Count",String.valueOf(mRecordLog.count()));
            }
        }
    }
}
