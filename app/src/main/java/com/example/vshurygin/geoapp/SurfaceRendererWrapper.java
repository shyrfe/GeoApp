package com.example.vshurygin.geoapp;

import android.opengl.GLSurfaceView;
import static android.opengl.GLES20.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;




/**
 * Created by vshurygin on 20.07.2016.
 */
public class SurfaceRendererWrapper implements GLSurfaceView.Renderer {
    static {System.loadLibrary("OpenGLObjectModel");}

    public static native void on_surface_created();
    public static native void on_surface_changed(int width, int height);
    public static native void on_draw_frame();

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        on_surface_created();
        //glClearColor(1.0f,0.0f,0.0f,0.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
            on_surface_changed(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        on_draw_frame();
        //glClear(GL_COLOR_BUFFER_BIT);
    }
}
