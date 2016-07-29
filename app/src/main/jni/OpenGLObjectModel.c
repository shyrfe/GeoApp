#include "OpenGLObjectModel.h"

void createViewMatrix()
{

    jmethodID method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"createViewMatrix","()[F");

    jfloatArray ViewMatrix = (jfloatArray)(*pJNIenv)->CallObjectMethod(pJNIenv,pSurfaceRendererWrapper,method);
    mViewMatrix = (*pJNIenv)->GetFloatArrayElements(pJNIenv,ViewMatrix,NULL);
    //ALOG("%f",mViewMatrix[0]);
}
void prepareData(){
    float s = 0.4f;
    float d = 0.9f;
    float l = 3;

    float vertices[] = {

            // первый треугольник
            -2*s, -s, d,
            2*s, -s, d,
            0, s, d,

            // второй треугольник
            -2*s, -s, -d,
            2*s, -s, -d,
            0, s, -d,

            // третий треугольник
            d, -s, -2*s,
            d, -s, 2*s,
            d, s, 0,

            // четвертый треугольник
            -d, -s, -2*s,
            -d, -s, 2*s,
            -d, s, 0,

            // ось X
            -l, 0,0,
            l,0,0,

            // ось Y
            0,-l,0,
            0,l,0,

            // ось Z
            0,0,-l,
            0,0,l,
    };
}
void bindData()
{
    aPositionLocation = glGetAttribLocation(programID, "a_Position");
    glVertexAttribPointer(aPositionLocation,POSITION_COUNT,GL_FLOAT,GL_FALSE,0,G_vertex_buffer_data);
    glEnableVertexAttribArray(aPositionLocation);

    uColorLocation = glGetUniformLocation(programID,"u_Color");
    glUniform4f(uColorLocation,0.0f,0.0f,1.0f,1.0f);

    uMatrixLocation = glGetUniformLocation(programID, "u_Matrix");
}
void MultiplyProjectionMatrixAndViewMatrix(float* projectionMatrix,float* ViewMatrix, float* result)
{
    const int STEP = 4;
   /* for(int i = 0; i < STEP; i++)
        for(int j = 0; j < STEP; j++)
        {
            result[i*STEP + j] = 0;
            for(int k = 0; k < STEP; k++)
                result[i*STEP+j] += projectionMatrix[i*STEP+k] * ViewMatrix[k*STEP+j];
        }*/
}
void bindMatrix()
{
    //jclass class = (*pJNIenv)->GetObjectClass(pJNIenv,pSurfaceRendererWrapper);//(*pJNIenv)->FindClass(pJNIenv,"SurfaceRendererWrapper");
    jmethodID method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"setProjectionMatrix","([F)V");
    jfloatArray projectionResult;
    projectionResult = (*pJNIenv)->NewFloatArray(pJNIenv,POJECTION_AND_VIEW_MATRIX_SIZE);
    (*pJNIenv)->SetFloatArrayRegion(pJNIenv,projectionResult,0,POJECTION_AND_VIEW_MATRIX_SIZE,mProjectionMatrix);

    (*pJNIenv)->CallVoidMethod(pJNIenv,pSurfaceRendererWrapper,method,projectionResult);

    method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"setViewMatrix","([F)V");
    jfloatArray viewResult;
    viewResult = (*pJNIenv)->NewFloatArray(pJNIenv,POJECTION_AND_VIEW_MATRIX_SIZE);
    (*pJNIenv)->SetFloatArrayRegion(pJNIenv,viewResult,0,POJECTION_AND_VIEW_MATRIX_SIZE,mViewMatrix);
    (*pJNIenv)->CallVoidMethod(pJNIenv,pSurfaceRendererWrapper,method,viewResult);

    method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"MultiplyProjectionMatrixAndViewMatrix","()[F");
    jfloatArray MatrixArrayResult = (jfloatArray)(*pJNIenv)->CallObjectMethod(pJNIenv,pSurfaceRendererWrapper,method);
    mMatrix = (*pJNIenv)->GetFloatArrayElements(pJNIenv,MatrixArrayResult,NULL);

    //mViewMatrix = (*pJNIenv)->GetFloatArrayElements(pJNIenv,ViewMatrix,NULL);

    //MultiplyProjectionMatrixAndViewMatrix(mProjectionMatrix,mViewMatrix,mMatrix);

    glUniformMatrix4fv(uMatrixLocation,1,GL_FALSE,mMatrix);
}

void frustum(float ProjectionMatrix[],float left, float right, float bottom, float top, float zNear, float zFar)
{
    float zDelta = (zFar-zNear);
    float dir = (right-left);
    float height = (top-bottom);
    float zNear2 = 2*zNear;
// i*4+j
    ProjectionMatrix[0*4+0]=2.0f*zNear/dir;
    ProjectionMatrix[0*4+1]=0.0f;
    ProjectionMatrix[0*4+2]=(right+left)/dir;
    ProjectionMatrix[0*4+3]=0.0f;
    ProjectionMatrix[1*4+0]=0.0f;
    ProjectionMatrix[1*4+1]=zNear2/height;
    ProjectionMatrix[1*4+2]=(top+bottom)/height;
    ProjectionMatrix[1*4+3]=0.0f;
    ProjectionMatrix[2*4+0]=0.0f;
    ProjectionMatrix[2*4+1]=0.0f;
    ProjectionMatrix[2*4+2]=-(zFar+zNear)/zDelta;
    ProjectionMatrix[2*4+3]=-zNear2*zFar/zDelta;
    ProjectionMatrix[3*4+0]=0.0f;
    ProjectionMatrix[3*4+1]=0.0f;
    ProjectionMatrix[3*4+2]=-1.0f;
    ProjectionMatrix[3*4+3]=0.0f;
}

/*void frustum(float mProjectionMatrix[],float left, float right, float bottom, float top, float near, float far)
{
    float r_width  = 1.0f / (right - left);
    float r_height = 1.0f / (top - bottom);
    float r_depth  = 1.0f / (near - far);
    float x = 2.0f * (near * r_width);
    float y = 2.0f * (near * r_height);
    float A = 2.0f * ((right+left) * r_width);
    float B = (top + bottom) * r_height;
    float C = (far + near) * r_depth;
    float D = 2.0f * (far * near * r_depth);

    //memset((void*)m, 0, 16*sizeof(float));
    mProjectionMatrix[ 0] = x;
    mProjectionMatrix[ 5] = y;
    mProjectionMatrix[ 8] = A;
    mProjectionMatrix[ 9] = B;
    mProjectionMatrix[10] = C;
    mProjectionMatrix[14] = D;
    mProjectionMatrix[11] = -1.0f;
}*/

void createProjectionMatrix(int width, int height)
{
    float left = -1.0f;
    float right = 1.0f;
    float bottom = -1.0f;
    float top = 1.0f;
    float near = 2.0f;
    float far = 8.0f;
    float ratio = 1;

    if (width > height)
    {
        ratio = (float) width / height;
        left *= ratio;
        right *= ratio;
    }
    else
    {
        ratio = (float) height / width;
        bottom *= ratio;
        top *= ratio;
    }

    frustum(mProjectionMatrix,left,right,bottom,top,near,far);
    glUniformMatrix4fv(uMatrixLocation, 1, 0, mProjectionMatrix);
}

void onSurfaceCreated()
{
    glClearColor(0,0,0,0);
    glEnable(GL_DEPTH_TEST);
    programID = LoadProgramFromShaders();
    glUseProgram(programID);

    createViewMatrix();
    prepareData();
    bindData();

}

void onSurfaceChanged(int width, int height)
{
    glViewport(0,0,width,height);
    createProjectionMatrix(width,height);
    bindMatrix();
}

void onDrawFrame()
{
    /*glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    glUniform4f(uColorLocation,1.0f,0.0f,0.0f,1.0f);
    glDrawArrays(GL_TRIANGLES,3,6);*/
    createViewMatrix();
    bindMatrix();

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    // треугольники
    glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 0, 3);

    glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 3, 3);
    //glDrawArrays(GL_TRIANGLES,3,6);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 6, 3);
    //glDrawArrays(GL_TRIANGLES,6,9);

    glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 9, 3);
    //glDrawArrays(GL_TRIANGLES,9,12);

    // оси
    glLineWidth(1);

    glUniform4f(uColorLocation, 0.0f, 1.0f, 1.0f, 1.0f);
    glDrawArrays(GL_LINES, 12, 2);

    glUniform4f(uColorLocation, 1.0f, 0.0f, 1.0f, 1.0f);
    glDrawArrays(GL_LINES, 14, 2);

    glUniform4f(uColorLocation, 1.0f, 0.5f, 0.0f, 1.0f);
    glDrawArrays(GL_LINES, 16, 2);
}

/*JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mSurfaceCreated(JNIEnv *env, jclass type)
{

    onSurfaceCreated();
}*/

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mSurfaceChanged(JNIEnv *env, jclass type,jobject surfaceRendererWrapper,
                                                                         jint width, jint height)
{
    pJNIenv = env;
    pSurfaceRendererWrapper = surfaceRendererWrapper;
    pSurfaceRendererWrapperClass = (*pJNIenv)->GetObjectClass(pJNIenv,pSurfaceRendererWrapper);//(*pJNIenv)->FindClass(pJNIenv,"SurfaceRendererWrapper");
    onSurfaceChanged(width,height);
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mDrawframe(JNIEnv *env, jclass type,jobject surfaceRendererWrapper)
{
    pJNIenv = env;
    pSurfaceRendererWrapper = surfaceRendererWrapper;
    pSurfaceRendererWrapperClass = (*pJNIenv)->GetObjectClass(pJNIenv,pSurfaceRendererWrapper);

    onDrawFrame();
}

JNIEXPORT jstring JNICALL
Java_com_example_vshurygin_geoapp_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance)
{
    //glClearColor(1.0f,0.0f,0.0f,0.0f);
    //return (*env)->NewStringUTF(env, "HelloJNI");
}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_mSurfaceCreated(JNIEnv *env, jclass type,
                                                                         jobject surfaceRendererWrapper)
{

    pJNIenv = env;
    pSurfaceRendererWrapper = surfaceRendererWrapper;
    pSurfaceRendererWrapperClass = (*pJNIenv)->GetObjectClass(pJNIenv,pSurfaceRendererWrapper);//(*pJNIenv)->FindClass(pJNIenv,"SurfaceRendererWrapper");
    /*jclass class = (*pJNIenv)->GetObjectClass(pJNIenv,surfaceRendererWrapper);//(*pJNIenv)->FindClass(pJNIenv,"SurfaceRendererWrapper");
    jmethodID method = (*pJNIenv)->GetMethodID(pJNIenv,class,"mFrustum","()[F");
    jfloatArray fa[4] = {0,0,0,0};

    jfloatArray result = (jfloatArray)(*pJNIenv)->CallObjectMethod(pJNIenv,surfaceRendererWrapper,method);*/
    //jmethodID mmm = (*pJNIenv)->GetMethodID(pJNIenv,class,"mFrustum","()V");
    //(*pJNIenv)->CallVoidMethod(pJNIenv,surfaceRendererWrapper,mmm);

    onSurfaceCreated();

}