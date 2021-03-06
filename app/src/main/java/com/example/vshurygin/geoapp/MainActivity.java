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
 9) фокусировать камеру на точке+
 10)прикрутить базу данных+
 11)индекс на коммент
 12)прикрутить зуум к точке если она далеко+
 13)плей пауз(менять кнопку плэй) +
 14)скрость прокрутки плэя+
 15)рисовать полоску между точками+
 16)OpenGL нарисовать нечто на карте нативными средствами+

 17)API с запросами по чему либо, магазины, кафе, музеи...(гугловские скорее всего) запросы через json (наверно)
 18)новый активити с отображением таблицы с результатами поиска по карте
 19)REST api посмотреть
* */
import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.nfc.Tag;
import android.opengl.GLSurfaceView;
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
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.example.vshurygin.geoapp.GeoAppService.MyBinder;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {

    static {System.loadLibrary("OpenGLObjectModel");}
    public native String getMsgFromJni();


    public static GoogleMap sGoogleMap;
    public static SurfaceRendererWrapper mSurfaceRenderer;
    public static LinearLayout sLinearLayout;

    private GLSurfaceView mGlSurfaceView;
    private boolean rendererSet;

    final private int UPDATE_MARKERS_TIME = 1000;
    //private int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private Button mCommentButton;
    private Button mPlayDelayMarkersButton;
    private Button mSearchButton;
    private ToggleButton mOnOffTogButton;
    private LocationManager mLocationManager;
    private TextView mStatusView;
    private TextView mSpeedDistanceStatus;
    private EditText mCommentBar;
    private SeekBar mMarkersDelaySpeedSeekBar;
    private int mMarkersDelaySpeed = 0;

    private GeoAppService mLocalGeoAppService;
    private boolean mIsServiceBind = false;
    private boolean mIsPlayDelayMarkersOn = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        createMapView();
        //GoogleApiClient
        //Log.d("Native",getMsgFromJni());
        GLSurfaceInitialize();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mMarkersDelaySpeedSeekBar = (SeekBar)findViewById(R.id.markersDelaySpeedSeekBar);
        mMarkersDelaySpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMarkersDelaySpeed = seekBar.getProgress();
            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mStatusView = (TextView)findViewById(R.id.StatusView);
        mStatusView.setText("Service Status: " + (isServiceRunning(GeoAppService.class)?"ON":"OFF"));
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        mSearchButton = (Button)findViewById(R.id.SearchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                if (conManager.getActiveNetworkInfo() != null)
                {
                    if (isServiceRunning(GeoAppService.class) && mIsServiceBind)
                    {
                        Intent intent = new Intent(MainActivity.this,SearchResultActivity.class);

                        Record r = mLocalGeoAppService.mRecordLog.getLastRecord();
                        //Log.d("LatLong",r.getLatitude() + " " + r.getLongitude());
                        intent.putExtra("latitude",r.getLatitude());
                        intent.putExtra("longitude",r.getLongitude());
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,R.string.serviceAreNotStarted,Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(MainActivity.this,R.string.internet_not_found,Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(MainActivity.this,"SearchOK!",Toast.LENGTH_SHORT).show();
            }
        });
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
                            mSpeedDistanceStatus.setText(String.valueOf(mLocalGeoAppService.mapManipulation.getAverageSpeed()) + " m/s  :  " +
                                    String.valueOf(mLocalGeoAppService.mapManipulation.getDistance() + "m"));
                        }
                    });
                }
                else
                {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSpeedDistanceStatus.setText("0 m/s  :  0 m");
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
                        mPlayDelayMarkersButton.setText(R.string.pauseButton);
                        mIsPlayDelayMarkersOn = true;

                        Thread playDelayMarkersThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean localPlayDelayMarkersOn = !mLocalGeoAppService.mapManipulation.showMarkersWithDelay(MainActivity.this,mMarkersDelaySpeed * 1000 + 1);
                                mOnOffTogButton.setClickable(true);

                                final Handler mainHandler = new Handler(Looper.getMainLooper());
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mPlayDelayMarkersButton.setText(R.string.playButton);
                                    }});
                                mIsPlayDelayMarkersOn = localPlayDelayMarkersOn;
                            }
                            });
                        if(playDelayMarkersThread.isAlive() == false)
                        {
                            playDelayMarkersThread.start();
                        }
                    }
                    else
                    {
                        mPlayDelayMarkersButton.setText(R.string.playButton);
                        Log.d("Play","Service not start!");
                        Toast.makeText(MainActivity.this,R.string.serviceAreNotStarted,Toast.LENGTH_LONG).show();
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
        /*Timer markersUpdateTimer = new Timer();
        markersUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mIsServiceBind && (mLocalGeoAppService.mapManipulation != null) && (!mIsPlayDelayMarkersOn))
                {
                    //mLocalGeoAppService.mapManipulation.showAllMarkers();
                }
            }
        },0,UPDATE_MARKERS_TIME);*/
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
        mOnOffTogButton = (ToggleButton)findViewById(R.id.ONOFFtogButton);
        mOnOffTogButton.setChecked(isServiceRunning(GeoAppService.class)?true:false);
        mOnOffTogButton.setClickable(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mOnOffTogButton.setClickable(true);
            }
        },1000);
        mOnOffTogButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isCheaked)
            {
                if(isCheaked)
                {
                    mStatusView.setText("Service Status: ON");
                    try
                    {
                        Intent intent = new Intent(MainActivity.this, GeoAppService.class);
                        startService(intent);
                        bindService(intent,mServiceConection, Context.BIND_AUTO_CREATE);
                    }
                    catch (Exception e){e.printStackTrace();}


                    Timer checkTimer = new Timer();
                    checkTimer.schedule(new TimerTask(){
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(!mLocalGeoAppService.isProvideEnabled())
                                {

                                       startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

                                }
                            //mLocalGeoAppService.mapManipulation.showAllMarkers();
                            //mLocalGeoAppService.mapManipulation.showAllMarkers();
                            }
                            catch (Exception e)
                            {e.printStackTrace();}
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
        try
        {
            if (sGoogleMap == null)
            {
                OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback()
                {
                    @Override
                    public void onMapReady(GoogleMap googleMap)
                    {
                        sGoogleMap = googleMap;
                        if (sGoogleMap == null)
                        {
                            Toast.makeText(MainActivity.this,
                                    "Error creating map",Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                ((MapFragment)getFragmentManager().findFragmentById(R.id.mapView)).getMapAsync(mapReadyCallback);//.getMap();

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

    private void GLSurfaceInitialize()
    {
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        final boolean isSupportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000 || isProbablyEmulator();

        if (isSupportsEs2)
        {
            sLinearLayout = (LinearLayout) findViewById(R.id.GLlayout);

            mGlSurfaceView = new GLSurfaceView(MainActivity.this);

            mGlSurfaceView.setZOrderOnTop(true);
            mGlSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mGlSurfaceView.setEGLConfigChooser(8,8,8,8,16,0);
            mGlSurfaceView.setEGLContextClientVersion(2);

            mSurfaceRenderer = new SurfaceRendererWrapper(MainActivity.this);
            mGlSurfaceView.setRenderer(mSurfaceRenderer);

            rendererSet = true;

            sLinearLayout.addView(mGlSurfaceView,0);

        }
        else
        {
            Toast.makeText(MainActivity.this,"This device does not support OpenGl ES 2.0",Toast.LENGTH_LONG).show();
        }
    }
    private boolean isProbablyEmulator() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86"));
    }
}
