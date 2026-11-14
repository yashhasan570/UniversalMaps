#include "com/universalmaps/app/sdk/core/jni_helper.hpp"
#include "com/universalmaps/app/sdk/platform/GuiThread.hpp"

extern "C"
{
// static void nativeProcessTask(long taskPointer);
JNIEXPORT void Java_app_universalmaps_sdk_util_concurrency_UiThread_nativeProcessTask(JNIEnv * env, jclass clazz,
                                                                                    jlong taskPointer)
{
  android::GuiThread::ProcessTask(taskPointer);
}
}
