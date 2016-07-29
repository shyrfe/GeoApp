//
// Created by vshurygin on 20.07.2016.
//

#ifndef GEOAPP_OPENGLOBJECTMODEL_H
#define GEOAPP_OPENGLOBJECTMODEL_H

#define POJECTION_AND_VIEW_MATRIX_SIZE 16


#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
//#include <EGL/egl.h>
#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>

#include "ShaderUtils.h"

#endif GEOAPP_OPENGLOBJECTMODEL_H

#define  LOG_TAG    "Log From JNI"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
JNIEnv* pJNIenv;
jobject* pSurfaceRendererWrapper; //объект класса SurfaceRendererWrapper, нужен для вызова методов класса
JavaVM* mJVM;
jclass* pSurfaceRendererWrapperClass;

const int POSITION_COUNT = 3; //количество координат которые мы берём из массива вершин 2-xy,3 - xyz,4-xyzw
static int uColorLocation; //id для u_Color в шейдере
static int aPositionLocation; //id для a_Position в шейдере
static int uMatrixLocation; //id для uMatrix в шейдере

//static const POJECTION_AND_VIEW_MATRIX_SIZE = 16;


static GLfloat mProjectionMatrix[POJECTION_AND_VIEW_MATRIX_SIZE];
static GLfloat* mViewMatrix;
static GLfloat* mMatrix;

float centerX;
float centerY;
float centerZ;

float upX;
float upY;
float upZ;

static GLuint programID;
#define VERTEX_S 0.4f
#define VERTEX_D 0.9f
#define VERTEX_L 3.0f
static const GLfloat G_vertex_buffer_data[] = {

        -2*VERTEX_S, -VERTEX_S, VERTEX_D,
        2*VERTEX_S, -VERTEX_S, VERTEX_D,
        0, VERTEX_S, VERTEX_D,

        // второй треугольник
        -2*VERTEX_S, -VERTEX_S, -VERTEX_D,
        2*VERTEX_S, -VERTEX_S, -VERTEX_D,
        0, VERTEX_S, -VERTEX_D,

        // третий треугольник
        VERTEX_D, -VERTEX_S, -2*VERTEX_S,
        VERTEX_D, -VERTEX_S, 2*VERTEX_S,
        VERTEX_D, VERTEX_S, 0,

        // четвертый треугольник
        -VERTEX_D, -VERTEX_S, -2*VERTEX_S,
        -VERTEX_D, -VERTEX_S, 2*VERTEX_S,
        -VERTEX_D, VERTEX_S, 0,

        // ось X
        -VERTEX_L, 0,0,
        VERTEX_L,0,0,

        // ось Y
        0,-VERTEX_L,0,
        0,VERTEX_L,0,

        // ось Z
        0,0,-VERTEX_L,
        0,0,VERTEX_L
};

void onSurfaceCreated();
void onSurfaceChanged(int width, int height);
void onDrawFrame();
void createProjectionMatrix(int width, int height);
void bindMatrix();
void createViewMatrix();