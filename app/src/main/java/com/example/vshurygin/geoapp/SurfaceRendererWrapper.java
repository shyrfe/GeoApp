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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SurfaceRendererWrapper implements GLSurfaceView.Renderer {

    static {System.loadLibrary("OpenGLObjectModel");}

    private final Context context;

    public static native void on_surface_created();
    public static native void on_surface_changed(int width, int height);
    public static native void on_draw_frame();

/////////////////////////////////////////////////////////////////////////////////////////////////////////
    /*private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private FloatBuffer mTriangle1Vertices;
    private FloatBuffer mTriangle2Vertices;
    private FloatBuffer mTriangle3Vertices;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;

    private final int mBytesPerFloat = 4;
    private final int mStrideBytes = 7 * mBytesPerFloat;
    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
final float[] triangle1VerticesData = {
        // X, Y, Z,
        // R, G, B, A
        -0.5f, -0.25f, 0.0f,
        1.0f, 0.0f, 0.0f, 1.0f,

        0.5f, -0.25f, 0.0f,
        0.0f, 0.0f, 1.0f, 1.0f,

        0.0f, 0.559016994f, 0.0f,
        0.0f, 1.0f, 0.0f, 1.0f};

    // This triangle is yellow, cyan, and magenta.
    final float[] triangle2VerticesData = {
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            1.0f, 0.0f, 1.0f, 1.0f};

    // This triangle is white, gray, and black.
    final float[] triangle3VerticesData = {
            // X, Y, Z,
            // R, G, B, A
            -0.5f, -0.25f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,

            0.5f, -0.25f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f,

            0.0f, 0.559016994f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f};


*/
    public SurfaceRendererWrapper (Context context)
    {this.context = context;}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
/////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* // Initialize the buffers.
        mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangle3Vertices = ByteBuffer.allocateDirect(triangle3VerticesData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTriangle1Vertices.put(triangle1VerticesData).position(0);
        mTriangle2Vertices.put(triangle2VerticesData).position(0);
        mTriangle3Vertices.put(triangle3VerticesData).position(0);*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"

                        + "attribute vec4 a_Position;     \n"
                        + "attribute vec4 a_Color;        \n"

                        + "varying vec4 v_Color;          \n"

                        + "void main()                    \n"
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"
                        + "   gl_Position = u_MVPMatrix   \n"
                        + "               * a_Position;   \n"
                        + "}                              \n";

        final String fragmentShader =
                "precision mediump float;       \n"
                        + "varying vec4 v_Color;          \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"
                        + "}                              \n";

        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0)
        {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            GLES20.glCompileShader(vertexShaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0)
        {
            throw new RuntimeException("Error creating vertex shader.");
        }
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0)
        {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0)
            {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0)
        {
            throw new RuntimeException("Error creating fragment shader.");
        }

        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0)
        {
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
            GLES20.glLinkProgram(programHandle);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0)
            {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        GLES20.glUseProgram(programHandle);*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        gl.glHint(GL10.GL_LINE_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
        gl.glEnable(GL10.GL_LINE_SMOOTH);
        on_surface_created();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
/////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* GLES20.glViewport(0, 0, width, height);
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);*/
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        on_surface_changed(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
/////////////////////////////////////////////////////////////////////////////////////////////////////////
        on_draw_frame();
        gl.glLoadIdentity();
/////////////////////////////////////////////////////////////////////////////////////////////////////////
       /* long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000.0f) * ((int) time);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle1Vertices);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle2Vertices);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriangle3Vertices);*/
    }

   /* private void drawTriangle(final FloatBuffer aTriangleBuffer)
    {
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                mStrideBytes, aTriangleBuffer);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }*/
}
