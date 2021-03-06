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
#include <math.h>
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


//static GLfloat mProjectionMatrix[POJECTION_AND_VIEW_MATRIX_SIZE];
static GLfloat* mProjectionMatrix;
static GLfloat* mViewMatrix;
static GLfloat* mMatrix;

float centerX;
float centerY;
float centerZ;

float upX;
float upY;
float upZ;

static GLuint programID;


static int global_vertex_buffer_size = 0;
static GLfloat* G_vertex_buffer_data;
static float* cubePositions;

//float cubePositions[6] = {-0.5f,0.5f,-1.0f,0.5f,-0.5f,-1.0f};
#define LOCAL_CUBE_VERTICES_BUFFER_SIZE 27
float localCubeVertices[] = {
         0,    0,   0,
        -0.05f,0.15f,0.05f,
         0.05f,0.15f,0.05f,

         0,    0,   0,
         -0.05f,0.15f,0.05f,
         0,    0.15f,-0.05f,

         0,    0,   0,
         0,    0.15f,-0.05f,
         0.05f,0.15f,0.05f
};


void onSurfaceCreated();
void onSurfaceChanged(int width, int height);
void onDrawFrame();
void createProjectionMatrix(int width, int height);
void addObjectVertexData(float _x , float _y, float _z);
void bindMatrix();
void createViewMatrix();