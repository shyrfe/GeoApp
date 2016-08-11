package com.example.vshurygin.geoapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class SearchResultActivity extends AppCompatActivity
{
    Intent mIntent;
    GooglePlacesSearch mGooglePlacesSearch;
    ListView mSearchResultViewList;
    PlaceInfo[] mResult;
    String[] mResultNames;
    final int RADIUS = 1000;
    final String PLACES_TYPE = "food|store|liquor_store";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
////////////////////////////////////////////////////////////////////////////////////////////////
        mIntent = getIntent();
        double latitude = mIntent.getDoubleExtra("latitude",0);
        double longitude = mIntent.getDoubleExtra("longitude",0);
////////////////////////////////////////////////////////////////////////////////////////////////
        mSearchResultViewList = (ListView)findViewById(R.id.SearchResultViewList);
        mSearchResultViewList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
                        mResultNames = new String[mResult.length];
                        for (int i = 0; i < mResultNames.length; i++)
                        {
                            mResultNames[i] = mResult[i].getName();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    ArrayList<PlaceInfo> placesInfoArray = new ArrayList<PlaceInfo>();
                                    placesInfoArray.addAll(Arrays.asList(mResult));
                                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchResultActivity.this,R.layout.search_result_view_list_item,R.id.searchPlaceName,mResultNames);
                                    PlaceInfoAdapter placeInfoAdapter= new PlaceInfoAdapter(SearchResultActivity.this,placesInfoArray);
                                    mSearchResultViewList.setAdapter(placeInfoAdapter);
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
        mSearchResultViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(SearchResultActivity.this,SearchItemDetail.class);
                intent.putExtra("PlaceInfo",mResult[position]);
                startActivity(intent);
                /*Log.d("List",String.valueOf(position));*/
            }
        });
    }

}
