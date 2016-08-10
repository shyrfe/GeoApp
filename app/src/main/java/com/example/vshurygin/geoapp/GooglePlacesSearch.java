package com.example.vshurygin.geoapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by vshurygin on 08.08.2016.
 */
public class GooglePlacesSearch
{
    private PlaceInfo[] mPlaces = null;
    private final String GOOGLE_API_PLACE_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";

    GooglePlacesSearch() {}


    public void setPlaces(double _latitude, double _longitude, double _radius, String _types)
    {
        new ProgressTask(GOOGLE_API_PLACE_SEARCH_URL,_latitude,_longitude,_radius,_types).execute();
    }

    public PlaceInfo[] getPlaces()
    {
        if (mPlaces != null)
        {
            return mPlaces;
        }
        return null;
    }


    class ProgressTask extends AsyncTask<String,Void,String>
    {
        private String mURL;
        private double mLatitude;
        private double mLongitude;
        private double mRadius;
        private String mTypes;

        ProgressTask(String _url, double _latitude, double _longitude, double _radius, String _types )
        {
            mURL = _url;
            mLatitude = _latitude;
            mLongitude = _longitude;
            mRadius = _radius;
            mTypes = _types;
        }
        private String prepareURL()
        {
            StringBuilder result = new StringBuilder();

            result.append(mURL);
            result.append("language=ru&");
            result.append("location="+mLatitude+","+mLongitude+"&");
            result.append("radius="+mRadius+"&");
            result.append("sensor=false&");
            if (mTypes != null)
            {
                result.append("type="+mTypes+"&");
            }
            result.append("key=AIzaSyCHpwqjnrTJVCh4gv0szpdibd6KGl5_vjg");

            return result.toString();
        }

        private  String getContent(String path)
        {
            BufferedReader reader = null;
            try
            {
                URL url = new URL(path);
                HttpURLConnection c=(HttpURLConnection)url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(10000);
                c.connect();
                reader= new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder buf=new StringBuilder();
                String line=null;
                while ((line=reader.readLine()) != null) {
                    //Log.d("PlacesSearch",line);
                    buf.append(line + "\n");
                }
                return(buf.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if(reader != null)
                {
                    try {
                        reader.close();
                    }
                    catch (Exception e)
                    {e.printStackTrace();}
                }
            }
            return null;
        }

        @Override
        protected String doInBackground(String... path)
        {
            String content;
            try
            {
                content = getContent(prepareURL());
                //Log.d("PlacesSearch",content);
                return content;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            JSONObject jsonObject = null;

            try
            {
                jsonObject = new JSONObject(result);
                JSONArray results = jsonObject.getJSONArray("results");

                ArrayList<PlaceInfo> placesArray = new ArrayList<>();
                for (int i = 0; i < results.length();i++)
                {
                    if ( !results.getJSONObject(i).isNull("photos"))
                    {
                        JSONArray photos = results.getJSONObject(i).getJSONArray("photos");
                        placesArray.add(new PlaceInfo(
                                results.getJSONObject(i).getString("name"),
                                results.getJSONObject(i).getString("icon"),
                                results.getJSONObject(i).getString("vicinity"),
                                results.getJSONObject(i).getString("place_id"),
                                photos.getJSONObject(0).getString("photo_reference")
                        ));
                    }
                    else
                    {
                        placesArray.add(new PlaceInfo(
                                results.getJSONObject(i).getString("name"),
                                results.getJSONObject(i).getString("icon"),
                                results.getJSONObject(i).getString("vicinity"),
                                results.getJSONObject(i).getString("place_id"),
                                null
                        ));
                    }

                    //Log.d("PlacesSearch",results.getJSONObject(i).getString("name"));
                }

                mPlaces = new PlaceInfo[placesArray.size()];
                mPlaces = placesArray.toArray(mPlaces);

            }
            catch (Exception e)
            {e.printStackTrace();}

            //Log.d("PlacesSearch",result);
        }
    }

}
