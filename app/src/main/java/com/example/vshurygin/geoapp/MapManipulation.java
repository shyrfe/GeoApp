package com.example.vshurygin.geoapp;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.internal.client.RecognitionOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by vshurygin on 14.07.2016.
 */
public class MapManipulation {

    private RecordLog mRecordLog;
    private ArrayList<Record> mAllRecords;

    public MapManipulation(RecordLog recordLog)
    {
        mRecordLog = recordLog;
        mAllRecords = (ArrayList<Record>) mRecordLog.readAll();
    }

    public void addAllMarkers(final GoogleMap googleMap)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for(Record rec : mAllRecords)
                {

                    googleMap.addMarker(new MarkerOptions().position(new LatLng(rec.getLatitude(),rec.getLongitude())));
                }
            }
        };
        mainHandler.post(myRunnable);
    }
    public void clearAllMarkers()
    {

    }
}
