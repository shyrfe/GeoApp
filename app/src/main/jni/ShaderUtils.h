//
// Created by vshurygin on 27.07.2016.
//
#include <GLES2/gl2.h>

#ifndef GEOAPP_SHADERUTILS_H
#define GEOAPP_SHADERUTILS_H


GLuint  LoadShader(GLenum type,const char* shaderSrc);
GLuint LoadProgramFromShaders();


#endif //GEOAPP_SHADERUTILS_H
