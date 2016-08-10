package com.example.vshurygin.geoapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;

/**
 * Created by vshurygin on 10.08.2016.
 */
public class PlaceInfo implements Parcelable
{
    private String mName;
    private String mIconAdress;
    private String mImageAdress;
    private String mVicinity;
    private String mPhotoReference;
    private String mPlaceId;

    final private String KEY = "AIzaSyCHpwqjnrTJVCh4gv0szpdibd6KGl5_vjg";
    final private String MAX_WIDTH = "800";
    final private String GOOGLE_URL = "https://maps.googleapis.com/maps/api/place/photo?";

    PlaceInfo(String _name, String _iconAdress, String _vicinity,String _placeId,String _photo_reference)
    {
        mName = _name;
        mIconAdress = _iconAdress;
        mVicinity = _vicinity;
        mPlaceId = _placeId;
        mPhotoReference = _photo_reference;
        if (mPhotoReference != null)
        {
            mImageAdress = GOOGLE_URL+"maxwidth="+MAX_WIDTH+"&"+"photoreference="+mPhotoReference+"&"+"key="+KEY;
        }
        else
        {
            mImageAdress = null;
        }
    }

    public String getName()
    {
        return mName;
    }
    public String getIconAdress()
    {
        return mIconAdress;
    }
    public String getVicinity()
    {
        return mVicinity;
    }
    public String getPhotoReference() { return mPhotoReference; }
    public String getImageAdress() { return mImageAdress; }
    public String getPlaceId() { return mPlaceId; }
    public String getKey(){ return KEY; }

    public int describeContents() {
        return 0;
    }
    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeString(mIconAdress);
        parcel.writeString(mImageAdress);
        parcel.writeString(mVicinity);
        parcel.writeString(mPlaceId);
        parcel.writeString(mPhotoReference);
    }

    public static final Parcelable.Creator<PlaceInfo> CREATOR = new Parcelable.Creator<PlaceInfo>() {
        // распаковываем объект из Parcel
        public PlaceInfo createFromParcel(Parcel in) {

            return new PlaceInfo(in);
        }

        public PlaceInfo[] newArray(int size) {
            return new PlaceInfo[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private PlaceInfo(Parcel parcel) {

        mName = parcel.readString();
        mIconAdress = parcel.readString();
        mImageAdress = parcel.readString();
        mVicinity = parcel.readString();
        mPlaceId = parcel.readString();
        mPhotoReference = parcel.readString();
    }
}
