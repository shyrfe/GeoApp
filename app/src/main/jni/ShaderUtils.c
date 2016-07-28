//
// Created by vshurygin on 27.07.2016.
//

#include "ShaderUtils.h"

GLuint  LoadShader(GLenum type,const char* shaderSrc)
{
    GLuint shader;
    GLint compiled;

    shader = glCreateShader(type);

    if (shader == 0)
    {
        return 0;
    }

    glShaderSource(shader,1,&shaderSrc,NULL);
    glCompileShader(shader);
    glGetShaderiv(shader, GL_COMPILE_STATUS,&compiled);
    if(!compiled)
    {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

GLuint LoadProgramFromShaders()
{
    GLuint programObject;
    GLuint vertexShader;
    GLuint fragmentShader;
    GLint linked;

    char fShaderStr[] = "precision mediump float;"
            "uniform vec4 u_Color;"
            "void main(){"
            "    gl_FragColor = u_Color;"
            "}";

    char vShaderStr[] =
            "attribute vec4 a_Position;"
            "uniform mat4 u_Matrix;"
            "void main(){"
            "    gl_Position = u_Matrix * a_Position;"
            "    gl_PointSize = 5.0;"
            " }";

    vertexShader = LoadShader(GL_VERTEX_SHADER, vShaderStr);
    fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fShaderStr);

    programObject = glCreateProgram();

    glAttachShader(programObject,vertexShader);
    glAttachShader(programObject,fragmentShader);

    glBindAttribLocation(programObject, 0 ,"vPosition");
    glLinkProgram(programObject);
    return programObject;
}