package com.example.vshurygin.geoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class SearchItemDetail extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_item_detail);
        Intent intent = getIntent();
        PlaceInfo placeInfo = intent.getParcelableExtra("PlaceInfo");
        if (placeInfo.getImageAdress() != null)
        {
            new DownloadImageTask((ImageView)findViewById(R.id.PlaceImage)).execute(placeInfo.getImageAdress());
        }

        Log.d("Item",String.valueOf(placeInfo.getName()));
        Log.d("Item",String.valueOf(placeInfo.getIconAdress()));
        Log.d("Item",String.valueOf(placeInfo.getImageAdress()));
        Log.d("Item",String.valueOf(placeInfo.getPhotoReference()));
        Log.d("Item",String.valueOf(placeInfo.getVicinity()));

        /*https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJ0eWadYY_NEERmblLNBm5O10&key=AIzaSyCHpwqjnrTJVCh4gv0szpdibd6KGl5_vjg*/
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.d("Ошибка изображения", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
