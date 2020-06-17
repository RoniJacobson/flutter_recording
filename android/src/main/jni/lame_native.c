//
// Created by Owner on 5/22/2020.
//

#include <string.h>
#include <jni.h>
#include "libmp3lame/lame.h"

lame_global_flags *lame_flags;
JNIEXPORT void JNICALL
Java_com_jacobson_flutter_1recording_MP3Lame_initialize(JNIEnv *env, jobject thiz, jint bit_rate,
                                                        jint sample_rate, jint lame_quality) {
    lame_flags = lame_init();
    lame_set_in_samplerate(lame_flags, sample_rate);
    lame_set_out_samplerate(lame_flags, sample_rate);
    lame_set_brate(lame_flags, bit_rate);
    lame_set_quality(lame_flags, lame_quality);
    lame_set_num_channels(lame_flags, 1);
    lame_set_mode(lame_flags, MONO);
    lame_init_params(lame_flags);
}

JNIEXPORT int JNICALL
Java_com_jacobson_flutter_1recording_MP3Lame_encodeBuffer(JNIEnv *env, jobject thiz,
                                                          jshortArray pcm_buffer, jint amount,
                                                          jbyteArray java_mp3buffer) {
    const jsize mp3buffer_size = (*env)->GetArrayLength(env, java_mp3buffer);
    jbyte *mp3buffer = (*env)->GetByteArrayElements(env, java_mp3buffer, NULL);

    jshort *left_buffer = (*env)->GetShortArrayElements(env, pcm_buffer, NULL);
    jshort *right_buffer = (*env)->GetShortArrayElements(env, pcm_buffer, NULL);

    int result = lame_encode_buffer(lame_flags, left_buffer, right_buffer,
                                    amount, mp3buffer, mp3buffer_size);

    (*env)->ReleaseShortArrayElements(env, pcm_buffer, left_buffer, 0);
    (*env)->ReleaseShortArrayElements(env, pcm_buffer, right_buffer, 0);
    (*env)->ReleaseByteArrayElements(env, java_mp3buffer, mp3buffer, 0);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_jacobson_flutter_1recording_MP3Lame_flush(JNIEnv *env, jobject thiz,
                                                   jbyteArray java_mp3buffer) {
    const jsize mp3buffer_size = (*env)->GetArrayLength(env, java_mp3buffer);
    jbyte *mp3buffer = (*env)->GetByteArrayElements(env, java_mp3buffer, NULL);

    int result = lame_encode_flush(lame_flags, mp3buffer, mp3buffer_size);

    (*env)->ReleaseByteArrayElements(env, java_mp3buffer, mp3buffer, 0);

    return result;
}

JNIEXPORT void JNICALL
Java_com_jacobson_flutter_1recording_MP3Lame_close(JNIEnv *env, jobject thiz) {
    lame_close(lame_flags);
    lame_flags = NULL;
}