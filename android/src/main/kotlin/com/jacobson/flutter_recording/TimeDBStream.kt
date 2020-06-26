package com.jacobson.flutter_recording

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel
import java.time.Instant
import java.util.*

object TimeDBStream: EventChannel.StreamHandler {
    var events: EventChannel.EventSink? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.events = events
    }

    fun sendInfo(timestamp: Int, maxDecibel: Double) {
        Handler(Looper.getMainLooper()).post {
            events?.success(arrayListOf(timestamp, maxDecibel))
        }
    }

    override fun onCancel(arguments: Any?) {
        return
    }
}