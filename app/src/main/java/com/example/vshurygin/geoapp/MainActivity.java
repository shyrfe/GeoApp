package com.example.vshurygin.geoapp;
/*
 1) переделать переменные
 2) добавить выключатель для текущего \
 3) добавить карты с отображением точе
 4) кнопка плей которая отображает последовательно точки
 5) вычесление средней скорости
 6) вычесление дистанции
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
import android.os.IBinder;
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
public class MainActivity extends AppCompatActivity {

    private int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private Button CommentButton;
    private ToggleButton ONOFFtogButton;
    private LocationManager locationManager;
    private TextView StatusView;
    private EditText CommentBar;

    private GeoAppService localGeoAppService;
    private boolean isServiceBind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        StatusView = (TextView)findViewById(R.id.StatusView);
        StatusView.setText("Service Status: " + (isServiceRunning(GeoAppService.class)?"ON":"OFF"));
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        CommentButton = (Button) findViewById(R.id.CommentButton);
        CommentBar = (EditText) findViewById(R.id.CommentText);
        CommentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                final String CommentStr = CommentBar.getText().toString();
                if(isServiceBind)
                {
                    Timer commentTimer = new Timer();
                    commentTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            localGeoAppService.setComment(CommentStr);
                        }
                    },2000);
                    CommentBar.setText("");
                }
                else
                {
                    StatusView.setText("Service Status: ON");
                    ONOFFtogButton.setChecked(true);

                    Timer commentTimer = new Timer();
                    commentTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            localGeoAppService.setComment(CommentStr);
                        }
                    },2000);
                    CommentBar.setText("");
                }

            }
        });
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ONOFFtogButton = (ToggleButton)findViewById(R.id.ONOFFtogButton);
        ONOFFtogButton.setChecked(isServiceRunning(GeoAppService.class)?true:false);
        ONOFFtogButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isCheaked)
            {
                if(isCheaked)
                {
                    StatusView.setText("Service Status: ON");
                    Intent intent = new Intent(MainActivity.this, GeoAppService.class);
                    startService(intent);
                    bindService(intent,mServiceConection, Context.BIND_AUTO_CREATE);

                    Timer checkTimer = new Timer();
                    checkTimer.schedule(new TimerTask(){
                        @Override
                        public void run()
                        {
                            if(!localGeoAppService.isProvideEnabled())
                            {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }
                        }
                    },500);
                }
                else
                {
                    StatusView.setText("Service Status: OFF");
                    if (isServiceBind)
                    {
                        localGeoAppService.timerSwitch(false);
                        unbindService(mServiceConection);
                        isServiceBind = false;
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
            localGeoAppService = myBinder.getService();
            isServiceBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBind = false;
        }
    };
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
