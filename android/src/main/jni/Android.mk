LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := lame_native
LOCAL_SRC_FILES := lame_native.c

include $(BUILD_SHARED_LIBRARY)