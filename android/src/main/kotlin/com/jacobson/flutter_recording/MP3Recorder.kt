package com.jacobson.flutter_recording

import android.annotation.TargetApi
import android.media.*
import android.os.Build
import android.os.Environment
import android.os.Process
import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.abs
import kotlin.math.ceil


@TargetApi(Build.VERSION_CODES.M)
class MP3Recorder(fileName: String, bitRate: Int, sampleRate: Int, notificationCallback: (name: String) -> Unit, callbackRate: Long, timestampBufferLength: Int) : RecordingSuper(fileName, bitRate, sampleRate, notificationCallback, callbackRate, timestampBufferLength) {
    private val timestampsList: MutableList<List<Number>> = mutableListOf()
    private val mp3Lame: MP3Lame
    private var recorder: AudioRecord
    private val lameQuality = 5
    private val channels = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private var recordingThread: Job? = null
    private var file: File? = null
    private var outputStream: FileOutputStream? = null
    private val bufferSize: Int
    private var maxAmplitude = 0.0
    private var running = false

    init {
        val rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
        bufferSize = AudioRecord.getMinBufferSize(rate, channels, encoding)
        mp3Lame = MP3Lame(bitRate, rate, sampleRate, lameQuality)
        val audioFormat = AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(rate)
                .setChannelMask(channels)
                .build()
        recorder = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize * 2)
                .build()
    }

    override fun startRecording() {
        writeAudioDataToFile()
        recordingThread?.start()
    }

    private fun writeAudioDataToFile() {
        recordingThread = GlobalScope.launch {
            running = true
            val buffer = ShortArray(bufferSize)
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            println("AudioRecord recording...")
            val state = Environment.getExternalStorageState()
            if (state != Environment.MEDIA_MOUNTED) {
                Log.d("Media Unmounted", "Need to add a thing to fix this")
            }
            print(Environment.getExternalStorageDirectory().absolutePath)
            file = File(fileName)
            file?.parentFile?.mkdirs()
            file?.createNewFile()
            outputStream = FileOutputStream(file, true)
            recorder.startRecording()
            val empty = ShortArray(500)
            infoTimer.scheduleAtFixedRate(timerTask {
                timerFunction()
            }, 0L, callbackRate)
            while (isActive) {
                // read the data into the buffer
                val readSize = recorder.read(buffer, 0, buffer.size)
                // on startup there will be zero bytes, strip them out
                if (running) {
                    if (buffer.sliceArray(1..500).contentEquals(empty)) {
                        val buffer2 = buffer.dropWhile { it.toInt() == 0 }.toShortArray()
                        val readSize2 = buffer2.size
                        writeShorts(readSize2, buffer2, outputStream!!)
                    } else {
                        writeShorts(readSize, buffer, outputStream!!)
                    }
                }
            }
            println("file closed")
        }
    }

    private fun writeShorts(readSize: Int, buffer: ShortArray, outputStream: FileOutputStream) {
        maxAmplitude = 0.0
        for (i in 0 until readSize) {
            val current = abs(buffer[i].toDouble())
            if (current > maxAmplitude) {
                maxAmplitude = current
            }
        }
        var db = 0.0
        if (maxAmplitude != 0.0) {
            db = amplitudeToDecibels(maxAmplitude)
        }
        writePCMToMP3(buffer, readSize)
        println("Max amplitude: $maxAmplitude ; DB: $db")
        notificationCallback("$db")
    }

    private fun writePCMToMP3(buffer: ShortArray, readSize: Int) {
        val mp3Buffer = ByteArray(ceil(1.25 * buffer.size + 7200).toInt())
        val bytesEncoded = mp3Lame.encodeBuffer(buffer, readSize, mp3Buffer)
        outputStream?.write(mp3Buffer, 0, bytesEncoded)
    }

    private fun close() {
        recorder.stop()
        recorder.release()
        val mp3Buffer = ByteArray(7200 * 2)
        mp3Lame.flush(mp3Buffer)
        outputStream?.write(mp3Buffer)
        outputStream?.flush()
        outputStream?.close()
        infoTimer.cancel()
    }

    override fun stopRecording() {
        runBlocking {
            recordingThread?.cancelAndJoin()
            println("stopped")
            close()
        }
        println("stopped")
    }

    override fun getMaxAmplitude(): Double {
        val temp = maxAmplitude
        maxAmplitude = 0.0
        return temp
    }

    override fun pauseRecording() {
        running = false
        super.pauseRecording()
    }

    override fun resumeRecoding() {
        running = true
        maxAmplitude = 0.0
        super.resumeRecoding()
    }

}