#include <jni.h>
#include <string>

extern "C" JNIEXPORT jboolean JNICALL
Java_com_palash_setu_native_NativeEngine_initVoskASR(JNIEnv* env, jobject, jstring model_path) {
    (void)env;
    (void)model_path;
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_palash_setu_native_NativeEngine_translateNMT(JNIEnv* env, jobject, jstring source_text, jstring target_language) {
    (void)source_text;
    (void)target_language;
    return env->NewStringUTF("Native NMT placeholder");
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_palash_setu_native_NativeEngine_synthesizePiperTTS(JNIEnv* env, jobject, jstring text, jstring voice_path) {
    (void)text;
    (void)voice_path;
    return env->NewByteArray(0);
}
