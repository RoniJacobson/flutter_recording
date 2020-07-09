package com.jacobson.flutter_recording

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.EventChannel

object TimeDBStream: EventChannel.StreamHandler {
    var events: EventChannel.EventSink? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        this.events = events
    }

    fun sendInfo(timestampsList: MutableList<List<Number>>) {
        Handler(Looper.getMainLooper()).post {
            events?.success(timestampsList)
        }
    }

    override fun onCancel(arguments: Any?) {
        return
    }
}