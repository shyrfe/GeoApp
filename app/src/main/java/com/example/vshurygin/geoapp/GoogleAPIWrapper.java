package com.example.vshurygin.geoapp;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by vshurygin on 05.08.2016.
 */
public class GoogleAPIWrapper implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks
{
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    GoogleAPIWrapper(Context context)
    {
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(mContext,"Connetion to Google API suspended",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnected(Bundle connectionHint)
    {
        Toast.makeText(mContext,"Connetion to Google API success",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        Toast.makeText(mContext,"Connetion to Google API failed!",Toast.LENGTH_SHORT).show();
    }
}
