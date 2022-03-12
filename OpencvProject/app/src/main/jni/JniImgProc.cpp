#include <jni.h>
#include <string.h>
#include <stdlib.h>

extern "C"
jstring Java_opencvproject_com_ImgProcessing_jniTellMeWhy(JNIEnv *env, jobject obj, jstring str) {
    const char *question = env->GetStringUTFChars(str, JNI_FALSE);
    char *answer = "fuck,no why!!!";
    char *data = (char *) malloc(strlen(question) + strlen(answer) + 1);
    strcpy(data, question);
    strcat(data, "JNI说:");
    strcat(data, answer);
    return env->NewStringUTF(data);
}