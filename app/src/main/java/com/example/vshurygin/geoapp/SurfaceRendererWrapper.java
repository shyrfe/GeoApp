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
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SurfaceRendererWrapper implements GLSurfaceView.Renderer {

    static {System.loadLibrary("OpenGLObjectModel");}

    private final Context context;

    /*public static native void nativeOnStart();
    public static native void nativeOnResume();
    public static native void nativeOnPause();
    public static native void nativeOnStop();
    public static native void nativeSetSurface(Surface surface);*/

    public static native void mSurfaceCreated();
    public static native void mSurfaceChanged(int width, int height);
    public static native void mDrawframe();


    public SurfaceRendererWrapper (Context context)
    {this.context = context;}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        //gl.glClearColor(1,1,1,0.5f);
/////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(0,0,0,0);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        mSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        mSurfaceChanged(width,height);
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
}
