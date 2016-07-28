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
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SurfaceRendererWrapper implements GLSurfaceView.Renderer {

    static {System.loadLibrary("OpenGLObjectModel");}

    private final Context context;

    public static native void mSurfaceCreated(SurfaceRendererWrapper surfaceRendererWrapper);
    public static native void mSurfaceChanged(SurfaceRendererWrapper surfaceRendererWrapper,int width, int height);
    public static native void mDrawframe();

    private float[] mProjectionMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    float[] mMatrix = new float[16];


    public SurfaceRendererWrapper (Context context)
    {this.context = context;}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
/////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(0,0,0,0);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        mSurfaceCreated(this);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        mSurfaceChanged(this,width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {

        mDrawframe();
        //drawframe();
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        //on_draw_frame();

        //gl.glLoadIdentity();
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
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
    public float[] createViewMatrix() {
        // точка положения камеры
        float eyeX = 0;
        float eyeY = 0;
        float eyeZ = 1;

        // точка направления камеры
        float centerX = 0;
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
        if (pM != null)
        {
            Log.d("setProjectionMatrix","OK");
        }
        mProjectionMatrix = pM;
    }
    public void setViewMatrix(float[] vM)
    {
        if (vM != null)
        {
            Log.d("setViewMatrix","OK");
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
}
