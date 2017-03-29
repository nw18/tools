#include <jni.h>
#include <string>
#include "../head/harm.h"
extern "C"
JNIEXPORT jstring JNICALL
Java_com_newind_mutualharm_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
