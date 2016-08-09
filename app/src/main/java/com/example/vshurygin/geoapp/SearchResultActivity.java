package com.example.vshurygin.geoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.util.Timer;
import java.util.TimerTask;

public class SearchResultActivity extends AppCompatActivity
{

    GooglePlacesSearch mGooglePlacesSearch;
    ListView mSearchResultViewList;
    String[] mResult;
    final int RADIUS = 1000;
    final String PLACES_TYPE = "food|store|liquor_store";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
////////////////////////////////////////////////////////////////////////////////////////////////
        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("latitude",0);
        double longitude = intent.getDoubleExtra("longitude",0);
////////////////////////////////////////////////////////////////////////////////////////////////
        mSearchResultViewList = (ListView)findViewById(R.id.SearchResultViewList);

////////////////////////////////////////////////////////////////////////////////////////////////
        mGooglePlacesSearch = new GooglePlacesSearch();
        mGooglePlacesSearch.setPlaces(latitude,longitude,RADIUS,PLACES_TYPE);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try
                {
                    mResult = mGooglePlacesSearch.getPlaces();

                    if (mResult != null)
                    {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchResultActivity.this,R.layout.search_result_view_list_item,mResult);
                                    mSearchResultViewList.setAdapter(adapter);
                                }
                                catch (Exception e)
                                {e.printStackTrace();}

                            }
                        });
                    }
                }
                catch (Exception e)
                {e.printStackTrace();}


            }
        },1000);
////////////////////////////////////////////////////////////////////////////////////////////////
    }
}
