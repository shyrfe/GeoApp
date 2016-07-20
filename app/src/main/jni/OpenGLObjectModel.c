#include <jni.h>
#include "OpenGLObjectModel.h"

JNIEXPORT jstring JNICALL
Java_com_example_vshurygin_geoapp_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

    //glClearColor(1.0f,0.0f,0.0f,0.0f);
    return (*env)->NewStringUTF(env, "HelloJNI");
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1surface_1created(JNIEnv *env, jobject instance) {

    //glClearColor(1.0f,0.0f,0.0f,0.0f);
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1surface_1changed(JNIEnv *env, jobject instance, jint width, jint height) {

    // TODO

}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_on_1draw_1frame(JNIEnv *env, jobject instance) {

    //glClear(GL_COLOR_BUFFER_BIT);

}