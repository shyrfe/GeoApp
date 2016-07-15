package com.example.vshurygin.geoapp;
/*
 1) переделать переменные+
 2) добавить выключатель для записи текущего
 3) добавить карты+
  3.1)с отображением точек+
 4) кнопка плей которая отображает последовательно точки+
 5) вычесление средней скорости+
 6) вычесление дистанции+
 7) починить "танцующие" кнопки
 8) реализовать обновление точек в реальном времени+
 9) фокусировать камеру на точке
 10)прикрутить базу данных
 11)индекс на коммент
 12)прикрутить зуум к точке если она далеко
 13)плей пауз(менять кнопку плэй)
 14)скрость прокрутки плэя
 15)рисовать полоску между точками
* */
import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.util.Timer;
import java.util.TimerTask;

import com.example.vshurygin.geoapp.GeoAppService.MyBinder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {

    public static GoogleMap sGoogleMap;

    final private int UPDATE_MARKERS_TIME = 2000;
    //private int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private Button mCommentButton;
    private Button mPlayDelayMarkersButton;
    private ToggleButton mOnOffTogButton;
    private LocationManager mLocationManager;
    private TextView mStatusView;
    private TextView mSpeedDistanceStatus;
    private EditText mCommentBar;
    //private GoogleMap mGoogleMap;

    private GeoAppService mLocalGeoAppService;
    private boolean mIsServiceBind = false;
    private boolean mIsPlayDelayMarkersOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createMapView();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mStatusView = (TextView)findViewById(R.id.StatusView);
        mStatusView.setText("Service Status: " + (isServiceRunning(GeoAppService.class)?"ON":"OFF"));
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mSpeedDistanceStatus = (TextView)findViewById(R.id.speedDistanceView);
        Timer speedDistanceTimer = new Timer();
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        speedDistanceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if((mIsServiceBind != false) && (mLocalGeoAppService.mapManipulation != null))
                {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSpeedDistanceStatus.setText(String.valueOf(mLocalGeoAppService.mapManipulation.getAverageSpeed()) + " m/s\n" +
                                    String.valueOf(mLocalGeoAppService.mapManipulation.getDistance() + "m"));
                        }
                    });
                }
                else
                {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSpeedDistanceStatus.setText("0 m/s\n0 m");
                        }
                    });
                }
            }
        },0,2000);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mPlayDelayMarkersButton = (Button)findViewById(R.id.PlayDelayMarkers);
        mPlayDelayMarkersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (!mIsPlayDelayMarkersOn)
                {
                    if (mIsServiceBind && (mLocalGeoAppService.mapManipulation != null))
                    {
                        mOnOffTogButton.setClickable(false);
                        mIsPlayDelayMarkersOn = true;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mIsPlayDelayMarkersOn = !mLocalGeoAppService.mapManipulation.showMarkersWithDelay();
                                mOnOffTogButton.setClickable(true);
                            }
                        }).start();
                    }
                    else
                    {
                        Log.d("Play","Server not start!");
                        Toast.makeText(MainActivity.this,"Запустите сервис!",Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Log.d("Play","notStart");
                    mLocalGeoAppService.mapManipulation.skipShowMarkersWithDelay();
                }
            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Timer markersUpdateTimer = new Timer();
        markersUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsServiceBind && (mLocalGeoAppService.mapManipulation != null) && (!mIsPlayDelayMarkersOn))
                {
                    mLocalGeoAppService.mapManipulation.showAllMarkers();
                }
            }
        },0,UPDATE_MARKERS_TIME);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mCommentButton = (Button) findViewById(R.id.CommentButton);
        mCommentBar = (EditText) findViewById(R.id.CommentText);
        mCommentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                final String CommentStr = mCommentBar.getText().toString();
                if(mIsServiceBind)
                {
                    Timer commentTimer = new Timer();
                    commentTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mLocalGeoAppService.setComment(CommentStr);
                        }
                    },2000);
                    mCommentBar.setText("");
                }
                else
                {
                    mStatusView.setText("Service Status: ON");
                    mOnOffTogButton.setChecked(true);

                    Timer commentTimer = new Timer();
                    commentTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mLocalGeoAppService.setComment(CommentStr);
                        }
                    },2000);
                    mCommentBar.setText("");
                }

            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mOnOffTogButton = (ToggleButton)findViewById(R.id.ONOFFtogButton);
        mOnOffTogButton.setChecked(isServiceRunning(GeoAppService.class)?true:false);
        mOnOffTogButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isCheaked)
            {
                if(isCheaked)
                {
                    mStatusView.setText("Service Status: ON");
                    Intent intent = new Intent(MainActivity.this, GeoAppService.class);
                    startService(intent);
                    bindService(intent,mServiceConection, Context.BIND_AUTO_CREATE);

                    Timer checkTimer = new Timer();
                    checkTimer.schedule(new TimerTask(){
                        @Override
                        public void run()
                        {
                            if(!mLocalGeoAppService.isProvideEnabled())
                            {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }

                            //mLocalGeoAppService.mapManipulation.showAllMarkers();
                            mLocalGeoAppService.mapManipulation.showAllMarkers();
                        }
                    },500);
                }
                else
                {
                    mStatusView.setText("Service Status: OFF");
                    if (mIsServiceBind)
                    {
                        mLocalGeoAppService.mapManipulation.hideAllMarkers();
                        mLocalGeoAppService.timerSwitch(false);
                        unbindService(mServiceConection);
                        mIsServiceBind = false;
                    }
                    Intent intent = new Intent(MainActivity.this, GeoAppService.class);
                    stopService(intent);
                }
            }
        });
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private ServiceConnection mServiceConection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBinder myBinder = (MyBinder)service;
            mLocalGeoAppService = myBinder.getService();
            mIsServiceBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mIsServiceBind = false;
        }
    };
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void createMapView()
    {
        try{
            if (sGoogleMap == null)
            {
                sGoogleMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                if (sGoogleMap == null)
                {
                    Toast.makeText(MainActivity.this,
                            "Error creating map",Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean isServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {return true;}
        }
        return false;
    }
}
