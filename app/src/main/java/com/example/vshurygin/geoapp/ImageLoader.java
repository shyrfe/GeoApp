package com.example.vshurygin.geoapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by vshurygin on 11.08.2016.
 */
public class ImageLoader
{
    final int DEFAULT_IMAGE_ID = R.mipmap.default_placeinfo_image;
    MemoryCache mMemoryCache = new MemoryCache();
    FileCache mFileCache;

    private Map<ImageView,String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView,String>());
    ExecutorService mExecutorService;

    private int mMaxWidth = 100;
    private int mMaxHeight = 100;

    Handler mHandler = new Handler();

    public ImageLoader(Context _context)
    {
        mFileCache = new FileCache(_context);
        mExecutorService = Executors.newFixedThreadPool(5);
    }

    public void setBitMapWidthAndHeight(int _w, int _h)
    {
        if((_w != 0) && (_h != 0))
        {
            mMaxWidth = _w;
            mMaxHeight = _h;
        }
    }
    public void DisplayImage(String _url, ImageView _imageView)
    {
        /*mMaxWidth = _imageView.getWidth();
        mMaxHeight = _imageView.getHeight();*/

        mImageViews.put(_imageView,_url);

        Bitmap bitmap = mMemoryCache.get(_url);

        if(bitmap != null)
        {
            /*bitmap = getRoundedCornerBitmap(bitmap);*/
            _imageView.setImageBitmap(bitmap);
        }
        else
        {
            queuePhoto(_url,_imageView);
            _imageView.setImageResource(DEFAULT_IMAGE_ID);
        }
    }

    private void queuePhoto(String _url, ImageView _imageView)
    {
        PhotoToLoad p = new PhotoToLoad(_url,_imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i)
        {
        url=u;
        imageView=i;
        }
    }

    class PhotosLoader implements Runnable
    {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad)
        {
            this.photoToLoad=photoToLoad;
        }

        @Override
        public void run() {
            try
            {
                if(imageViewReused(photoToLoad))
                {
                    return;
                }

                Bitmap bmp = getBitmap(photoToLoad.url);
                bmp = ScaleBitmap(bmp);
                bmp = getRoundedCornerBitmap(bmp);
                mMemoryCache.put(photoToLoad.url, bmp);

                if(imageViewReused(photoToLoad))
                {
                    return;
                }

                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                mHandler.post(bd);

            }
            catch(Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    private Bitmap getBitmap(String _url)
    {
        File f = mFileCache.getFile(_url);
        Bitmap b = decodeFile(f);
        if(b != null)
        {
            return b;
        }
        try {

            Bitmap bitmap=null;
            URL imageUrl = new URL(_url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;

        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
            if(ex instanceof OutOfMemoryError)
            {
                mMemoryCache.clear();
            }

            return null;
        }
    }

    private Bitmap decodeFile(File f)
    {
        try
        {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();

            final int REQUIRED_SIZE=85;

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while (true)
            {
                if (width_tmp/2 < REQUIRED_SIZE || height_tmp/2 < REQUIRED_SIZE)
                {
                    break;
                }
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2=new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2,null,o2);
            stream2.close();
            return  bitmap;
        }
        catch (FileNotFoundException e)
        {e.printStackTrace();}
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    boolean imageViewReused (PhotoToLoad photoToLoad)
    {
        String tag = mImageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
        {
            return true;
        }
        return false;
    }

    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;

            // Show bitmap on UI
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(DEFAULT_IMAGE_ID);
        }
    }

    public void clearCache() {
        //Clear cache directory downloaded images and stored data in maps
        mMemoryCache.clear();
        mFileCache.clear();
    }

    public  void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {

            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                //Read byte from input stream

                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);



        return output;
    }

    private Bitmap ScaleBitmap(Bitmap _bitmap)
    {

        _bitmap = Bitmap.createScaledBitmap(_bitmap,mMaxWidth,mMaxHeight,true);
        return _bitmap;
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




    private class FileCache
    {
        private File mCacheDir;
        public FileCache(Context _context)
        {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                mCacheDir = new File(Environment.getExternalStorageDirectory(),"GeoApp");
            }
            else
            {
                mCacheDir=_context.getCacheDir();
            }

            if(!mCacheDir.exists())
            {
                mCacheDir.mkdirs();
            }
        }

        public File getFile(String _url)
        {
            String filename = String.valueOf(_url.hashCode());
            File cacheFile = new File (mCacheDir,filename);
            return cacheFile;
        }

        public void clear()
        {
            File[] files = mCacheDir.listFiles();
            if(files == null)
            {
                return;
            }
            for (File f : files)
            {
                f.delete();
            }
        }
    }

    private class MemoryCache
    {
        //private static final String TAG = "MemoryCache";

        private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,1.5f,true));
        private long mSize = 0;
        private long mLimit = 1000000;

        public MemoryCache()
        {
            setLimit(Runtime.getRuntime().maxMemory()/4);
        }
        public void setLimit(long _newLimit)
        {
            mLimit = _newLimit;
        }

        public void put(String _id, Bitmap _bitmap)
        {
            try
            {
                if(mCache.containsKey(_id))
                {
                    mSize -=getSizeInBytes(mCache.get(_id));
                }
                mCache.put(_id,_bitmap);
                mSize+=getSizeInBytes(_bitmap);
                checkSize();
            }
            catch (Throwable th)
            {
                th.printStackTrace();
            }
        }

        public Bitmap get (String _id)
        {
            try
            {
                if(!mCache.containsKey(_id))
                {
                    return null;
                }
                return mCache.get(_id);
            }
            catch (NullPointerException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }
        private void checkSize() {

            if(mSize>mLimit){
                Iterator<Map.Entry<String, Bitmap>> iter=mCache.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String, Bitmap> entry=iter.next();
                    mSize-=getSizeInBytes(entry.getValue());
                    iter.remove();
                    if(mSize<=mLimit)
                        break;
                }

            }
        }

        public void clear() {
            try{
                // Clear cache
                mCache.clear();
                mSize=0;
            }catch(NullPointerException ex){
                ex.printStackTrace();
            }
        }

        long getSizeInBytes(Bitmap bitmap) {
            if(bitmap==null)
                return 0;
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
}
/* private Vector<ImageView> mDownloadedView = new Vector<ImageView>();

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
            *//*File file = new File(_context.getExternalCacheDir(), _url
                    + ".cache");*//*
            File dir = Environment.getExternalStorageDirectory();//new File(_context.getExternalCacheDir()+ "/geoApp/imageCache/");
            dir = new File(dir.getAbsolutePath()+"/GeoAppDir/");
            if(!dir.exists())
            {
                dir.mkdirs();
            }

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
    }*/