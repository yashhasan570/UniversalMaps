#include <jni.h>
#include "com/universalmaps/app/sdk/core/logging.hpp"

extern "C"
{
JNIEXPORT void Java_app_universalmaps_sdk_util_log_LogsManager_nativeToggleCoreDebugLogs(JNIEnv * /*env*/,
                                                                                       jclass /*clazz*/,
                                                                                       jboolean enabled)
{
  jni::ToggleDebugLogs(enabled);
}
}  // extern "C"
