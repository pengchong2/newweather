LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_CERTIFICATE := platform



LOCAL_PACKAGE_NAME := WeatherWidget_Summary
LOCAL_STATIC_JAVA_LIBRARIES := libsuport libcommons

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES :=libsuport:libs/android-support-v4.jar \
					libcommons:libs/commons-codec-1.8.jar \
								
 
LOCAL_MODULE_TAGS := optional
include $(BUILD_MULTI_PREBUILT)

########################
include $(call all-makefiles-under,$(LOCAL_PATH))
