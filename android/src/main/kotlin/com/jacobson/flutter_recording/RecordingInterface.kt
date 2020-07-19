package com.jacobson.flutter_recording

import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.log10

abstract class RecordingSuper (protected val fileName: String, protected val bitRate: Int, protected val sampleRate: Int, protected val notificationCallback: (name: String) -> Unit, protected val callbackRate: Long, private val timestampBufferLength: Int) {
    private val timestampsList: MutableList<List<Number>> = mutableListOf()
    private var currentTime = -callbackRate.toInt()
    protected var infoTimer = Timer()
    protected var lastTimerTick: Long = System.currentTimeMillis()
    protected var pauseTime: Long = System.currentTimeMillis()
    abstract fun startRecording()
    abstract fun stopRecording()
    abstract fun getMaxAmplitude(): Double

    open fun pauseRecording() {
        infoTimer.cancel()
        pauseTime = System.currentTimeMillis()
    }

    open fun resumeRecoding() {
        infoTimer = Timer()
        infoTimer.scheduleAtFixedRate(timerTask { timerFunction() }, callbackRate - (pauseTime - lastTimerTick), callbackRate)
    }

    fun amplitudeToDecibels(amplitude: Double): Double {
        return 20.0 * log10(amplitude / 32767.0) + 90
    }

    private fun hms(currentTime: Int): String {
        val fullSeconds = currentTime / 1000;
        val seconds = fullSeconds % 60;
        val minutes = fullSeconds / 60 % 60;
        val hours = fullSeconds / 3600;
        return "$hours:$minutes:$seconds";
    }

    fun timerFunction() {
        synchronized(this) {
            lastTimerTick = System.currentTimeMillis()
            currentTime += callbackRate.toInt()
            timestampsList.add(listOf(currentTime, amplitudeToDecibels(getMaxAmplitude())))
            TimeDBStream.sendInfo(timestampsList.toMutableList())
            if (timestampsList.count() == timestampBufferLength)
                timestampsList.removeAt(0)
            notificationCallback(hms(currentTime))
        }
    }
}