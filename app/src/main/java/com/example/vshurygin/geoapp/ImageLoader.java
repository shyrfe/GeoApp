package com.example.vshurygin.geoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

/**
 * Created by vshurygin on 11.08.2016.
 */
public class ImageLoader
{
    private Vector<ImageView> mDownloadedView = new Vector<ImageView>();

    private boolean findView(ImageView _view)
    {
        for (int i = 0; i < mDownloadedView.size(); i++)
        {
            if (mDownloadedView.elementAt(i).equals(_view))
            {
                return true;
            }
        }
        return false;
    }

    private static void fileSave(InputStream is, FileOutputStream outputStream) {
        int i;
        try {
            while ((i = is.read()) != -1) {
                outputStream.write(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap mDownloadImage(final Context _context,final int _cacheTime,final String _url,final ImageView _bmImageView)
    {
        Bitmap bitmap = null;
        if (_cacheTime != 0) {
            /*File file = new File(_context.getExternalCacheDir(), _url
                    + ".cache");*/
            File dir = Environment.getExternalStorageDirectory();//new File(_context.getExternalCacheDir()+ "/geoApp/imageCache/");
            dir = new File(dir.getAbsolutePath()+"/GeoAppDir/");
            dir.mkdirs();
            File file = new File(dir, _url
                    + ".cache");

            long time = new Date().getTime() / 1000;
            long timeLastModifed = file.lastModified() / 1000;

            try {
                if (file.exists()) {
                    if (timeLastModifed + _cacheTime < time) {
                        file.delete();
                        file.createNewFile();
                        fileSave(new URL(_url).openStream(),
                                new FileOutputStream(file));
                    }
                } else {
                    //file.mkdirs();
                    file.createNewFile();
                    fileSave(new URL(_url).openStream(), new FileOutputStream(
                            file));
                }
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmap == null) {
                file.delete();
            }
        } else {
            try {
                bitmap = BitmapFactory.decodeStream(new URL(_url).openStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (_bmImageView != null) {
            mDownloadedView.remove(_bmImageView);
        }
        return bitmap;
    }

    public void LoadImage(final Context _context,final int _cacheTime,final String _url,final ImageView _bmImageView)
    {
        if (_bmImageView != null)
        {
            if (findView(_bmImageView))
            {
                return;
            }
            mDownloadedView.add(_bmImageView);
        }

        new AsyncTask<String,Void,Bitmap>()
        {
            protected Bitmap doInBackground(String... iUrl) {
                return mDownloadImage(_context, _cacheTime, iUrl[0], _bmImageView);
            }
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (_bmImageView != null) {
                    _bmImageView.setImageBitmap(result);
                }
            }
        }.execute(new String[] {_url});
        //new DownloadImageTask(_bmImage).execute(_url);
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
