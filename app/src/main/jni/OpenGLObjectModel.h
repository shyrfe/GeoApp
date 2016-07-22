//
// Created by vshurygin on 20.07.2016.
//

#ifndef GEOAPP_OPENGLOBJECTMODEL_H
#define GEOAPP_OPENGLOBJECTMODEL_H
#include <GLES2/gl2.h>
//#include <GLES2/gl2ext.h>
//#include <EGL/egl.h>
#include <stdlib.h>
#include <jni.h>
#endif GEOAPP_OPENGLOBJECTMODEL_H


GLuint programObject;
GLfloat mTriangleVertices[] = {0.0f,  0.5f, 0.0f,
                       -0.5f, -0.5f, 0.0f,
                       0.5f, -0.5f,  0.0f};

void onSurfaceCreated();
void onSurfaceChanged(int width, int height);
void onDrawFrame();

GLuint  LoadShader(const char* shaderSrc, GLenum type);
