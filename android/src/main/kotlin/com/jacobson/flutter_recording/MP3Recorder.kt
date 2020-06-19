package com.jacobson.flutter_recording

import android.annotation.TargetApi
import android.content.ContentValues.TAG
import android.media.*
import android.os.Build
import android.os.Environment
import android.os.Process
import android.system.Os.close
import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log10


@TargetApi(Build.VERSION_CODES.M)
class MP3Recorder(val fileName: String?, bitRate: Int, sampleRate: Int, lameQuality: Int, val notificationCallback: (name: String) -> Unit) : RecordingInterface {
//    lame_set_in_samplerate(glf, inSamplerate);
//    lame_set_num_channels(glf, outChannel);
//    lame_set_out_samplerate(glf, outSamplerate);
//    lame_set_brate(glf, outBitrate);
//    lame_set_quality(glf, quality);
    private val mp3Lame: MP3Lame
    private var recorder: AudioRecord
    private val sampleRate = 44100
    private val channels = AudioFormat.CHANNEL_IN_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private var recordingThread: Job? = null
    private var file: File? = null
    private var outputStream: FileOutputStream? = null
    private val bufferSize: Int
    init {
        val rate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_SYSTEM)
        bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        mp3Lame = MP3Lame(bitRate, rate, lameQuality)
        val audioFormat = AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(rate)
                .setChannelMask(channels)
                .build()
//        val bufferSize = AudioRecord.getMinBufferSize(this.sampleRate, channels, encoding)
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
        recordingThread = GlobalScope.launch{
            val buffer = ShortArray(bufferSize)
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

            println("AudioRecord recording...")
            val state = Environment.getExternalStorageState()
            if (state != Environment.MEDIA_MOUNTED) {
                Log.d("Media Unmounted", "Need to add a thing to fix this")
            }
            file = File(Environment.getExternalStorageDirectory(), fileName)
            file?.createNewFile()
            outputStream = FileOutputStream(file, true)
            recorder.startRecording()
            val empty = ShortArray(500)
            while (isActive) {
                    // read the data into the buffer
                    val readSize = recorder.read(buffer, 0, buffer.size)
                    // on startup there will be zero bytes, strip them out
                    if (buffer.sliceArray(1..500).contentEquals(empty)) {
                        val buffer2 = buffer.dropWhile { it.toInt() == 0 }.toShortArray()
                        val readSize2 = buffer2.size
                        writeShorts(readSize2, buffer2, outputStream!!)
                    } else {
                        writeShorts(readSize, buffer, outputStream!!)
                    }
            }
            println("file closed")
        }
    }

    private fun writeShorts(readSize: Int, buffer: ShortArray, outputStream: FileOutputStream) {
        var maxAmplitude = 0.0
        for (i in 0 until readSize) {
            val current = abs(buffer[i].toDouble())
            if (current > maxAmplitude) {
                maxAmplitude = current
            }
        }
        var db = 0.0
        if (maxAmplitude != 0.0) {
            db = 20.0 * log10(maxAmplitude / 32767.0) + 90
        }
        writePCMToMP3(buffer, readSize)
        println("Max amplitude: $maxAmplitude ; DB: $db")
        notificationCallback("$db")
    }

    private fun writePCMToMP3(buffer: ShortArray, readSize: Int) {
        val mp3Buffer = ByteArray(ceil(1.25*buffer.size + 7200).toInt())
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
    }

    override fun stopRecording() {
        runBlocking {
            recordingThread?.cancelAndJoin()
            println("stopped")
            close()
        }
        println("stopped")
    }

    override fun pauseRecording() {
        TODO("Not yet implemented")
    }

    override fun resumeRecoding() {
        TODO("Not yet implemented")
    }

}