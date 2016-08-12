package com.example.vshurygin.geoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by vshurygin on 11.08.2016.
 */
public class PlaceInfoAdapter extends BaseAdapter
{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<PlaceInfo> mPlaces;
    private ImageLoader mImageLoader;

    PlaceInfoAdapter(Context _context, ArrayList<PlaceInfo> _places)
    {
        mContext = _context;
        mPlaces = _places;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageLoader = new ImageLoader(mContext);
    }

    @Override
    public int getCount()
    {
        return mPlaces.size();
    }

    @Override
    public PlaceInfo getItem(int _position)
    {
        return mPlaces.get(_position);
    }

    @Override
    public long getItemId(int _position)
    {
        return _position;
    }

    @Override
    public View getView(int _position, View _convertView, ViewGroup _parent)
    {
        View view = _convertView;
        if (view == null)
        {
            view = mLayoutInflater.inflate(R.layout.search_result_view_list_item,_parent,false);
        }
        PlaceInfo p = getPlaceInfo(_position);

        ((TextView) view.findViewById(R.id.searchPlaceName)).setText(p.getName());
        ((TextView) view.findViewById(R.id.place_address)).setText(p.getVicinity());

        if (p.getImageAdress() != null)
        {
            //new DownloadImageTask((ImageView)view.findViewById(R.id.placeImage)).execute(p.getImageAdress());
            //mImageLoader.LoadImage(mContext,3600,p.getImageAdress(),((ImageView)view.findViewById(R.id.placeImage)));
            ImageView imageView = (ImageView)view.findViewById(R.id.placeImage);
            mImageLoader.setBitMapWidthAndHeight(imageView.getWidth(),imageView.getHeight());
            mImageLoader.DisplayImage(p.getImageAdress(),imageView);
        }
        else
        {
            ((ImageView)view.findViewById(R.id.placeImage)).setImageResource(R.mipmap.default_placeinfo_image);
        }
        return view;
    }

    PlaceInfo getPlaceInfo(int _position)
    {
        return ((PlaceInfo) getItem(_position));
    }


}
