package com.jacobson.flutter_recording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RecorderBroadcastReceiver(private val recording: RecordingForegroundService) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "flutter.recorder.stop" -> {
                val serviceIntent = Intent(recording.applicationContext, RecordingForegroundService::class.java)
                println("stopped")
                recording.applicationContext.stopService(serviceIntent)
            }
        }
    }
}
