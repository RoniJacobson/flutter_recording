package com.jacobson.flutter_recording

public class MP3Lame (bitRate: Int, inSampleRate: Int, sampleRate: Int, lameQuality: Int){
    init {
        System.loadLibrary("lame_native")
        initialize(bitRate, inSampleRate, sampleRate, lameQuality)
    }


    private external fun initialize(bitRate: Int, inSampleRate: Int, sampleRate: Int, lameQuality: Int)
    external fun encodeBuffer(pcmBuffer: ShortArray, amount: Int, mp3buffer: ByteArray): Int
    external fun flush(mp3buffer: ByteArray): Int
    external fun close()
}