#include "OpenGLObjectModel.h"

JNIEXPORT jstring JNICALL
Java_com_example_vshurygin_geoapp_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance)
{
    //glClearColor(1.0f,0.0f,0.0f,0.0f);
    return (*env)->NewStringUTF(env, "HelloJNI");
}

static const GLfloat G_vertex_buffer_data[] = {
        -0.5f, -0.2f,
        0.0f, 0.2f,
        0.5f,  -0.2f,
};

GLuint vertexbuffer;
GLuint programID;

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

GLuint LoadShaders()
{
    GLuint programObject;

    GLbyte fShaderStr[] = "precision mediump float;"
            "uniform vec4 u_Color;"
            "void main(){"
            "    gl_FragColor = u_Color;"
            "}";

    GLbyte vShaderStr[] = "attribute vec4 a_Position;"
            "void main(){"
            "    gl_Position = a_Position;"
            " }";

    GLuint vertexShader;
    GLuint fragmentShader;

    GLint linked;

    vertexShader = LoadShader(GL_VERTEX_SHADER, vShaderStr);
    fragmentShader = LoadShader(GL_FRAGMENT_SHADER, fShaderStr);

    programObject = glCreateProgram();

    glAttachShader(programObject,vertexShader);
    glAttachShader(programObject,fragmentShader);

    glBindAttribLocation(programObject, 0 ,"vPosition");
    glLinkProgram(programObject);
    return programObject;
}

void onSurfaceCreated()
{
    glClearColor(0,0,0,0);
    programID = LoadShaders();
    glUseProgram(programID);
    int uColorLocation = glGetUniformLocation(programID,"u_Color");
    glUniform4f(uColorLocation,0.0f,0.0f,1.0f,1.0f);
    int aPositionLocation = glGetAttribLocation(programID, "a_Position");
    glVertexAttribPointer(aPositionLocation,2,GL_FLOAT,GL_FALSE,0,G_vertex_buffer_data);//возможны проблеммы с G_vertex_buffer_data
    glEnableVertexAttribArray(aPositionLocation);
}
void onSurfaceChanged(int width, int height)
{
    glViewport(0,0,width,height);
}
void onDrawFrame()
{
    glClear(GL_COLOR_BUFFER_BIT);
    glDrawArrays(GL_TRIANGLES, 0, 3);
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mSurfaceCreated(JNIEnv *env, jclass type)
{
    onSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mSurfaceChanged(JNIEnv *env, jclass type,
                                                                         jint width, jint height)
{
    onSurfaceChanged(width,height);
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mDrawframe(JNIEnv *env, jclass type)
{
    onDrawFrame();
}