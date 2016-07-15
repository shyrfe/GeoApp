package com.example.vshurygin.geoapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.internal.client.RecognitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by vshurygin on 14.07.2016.
 */
public class MapManipulation {

    private int SHOW_MARKERS_DELAY = 1000;

    private GoogleMap mGoogleMap;
    private RecordLog mRecordLog;
    private ArrayList<Record> mAllRecords;
    private ArrayList<Marker> mAllMarkers;

    public MapManipulation(GoogleMap googleMap, RecordLog recordLog)
    {
        mGoogleMap = googleMap;
        mRecordLog = recordLog;
        mAllRecords = (ArrayList<Record>) mRecordLog.readAll();
        mAllMarkers = new ArrayList<Marker>();
        addAllMarkers();
    }

    public void addAllMarkers()
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for(Record rec : mAllRecords)
                {
                    Marker mrk = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(rec.getLatitude(),rec.getLongitude())).visible(false));
                    mAllMarkers.add(mrk);
                }
            }
        };
        mainHandler.post(myRunnable);
    }
    public void addRecord(final Record record)
    {
        mAllRecords.add(record);

        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                addMarker(mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(record.getLatitude(),record.getLongitude())).visible(false)));
            }
        };
        mainHandler.post(myRunnable);

    }
    public void removeAllMarkers()
    {
        for (Marker mrk : mAllMarkers)
        {
            try
            {
                mrk.remove();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        mAllMarkers.clear();
    }

    public float getAverageSpeed()
    {
        float speed = 0;
        for (Record rcd: mAllRecords)
        {
            speed += rcd.getSpeed();
        }
        speed = speed/mAllRecords.size();
        return speed;
    }
    public float getDistance()
    {
        float distance = 0;
        float[] d = new float[1];
        for (int i=0; i < mAllRecords.size();i++)
        {
            if (i != (mAllRecords.size() - 1))
            {
                Location.distanceBetween(mAllRecords.get(i).getLatitude(),mAllRecords.get(i).getLongitude(),mAllRecords.get(i+1).getLatitude(),mAllRecords.get(i+1).getLongitude(),d);
                distance += d[0];
            }

        }
        return distance;
    }
    public void hideAllMarkers()
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (Marker mrk:mAllMarkers)
                {
                    mrk.setVisible(false);
                }
            }
        };
        mainHandler.post(myRunnable);

    }
    public void showAllMarkers()
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (Marker mrk: mAllMarkers)
                {
                    mrk.setVisible(true);
                }
            }
        };
        mainHandler.post(myRunnable);
    }
    public void showMarkersWithDelay()
    {
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        for (final Marker mrk : mAllMarkers)
        {
            try
            {
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mrk.setVisible(true);
                    }
                };

                mainHandler.post(myRunnable);

                Thread.sleep(SHOW_MARKERS_DELAY);
            }
            catch (Exception e)
            {e.printStackTrace();}
        }

        /*Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (Marker mrk: mAllMarkers)
                {
                    try{
                        Thread.sleep(1000);
                        mrk.setVisible(true);
                    }
                    catch (Exception e)
                    {e.printStackTrace();}

                }
            }
        };
        mainHandler.post(myRunnable);*/
    }

    private void addMarker(Marker marker)
    {
        mAllMarkers.add(marker);
    }
}
