package com.example.vshurygin.geoapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.text.internal.client.RecognitionOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by vshurygin on 14.07.2016.
 */
public class MapManipulation {

    private int SHOW_MARKERS_DELAY = 1000;

    private GoogleMap mGoogleMap;
    private RecordLog mRecordLog;
    private SurfaceRendererWrapper mSurfaceRendererWrapper;

    private  CopyOnWriteArrayList<Marker> mAllMarkers;

    private CopyOnWriteArrayList<Record> mAllRecords = new CopyOnWriteArrayList<Record>();
    private boolean mShowMarkersWithDelayIsSkip = false;

    public MapManipulation(GoogleMap googleMap, RecordLog recordLog,SurfaceRendererWrapper surfaceRendererWrapper)
    {
        mGoogleMap = googleMap;
        mRecordLog = recordLog;
        mSurfaceRendererWrapper = surfaceRendererWrapper;
        //mSurfaceRendererWrapper.add3DMarker(521,633);
        try
        {
            mAllRecords.addAll(mRecordLog.readAll());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mAllMarkers = new CopyOnWriteArrayList<>();

        try
        {
            addAllMarkers();
            //addAllPolylines();
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mAllRecords.get(0).getLatitude(),mAllRecords.get(0).getLongitude()),17));
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
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
                    Marker mrk = mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(rec.getLatitude(),rec.getLongitude())).visible(false).title(rec.getComment()));
                    mAllMarkers.add(mrk);
                }
            }
        };
        mainHandler.post(myRunnable);
    }

    public void addRecord(final Record record)
    {
        mAllRecords.add(record);
        Log.d("RECORDADD",record.toString());
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                addMarker(mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(record.getLatitude(),record.getLongitude())).visible(false).title(record.getComment())));
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
       mSurfaceRendererWrapper.deleteMarkers();
        Runnable myRunnable = new Runnable() {
            @Override
            public void run()
            {
                for (Marker mrk: mAllMarkers)
                {
                    mrk.setVisible(true);
                    /////////test///////////
                    LatLng latLng = mrk.getPosition();
                    Projection projection = mGoogleMap.getProjection();
                    Point point = projection.toScreenLocation(latLng);
                    mSurfaceRendererWrapper.add3DMarker(point.x,point.y);
                    Log.d("Marker",point.toString());
                    ////////////////////////
                }

            }
        };
        mainHandler.post(myRunnable);
    }

    public boolean showMarkersWithDelay(final Activity activity, final int delay)
    {
        mShowMarkersWithDelayIsSkip = false;
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        hideAllMarkers();

        final PolylineOptions lineOnMap = new PolylineOptions();
        final CopyOnWriteArrayList<Polyline> polylines = new CopyOnWriteArrayList<>();

        for (int i = 0; i < mAllMarkers.size(); i++)
        {
            if(mShowMarkersWithDelayIsSkip)
            {continue;}
            try
            {
                final int f_i = i;
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mAllMarkers.get(f_i).setVisible(true);

                        if ((f_i == 0) || (f_i == mAllMarkers.size()-1))
                        {
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(mAllMarkers.get(f_i).getPosition()),delay,null);
                        }
                        else
                        {
                            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
                            latLngBuilder.include(mAllMarkers.get(f_i).getPosition());
                            latLngBuilder.include(mAllMarkers.get(f_i-1).getPosition());
                            int size = activity.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBuilder.build(),size,size,0));
                        }
                        lineOnMap.add(mAllMarkers.get(f_i).getPosition());
                        polylines.add(mGoogleMap.addPolyline(lineOnMap));
                    }
                };

                mainHandler.post(myRunnable);

                if (SHOW_MARKERS_DELAY > delay)
                {Thread.sleep(SHOW_MARKERS_DELAY);}
                else
                {Thread.sleep(delay);}
            }
            catch (Exception e)
            {e.printStackTrace();}
        }

        for (final Polyline pol : polylines)
        {
            mainHandler.post(new Runnable() {
            @Override
            public void run() {
                pol.remove();
            }
        });
        }
        mShowMarkersWithDelayIsSkip = false;
        return true;
    }

    public void skipShowMarkersWithDelay()
    {
        mShowMarkersWithDelayIsSkip = true;
    }

    private void addMarker(Marker marker)
    {
        mAllMarkers.add(marker);
    }
}
