
//#include <GLES2/gl2.h>
#include "OpenGLObjectModel.h"


void onSurfaceCreated()
{
    glClearColor(1.0f,1.0f,1.0f,0.5f);
    //glClearColor(0f, 0f, 0f, 0f);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
}

void onSurfaceChanged(int width, int height)
{

}

void onDrawFrame()
{
    //glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glClearColor(0.0f,0.0f,0.0f,0.0f);
    glClear(GL_COLOR_BUFFER_BIT);
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

