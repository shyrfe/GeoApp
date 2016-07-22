#include "OpenGLObjectModel.h"


GLuint  LoadShader(const char* shaderSrc, GLenum type)
{
    GLuint shader;
    GLint compiled;

    shader = glCreateShader(type);//создаём объект шейдера

    if (shader == 0)
    {
        return 0;
    }

    glShaderSource(shader,1,&shaderSrc,NULL);// выгружаем в шейдер shaderSrc
    glCompileShader(shader);//компилим это барахло
    glGetShaderiv(shader, GL_COMPILE_STATUS,&compiled);//проверяем compile status
    if(!compiled)
    {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);

        if(infoLen > 1 )
        {
            char* infoLog = malloc(sizeof(char) * infoLen);
            glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
            //esLogMessage("Error compiling shader: \n%s\n", infoLog);
            free(infoLog);
        }
        glDeleteShader(shader);
        return 0;
    }
    return shader;

}


void onSurfaceCreated()
{
    glClearColor(1.0f,1.0f,1.0f,0.5f);
    //glClearColor(0f, 0f, 0f, 0f);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);

    GLbyte vShaderStr[] =
            "attribute vec4 vPosition; \n"
            "void main()               \n"
            "{                         \n"
            "  gl_Position = vPosition;\n"
            "}                         \n";

    GLbyte fShaderStr[] =
            "precision mediump float;                   \n"
            "void main()                                \n"
            "{                                          \n"
            "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); \n"
            "}                                          \n";

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



}

void onSurfaceChanged(int width, int height)
{

}

void onDrawFrame()
{
    //glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glClearColor(0.0f,0.0f,0.0f,0.0f);
    glClear(GL_COLOR_BUFFER_BIT);

    glUseProgram(programObject);
    glVertexAttribPointer(0,3,GL_FLOAT, GL_FALSE, 0, mTriangleVertices);
    glEnableVertexAttribArray(0);
    glDrawArrays(GL_TRIANGLES,0,3);

}


JNIEXPORT jstring JNICALL
Java_com_example_vshurygin_geoapp_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance)
{
    //glClearColor(1.0f,0.0f,0.0f,0.0f);
    return (*env)->NewStringUTF(env, "HelloJNI");
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1surface_1created(JNIEnv *env, jobject instance)
{
    onSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1surface_1changed(JNIEnv *env, jobject instance, jint width, jint height)
{
    onSurfaceChanged(width,height);
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1draw_1frame(JNIEnv *env, jobject instance)
{
    onDrawFrame();
}

