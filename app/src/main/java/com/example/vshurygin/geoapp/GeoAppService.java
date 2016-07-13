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
    private String FILE_PATH = "GeoAppDir";
    private IBinder mBinder = new MyBinder();
    private LocationManager locationManager;
    private int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private Location localLocation;
    private Timer timer;
    private boolean provideEnabledStatus = true;
    private ArrayList<String> commentCache = new ArrayList<String>();
    private RecordLog recordLog;
    private TelephonyManager mTelephonyManager;

    public GeoAppService() {
    }

    @Override
    public void onCreate()
    {

        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //ActivityCompat.requestPermissions( (Activity)this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    //MY_PERMISSION_ACCESS_FINE_LOCATION );
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,1, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1,1, locationListener);

        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        recordLog = new RecordLog(FILE_PATH,GeoAppService.this);


        timer = new Timer();
        timer.schedule(new XMLpackTimer(),0,30000);

    }

    @Override
    public void onDestroy()
    {
        recordLog.WriterSwitch(false);
        Log.d("Service","Destroy");
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {}
        locationManager.removeUpdates(locationListener);
        if (timer != null) {
            timer.cancel();
            timer.purge();
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
        //recordLog.WriterSwitch(false);
        Log.d("BindStatus","BindOFF");
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        return true;
    }

    public void setComment(String comment)
    {
        if(localLocation != null)
        {
            recordLog.add(Record.parse(localLocation,mTelephonyManager.getDeviceId(),comment));
            Log.d("Comment","OK");
        }
        else
        {
            Log.d("Comment","NotOK");
            commentCache.add(comment);
        }
    }

    public void timerSwitch(boolean isOn)
    {
        if(isOn)
        {
            if (timer != null)
            {
                timer.schedule(new XMLpackTimer(),0,30000);
            }
            else
            {
                timer = new Timer();
                timer.schedule(new XMLpackTimer(),0,30000);
            }
        }
        else
        {
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        }
    }

    public boolean isProvideEnabled()
    {
        return provideEnabledStatus;
    }

    public void addAllCommentCache()
    {
        for (String str : commentCache)
        {
            recordLog.add(Record.parse(localLocation,mTelephonyManager.getDeviceId(),str));
            Log.d("commentCache","\""+str+"\" comment add");
        }

        commentCache.clear();
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
            localLocation = location;
            addAllCommentCache();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        provideEnabledStatus = true;
        Log.d("Provider","ON");
    }

    @Override
    public void onProviderDisabled(String provider) {
        provideEnabledStatus = false;
        Log.d("Provider","OFF");
    }
};
///////////////////////////////////////////////////////////////////////////////////
  /*  public class XMLpackage
    {
        public long Timestamp;
        public String IMEI;
        public int Interval;
        public String Comment;
        public double Latitude;
        public double Longitude;
        public float Radius;
        public double Speed;

        final String FILE_NAME = "location.txt";
        final String DIR_SD = "GeoAppDir";

        XMLpackage(Location location)
        {
            if (location != null)
            {
                Timestamp = location.getTime();
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                IMEI = telephonyManager.getDeviceId();
                Interval = 30;
                Comment = "";
                Latitude = location.getLatitude();
                Longitude = location.getLongitude();
                Radius = location.getAccuracy();
                Speed = location.getSpeed();
            }
        }
        void setAllParameters(long pTimestamp,String pIMEI,int pInterval,String pComment, double pLatitude,double pLongitude, float pRadius, double pSpeed)
        {
            Timestamp = pTimestamp;
            IMEI = pIMEI;
            Interval = pInterval;
            Comment = pComment;
            Latitude = pLatitude;
            Longitude = pLongitude;
            Radius = pRadius;
            Speed = pSpeed;
        }
        void LogAll()
        {
            SimpleDateFormat formating = new SimpleDateFormat("HH:mm:ss:SSS");
            Log.d(formating.format(Timestamp),formating.format(Timestamp));

            Log.d(String.valueOf(IMEI),String.valueOf(IMEI));
            Log.d(String.valueOf(Interval),String.valueOf(Interval));
            Log.d(String.valueOf(Comment),String.valueOf(Comment));
            Log.d(String.valueOf(Latitude),String.valueOf(Latitude));
            Log.d(String.valueOf(Longitude),String.valueOf(Longitude));
            Log.d(String.valueOf(Radius),String.valueOf(Radius));
            Log.d(String.valueOf(Speed),String.valueOf(Speed));
        }
       public void PackXMLAll()
        {
            try
            {
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

                Document doc = docBuilder.newDocument();

                Element rootElement = doc.createElement("coordinate");
                doc.appendChild(rootElement);

                SimpleDateFormat formating = new SimpleDateFormat("HH:mm:ss:SSS");
                Element XMLTimeStamp = doc.createElement("TimeStamp");
                XMLTimeStamp.appendChild(doc.createTextNode(formating.format(Timestamp)));
                rootElement.appendChild(XMLTimeStamp);

                Element XMLIMEI = doc.createElement("IMEI");
                XMLTimeStamp.appendChild(doc.createTextNode(String.valueOf(IMEI)));
                rootElement.appendChild(XMLIMEI);

                Element XMLInterval = doc.createElement("Interval");
                XMLInterval.appendChild(doc.createTextNode(String.valueOf(Interval)));
                rootElement.appendChild(XMLInterval);

                Element XMLComment = doc.createElement("Comment");
                XMLComment.appendChild(doc.createTextNode(String.valueOf(Comment)));
                rootElement.appendChild(XMLComment);

                Element XMLLatitude = doc.createElement("Latitude");
                XMLLatitude.appendChild(doc.createTextNode(String.valueOf(Latitude)));
                rootElement.appendChild(XMLLatitude);

                Element XMLLongtitude = doc.createElement("Longtitude");
                XMLLongtitude.appendChild(doc.createTextNode(String.valueOf(Longitude)));
                rootElement.appendChild(XMLLongtitude);

                Element XMLRadius = doc.createElement("Radius");
                XMLRadius.appendChild(doc.createTextNode(String.valueOf(Radius)));
                rootElement.appendChild(XMLRadius);

                Element XMLSpeed = doc.createElement("Speed");
                XMLSpeed.appendChild(doc.createTextNode(String.valueOf(Speed)));
                rootElement.appendChild(XMLSpeed);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();


                //Если SD карты нет
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                {
                    Log.d("CheckSD","NoSD");
                    File sdPath = Environment.getExternalStorageDirectory();
                    sdPath = new File(sdPath.getAbsolutePath()+"/"+DIR_SD);
                    File sdFile = new File(sdPath,FILE_NAME);

                    DOMSource source = new DOMSource(doc);
                    try
                    {
                        StreamResult result = new StreamResult(sdFile);
                        BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                        transformer.transform(source,result);
                        bw.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }


                }
                //Если SD карта есть
                else
                {
                    Log.d("CheckSD","SD");
                    File sdPath = Environment.getExternalStorageDirectory();
                    sdPath = new File(sdPath.getAbsolutePath()+"/"+DIR_SD);
                    sdPath.mkdirs();
                    File sdFile = new File(sdPath,FILE_NAME);

                    DOMSource source = new DOMSource(doc);

                        StringWriter sw = new StringWriter();
                        StreamResult result = new StreamResult(sw);

                        transformer.transform(source,result);


                        try
                        {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile,true));
                            bw.write(sw.toString());
                            bw.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                }

            }
            catch (ParserConfigurationException pce)
            {
                pce.printStackTrace();
            }
            catch (TransformerException tfe)
            {
                tfe.printStackTrace();
            }


        }

        @Override
       public String toString()
       {

           StringWriter sw = new StringWriter();
           try {
               DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
               DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

               Document doc;

               File sdPath = Environment.getExternalStorageDirectory();
               sdPath = new File(sdPath.getAbsolutePath()+"/"+DIR_SD+"/"+FILE_NAME);

               Element rootElement;
               if (sdPath.exists()&&sdPath.isFile())
               {
                   Log.d("File","found");
                   doc = docBuilder.parse(sdPath);
                   rootElement = doc.getDocumentElement();
               }
               else
               {
                   Log.d("File","notfound");
                   doc = docBuilder.newDocument();
                   rootElement = doc.createElement("root");
                   doc.appendChild(rootElement);
               }

               Element coordElement = doc.createElement("coordinate");
               rootElement.appendChild(coordElement);

               SimpleDateFormat formating = new SimpleDateFormat("HH:mm:ss:SSS");
               Element XMLTimeStamp = doc.createElement("TimeStamp");
               XMLTimeStamp.appendChild(doc.createTextNode(formating.format(Timestamp)));
               coordElement.appendChild(XMLTimeStamp);

               Element XMLIMEI = doc.createElement("IMEI");
               XMLTimeStamp.appendChild(doc.createTextNode(String.valueOf(IMEI)));
               coordElement.appendChild(XMLIMEI);

               Element XMLInterval = doc.createElement("Interval");
               XMLInterval.appendChild(doc.createTextNode(String.valueOf(Interval)));
               coordElement.appendChild(XMLInterval);

               Element XMLComment = doc.createElement("Comment");
               XMLComment.appendChild(doc.createTextNode(String.valueOf(Comment)));
               coordElement.appendChild(XMLComment);

               Element XMLLatitude = doc.createElement("Latitude");
               XMLLatitude.appendChild(doc.createTextNode(String.valueOf(Latitude)));
               coordElement.appendChild(XMLLatitude);

               Element XMLLongtitude = doc.createElement("Longtitude");
               XMLLongtitude.appendChild(doc.createTextNode(String.valueOf(Longitude)));
               coordElement.appendChild(XMLLongtitude);

               Element XMLRadius = doc.createElement("Radius");
               XMLRadius.appendChild(doc.createTextNode(String.valueOf(Radius)));
               coordElement.appendChild(XMLRadius);

               Element XMLSpeed = doc.createElement("Speed");
               XMLSpeed.appendChild(doc.createTextNode(String.valueOf(Speed)));
               coordElement.appendChild(XMLSpeed);

               TransformerFactory transformerFactory = TransformerFactory.newInstance();
               Transformer transformer = transformerFactory.newTransformer();
               transformer.setOutputProperty(OutputKeys.INDENT,"yes");
               transformer.transform(new DOMSource(doc),new StreamResult(sw));
           }
           catch (ParserConfigurationException pce)
           {
               pce.printStackTrace();
           }
           catch (FileNotFoundException fnf)
           {
               fnf.printStackTrace();
           }
           catch (SAXException sax)
           {
               sax.printStackTrace();
           }
           catch (IOException io)
           {
               io.printStackTrace();
           }
           catch (TransformerException tfe)
           {
               tfe.printStackTrace();
           }
            Log.d("sw",sw.toString());
           return sw.toString();
       }
       public void writeString(String str)
       {
           //Если SD карты нет
           if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
           {
               Log.d("CheckSD","NoSD");try {
               // отрываем поток для записи
               BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                       openFileOutput(FILE_NAME, MODE_APPEND)));
               // пишем данные
               bw.write(str);
               // закрываем поток
               bw.close();
               Log.d("NoSD", "Файл записан");

           } catch (IOException e) {
               e.printStackTrace();
           }

           }
           //Если SD карта есть
           else
           {
               Log.d("CheckSD","SD");
               File sdPath = Environment.getExternalStorageDirectory();
               sdPath = new File(sdPath.getAbsolutePath()+"/"+DIR_SD);
               sdPath.mkdirs();
               File sdFile = new File(sdPath,FILE_NAME);

               try
               {
                   BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile,true));
                   bw.write(str);
                   bw.close();
                   Log.d(str,str);
                   Log.d("SD", "Файл записан");
               }
               catch (IOException e)
               {
                   e.printStackTrace();
               }
           }
       }
       public void setCommentParameters(Location location,String comment)
       {
           if (location != null)
           {
               Timestamp = location.getTime();
               TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
               IMEI = telephonyManager.getDeviceId();
               Interval = 0;
               Comment = comment;
               Latitude = location.getLatitude();
               Longitude = location.getLongitude();
               Radius = location.getAccuracy();
               Speed = location.getSpeed();
           }
       }
       public void writeComment(Location location,String comment)
       {
           this.setCommentParameters(location,comment);
           this.writeString(this.toString());
       }
    }
    */
///////////////////////////////////////////////////////////////////////////////////
    class XMLpackTimer extends TimerTask
    {
        @Override
        public void run()
        {
            if (localLocation != null)
            {

                recordLog.add(Record.parse(localLocation,mTelephonyManager.getDeviceId()));
                Log.d("Count",String.valueOf(recordLog.count()));
            }
        }
    }

    /*public  String locationToString(Location location)
    {
        if (location != null)
        {
            //SimpleDateFormat formating = new SimpleDateFormat("HH:mm:ss:SSS");
            SimpleDateFormat formating = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            //Log.d("TimeStampIn",String.valueOf(location.getTime()));
            StringBuilder str = new StringBuilder("<coordinate>"+System.getProperty("line.separator"));
            str.append("<TimeStamp>" + formating.format(location.getTime()) +"</TimeStamp>"+System.getProperty("line.separator"));
            str.append("<IMEI>" + String.valueOf(telephonyManager.getDeviceId()) + "</IMEI>"+System.getProperty("line.separator"));
            str.append("<Interval>" + "30" + "</Interval>"+System.getProperty("line.separator"));
            str.append("<Comment>" + "" + "</Comment>"+System.getProperty("line.separator"));
            str.append("<Latitude>" + String.valueOf(location.getLatitude()) + "</Latitude>"+System.getProperty("line.separator"));
            str.append("<Longitude>" + String.valueOf(location.getLongitude()) + "</Longitude>"+System.getProperty("line.separator"));
            str.append("<Radius>" + String.valueOf(location.getAccuracy()) + "</Radius>"+System.getProperty("line.separator"));
            str.append("<Speed>" + String.valueOf(location.getSpeed()) + "</Speed>"+System.getProperty("line.separator"));
            str.append("</coordinate>"+System.getProperty("line.separator"));
            return str.toString();
        }
        return null;
    }
    public String locationToString(Location location, String comment)
    {
        if (location != null)
        {
            SimpleDateFormat formating = new SimpleDateFormat("HH:mm:ss.SSS");
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            StringBuilder str = new StringBuilder("<coordinate>"+System.getProperty("line.separator"));
            str.append("<TimeStamp>" + formating.format(location.getTime()) +"</TimeStamp>"+System.getProperty("line.separator"));
            str.append("<IMEI>" + String.valueOf(telephonyManager.getDeviceId()) + "</IMEI>"+System.getProperty("line.separator"));
            str.append("<Interval>" + "0" + "</Interval>"+System.getProperty("line.separator"));
            str.append("<Comment>" + comment + "</Comment>"+System.getProperty("line.separator"));
            str.append("<Latitude>" + String.valueOf(location.getLatitude()) + "</Latitude>"+System.getProperty("line.separator"));
            str.append("<Longitude>" + String.valueOf(location.getLongitude()) + "</Longitude>"+System.getProperty("line.separator"));
            str.append("<Radius>" + String.valueOf(location.getAccuracy()) + "</Radius>"+System.getProperty("line.separator"));
            str.append("<Speed>" + String.valueOf(location.getSpeed()) + "</Speed>"+System.getProperty("line.separator"));
            str.append("</coordinate>"+System.getProperty("line.separator"));
            return str.toString();
        }
        return null;
    }
    */
}
