#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_vshurygin_geoapp_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "HelloJNI");
}