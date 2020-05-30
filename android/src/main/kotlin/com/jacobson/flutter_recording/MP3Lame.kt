package com.jacobson.flutter_recording

public class MP3Lame (bitRate: Int, sampleRate: Int, lameQuality: Int){
    init {
        System.loadLibrary("lame_native")
        initialize(bitRate, sampleRate, lameQuality)
    }


    private external fun initialize(bitRate: Int, sampleRate: Int, lameQuality: Int)
}