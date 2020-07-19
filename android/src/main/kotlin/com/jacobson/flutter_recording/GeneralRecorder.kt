package com.jacobson.flutter_recording

import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import kotlin.concurrent.timerTask

class GeneralRecorder(fileName: String, encoder: Int, outputFormat: Int, bitRate: Int, sampleRate: Int, notificationCallback: (name: String) -> Unit, callbackRate: Long, timestampBufferLength: Int) : RecordingSuper(fileName, bitRate, sampleRate, notificationCallback, callbackRate, timestampBufferLength) {

    val recorder: MediaRecorder = MediaRecorder()
    init {
        recorder.setAudioChannels(1)
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(outputFormat)
        recorder.setAudioEncoder(encoder)
        recorder.setAudioEncodingBitRate(bitRate)
        recorder.setAudioSamplingRate(sampleRate)
        val file = File(fileName)
        file.parentFile.mkdirs()
        recorder.setOutputFile(fileName)
        recorder.prepare()
    }
    override fun startRecording() {
        recorder.start()
        infoTimer.scheduleAtFixedRate(timerTask { timerFunction() }, callbackRate - (pauseTime - lastTimerTick), callbackRate)
    }

    override fun stopRecording() {
        infoTimer.cancel()
        recorder.stop()
    }

    override fun getMaxAmplitude(): Double {
        return recorder.maxAmplitude.toDouble()
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun pauseRecording() {
        recorder.pause()
        super.pauseRecording()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun resumeRecoding() {
        recorder.resume()
        super.resumeRecoding()
    }
}