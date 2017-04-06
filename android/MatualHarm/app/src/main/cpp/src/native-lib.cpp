#include <jni.h>
#include <string>
#include "../head/harm.h"

inline void *getPtr(JNIEnv *env, jobject instance){
    jfieldID  fid = env->GetFieldID(env->GetObjectClass(instance),"id","long");
    return (void*) env->GetLongField(instance,fid);
}

JNIEXPORT jlong JNICALL
Java_com_newind_core_NObject_createCache(JNIEnv *env, jclass type) {

    // TODO

}

JNIEXPORT void JNICALL
Java_com_newind_core_NObject_destroyCache(JNIEnv *env, jclass type, jlong id) {

    // TODO

}

JNIEXPORT jint JNICALL

Java_com_newind_core_NObject_fetchAll(JNIEnv *env, jclass type_, jlong cacheID, jint type, jlongArray idList_) {
    jlong *idList = env->GetLongArrayElements(idList_, NULL);
    jsize idListSize = env->GetArrayLength(idList_);

    env->ReleaseLongArrayElements(idList_, idList, 0);
}
