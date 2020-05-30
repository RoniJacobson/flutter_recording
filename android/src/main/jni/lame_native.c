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
