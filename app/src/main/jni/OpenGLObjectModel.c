#include "OpenGLObjectModel.h"
void clearPositionBuffer()
{
    //float* local_buff = &cubePositions;
    //cubePositions = NULL;

    //free(local_buff);
    //free(cubePositions);
}
void clearVertexBuffer()
{
    global_vertex_buffer_size = 0;
    //float* local_buff = &G_vertex_buffer_data;
    //G_vertex_buffer_data = NULL;
    //free(G_vertex_buffer_data);
    //bindData();
    //free(local_buff);
}
void prepareData(){

    int i = 0;
    int j = 0;

    if (global_vertex_buffer_size != 0)
    {
        int vertexNumber = global_vertex_buffer_size / LOCAL_CUBE_VERTICES_BUFFER_SIZE;
        int vertex_i = 0;
        while (vertex_i < vertexNumber) {
            while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
            {
                if (j == 3) { j = 0; }
                G_vertex_buffer_data[(vertex_i * LOCAL_CUBE_VERTICES_BUFFER_SIZE) + i] =
                        localCubeVertices[i] + cubePositions[(vertex_i * 3) + j];
                i++;
                j++;
            }
            i = 0;
            j = 0;
            vertex_i++;
        }
    }
}

void addObjectVertexData(float _x , float _y, float _z)
{
    int i = 0;
    int j = 0;
    //ALOG("GLOBAL do %i",global_vertex_buffer_size);
    global_vertex_buffer_size += LOCAL_CUBE_VERTICES_BUFFER_SIZE;
    //ALOG("GLOBAL posle %i",global_vertex_buffer_size);
    G_vertex_buffer_data = (float*)realloc(G_vertex_buffer_data,(sizeof(float)*global_vertex_buffer_size));

    int vertexNumber = ((global_vertex_buffer_size/LOCAL_CUBE_VERTICES_BUFFER_SIZE)-1);

    cubePositions = (float*)realloc(cubePositions,sizeof(float)*((vertexNumber+1)*3));

    cubePositions[vertexNumber*3] = _x;
    cubePositions[(vertexNumber*3)+1] = _y;
    cubePositions[(vertexNumber*3)+2] = _z;
    //ALOG("x,y,z: %f , %f , %f",cubePositions[vertexNumber*3],cubePositions[(vertexNumber*3)+1],cubePositions[(vertexNumber*3)+2]);

    while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
    {
        if (j == 3)
        {j = 0;}
        G_vertex_buffer_data[(LOCAL_CUBE_VERTICES_BUFFER_SIZE*vertexNumber)+i] = localCubeVertices[i] + cubePositions[(3*vertexNumber)+j];
        //ALOG("G_Vertex: %f",G_vertex_buffer_data[(LOCAL_CUBE_VERTICES_BUFFER_SIZE*vertexNumber)+i]);
        i++;
        j++;
    }

}

void rotateVertexBufferCubeX(float A)
{
    int i = 0;
    A = A * M_PI/180;

    float m[LOCAL_CUBE_VERTICES_BUFFER_SIZE];

    while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
    {
        m[i] = localCubeVertices[i] * 1 + localCubeVertices[i+1] * 0 + localCubeVertices[i+2] * 0;
        m[i+1] = localCubeVertices[i] * 0 + localCubeVertices[i+1] * cosf(A) + localCubeVertices[i+2] * sinf(A);
        m[i+2] = localCubeVertices[i] * 0 + localCubeVertices[i+1] * (-1*sinf(A)) + localCubeVertices[i+2] * cosf(A);
        i += 3;
    }


    i=0;
    while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
    {
        localCubeVertices[i] = m[i];
        i++;
    }
    i=0;
    prepareData();
}
void rotateVertexBufferObjectY(float A)
{
    int i = 0;
    A = A * M_PI/180;

    float m[LOCAL_CUBE_VERTICES_BUFFER_SIZE];

    while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
    {
        m[i] = localCubeVertices[i] * cosf(A) + localCubeVertices[i+1] * 0 + localCubeVertices[i+2] * sinf(A);
        m[i+1] = localCubeVertices[i] * 0 + localCubeVertices[i+1] * 1 + localCubeVertices[i+2] * 0;
        m[i+2] = localCubeVertices[i] * (-1*sinf(A)) + localCubeVertices[i+1] * 0 + localCubeVertices[i+2] * cosf(A);
        i += 3;
    }


    i=0;
    while (i < LOCAL_CUBE_VERTICES_BUFFER_SIZE)
    {
        localCubeVertices[i] = m[i];
        i++;
    }
    i=0;
    prepareData();
}

void createViewMatrix()
{
    jmethodID method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"createViewMatrix","()[F");

    jfloatArray ViewMatrix = (jfloatArray)(*pJNIenv)->CallObjectMethod(pJNIenv,pSurfaceRendererWrapper,method);
    mViewMatrix = (*pJNIenv)->GetFloatArrayElements(pJNIenv,ViewMatrix,NULL);
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

void createProjectionMatrix(int width, int height)
{
    float left = -1.0f;
    float right = 1.0f;

    float bottom = -1.0f;
    float top = 1.0f;

    float near = 1.0f;
    float far = 5.0f;

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

    float values[] = {left,right,bottom,top,near,far};

    /*frustum(mProjectionMatrix,left,right,bottom,top,near,far);*/
    jmethodID method = (*pJNIenv)->GetMethodID(pJNIenv,pSurfaceRendererWrapperClass,"createProjectionMatrix","([F)[F");
    jfloatArray projectionValues;
    jfloatArray projectionMatrix;

    projectionValues = (*pJNIenv)->NewFloatArray(pJNIenv,6);
    (*pJNIenv)->SetFloatArrayRegion(pJNIenv,projectionValues,0,6,values);

    projectionMatrix = (jfloatArray)(*pJNIenv)->CallObjectMethod(pJNIenv,pSurfaceRendererWrapper,method,projectionValues);
    mProjectionMatrix =(*pJNIenv)->GetFloatArrayElements(pJNIenv,projectionMatrix,0);

    glUniformMatrix4fv(uMatrixLocation, 1, 0, mProjectionMatrix);
}

void onSurfaceCreated()
{
    glClearColor(0,0,0,0);
    glEnable(GL_DEPTH_TEST);
    programID = LoadProgramFromShaders();
    glUseProgram(programID);

    createViewMatrix();



    //prepareData();
    bindData();
}

void onSurfaceChanged(int width, int height)
{
    glViewport(0,0,width,height);
    createProjectionMatrix(width,height);
    bindMatrix();
    /*addObjectVertexData(-0.5f,0.5f,-1.0f);
    addObjectVertexData(0.5f,-0.5f,-1.0f);
    bindData();*/
}

void onDrawFrame()
{
    //createViewMatrix();
    bindMatrix();
    if(global_vertex_buffer_size != 0)
    {
        rotateVertexBufferObjectY(2);
    }


    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    int i =0;
    while(i < (global_vertex_buffer_size/LOCAL_CUBE_VERTICES_BUFFER_SIZE))
    {
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, (i*9), 3);
        glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, (i*9)+3, 3);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, (i*9)+6, 3);
        i++;
    }

    //free(G_vertex_buffer_data);
    //G_vertex_buffer_data = NULL;
    global_vertex_buffer_size = 0;
    //free(cubePositions);
    //cubePositions = NULL;
    //clearPositionBuffer();
    //clearVertexBuffer();
   /* glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 0, 3);
    glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 3, 3);
    glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 6, 3);
    glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 9, 3);
    glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 12, 3);
    glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
    glDrawArrays(GL_TRIANGLES, 15, 3);*/
}

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

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_addObject(JNIEnv *env, jclass type,
                                                                   jfloat _x, jfloat _y) {

    /*cubePositon[0] = _x;
    cubePositon[1] = _y;*/
    //ALOG("Object Added");
    addObjectVertexData(_x,_y,-1.0f);
    bindData();

}

JNIEXPORT void JNICALL
Java_com_example_vshurygin_geoapp_SurfaceRendererWrapper_clearAllData(JNIEnv *env, jclass type) {

    //clearPositionBuffer();
    //clearVertexBuffer();

}