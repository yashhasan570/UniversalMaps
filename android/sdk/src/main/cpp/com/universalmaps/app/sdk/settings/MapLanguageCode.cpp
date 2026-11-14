#include "com/universalmaps/app/sdk/Framework.hpp"
#include "com/universalmaps/app/sdk/core/jni_helper.hpp"

#include "platform/settings.hpp"

extern "C"
{
JNIEXPORT void Java_app_universalmaps_sdk_settings_MapLanguageCode_setMapLanguageCode(JNIEnv * env, jobject,
                                                                                    jstring languageCode)
{
  g_framework->SetMapLanguageCode(jni::ToNativeString(env, languageCode));
}

JNIEXPORT jstring Java_app_universalmaps_sdk_settings_MapLanguageCode_getMapLanguageCode(JNIEnv * env, jobject)
{
  return jni::ToJavaString(env, g_framework->GetMapLanguageCode());
}
}
