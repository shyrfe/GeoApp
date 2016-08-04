/**
 * Created by vshurygin on 20.07.2016.
 */
package com.example.vshurygin.geoapp;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import static android.opengl.GLES20.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.CopyOnWriteArrayList;

public class SurfaceRendererWrapper implements GLSurfaceView.Renderer {

    static {System.loadLibrary("OpenGLObjectModel");}

    private final Context context;

    static native void mSurfaceCreated(SurfaceRendererWrapper surfaceRendererWrapper);
    static native void mSurfaceChanged(SurfaceRendererWrapper surfaceRendererWrapper,int width, int height);
    static native void mDrawframe(SurfaceRendererWrapper surfaceRendererWrapper);
    static native void addObject(float _x, float _y);
    static native void clearAllData();

    private int mWidth;
    private int mHeight;

    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    float[] mMatrix = new float[16];

    private CopyOnWriteArrayList<MarkerPosition> mAllMarkers = new CopyOnWriteArrayList<>();

    public SurfaceRendererWrapper (Context context)
    {this.context = context;}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        mSurfaceCreated(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        mWidth = width;
        mHeight = height;

        mSurfaceChanged(this,width,height);

    }
    @Override
    public void onDrawFrame(GL10 gl)
    {
        for (int i = 0; i < mAllMarkers.size();i++)
        {
            addObject(mAllMarkers.get(i).getXInGLCoord(),mAllMarkers.get(i).getYInGLCoord());
        }
        mDrawframe(this);
    }

    public void add3DMarker(final int _x, final int _y)
    {
               /* if ((mWidth != 0) && (mHeight != 0))
                {
                    float x  = (1.0f-((float)_x/((float)mWidth/2.0f)))*(-1.0f);
                    float y = (1.0f-((float)_y/((float)mHeight/2.0f)));//(float)_y/(mHeight/2);
                    addObject(x,y);
                }*/
        if ((mWidth != 0) && (mHeight != 0))
        {
            mAllMarkers.add(new MarkerPosition(_x,_y));
        }

    }
    public void refresh3DMarker(int marker_n,int _x, int _y)
    {
        mAllMarkers.get(marker_n).setXY(_x,_y);
    }
    public void delete3DMarkers()
    {
        //mAllMarkers.clear();
        mAllMarkers = new CopyOnWriteArrayList<>();
    }
// C function Wrappers
    public float[] mFrustum()
    {
        float left = -1.0f;
        float right = 1.0f;
        float bottom = -1.0f;
        float top = 1.0f;
        float near = 2.0f;
        float far = 8.0f;
        float ratio = 1;

        float[] m = new float[16];

        Matrix.frustumM(m,0,left,right,bottom,top,near,far);
        return m;
    }
    public float[] createProjectionMatrix(float[] val)
    {
        float[] matrix = new float[16];
        Matrix.orthoM(matrix,0,val[0],val[1],val[2],val[3],val[4],val[5]);
        return matrix;
    }
    public float[] createViewMatrix() {
        float time = (float)(SystemClock.uptimeMillis()%1000)/1000;
        float angle = time *2 * (float)Math.PI;

        // точка положения камеры
        float eyeX = 0;//(float) (Math.cos(angle) * 2f);
        float eyeY = 0;
        float eyeZ = 1;//(float) (Math.sin(angle) * 2f);

        // точка направления камеры
        float centerX = 0;  //(float) (Math.cos(angle) * 2f);
        float centerY = 0;
        float centerZ = 0;

        // up-вектор
        float upX = 0;
        float upY = 1;
        float upZ = 0;

        float[] mViewMatrix = new float[16];

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        return mViewMatrix;
    }
    public void setProjectionMatrix(float[] pM)
    {
        if (pM == null)
        {
            Log.d("setProjectionMatrix","matrix is empty");
        }
        mProjectionMatrix = pM;
    }
    public void setViewMatrix(float[] vM)
    {
        if (vM == null)
        {
            Log.d("setViewMatrix","matrix is empty");
        }
        mViewMatrix = vM;
    }
    public float[] MultiplyProjectionMatrixAndViewMatrix()
    {
        if ((mProjectionMatrix != null)&& (mViewMatrix != null))
        {
            Matrix.multiplyMM(mMatrix,0,mProjectionMatrix,0,mViewMatrix,0);
        }
        return mMatrix;
    }
////////////////////////
    private class MarkerPosition
    {
        private int mX;
        private int mY;
        MarkerPosition(int _x , int _y)
        {
            mX = _x;
            mY = _y;
        }

        public int getX()
        {return mX;}
        public int getY()
        {return mY;}
        public void setX(int _x)
        {
            mX = _x;
        }
        public void setY(int _y) { mY = _y; }
        public void setXY(int _x, int _y)
        {
            mX = _x;
            mY = _y;
        }
        public float getXInGLCoord()
        {
                    return (1.0f-((float)mX/((float)mWidth/2.0f)))*(-1.0f);
        }
        public float getYInGLCoord()
        {
            return  (1.0f-((float)mY/((float)mHeight/2.0f)));
        }
    }
}

