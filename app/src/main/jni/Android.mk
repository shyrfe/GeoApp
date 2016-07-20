LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := OpenGLObjectModel
LOCAL_CFLAGS    := -Wall -Wextra
LOCAL_SRC_FILES := OpenGLObjectModel.c jni.c
LOCAL_LDLIBS := -lGLESv2

include $(BUILD_SHARED_LIBRARY)