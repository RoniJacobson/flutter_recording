package com.jacobson.flutter_recording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData

class RecorderBroadcastReceiver(private val recording: RecordingForegroundService, private val className: String) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println(intent?.action)
        when (intent?.action) {
            "$className.recorder.stop" -> {
                val serviceIntent = Intent(recording.applicationContext, RecordingForegroundService::class.java)
                println("stopped")
                recording.applicationContext.stopService(serviceIntent)
            }
            "$className.recorder.pause" -> {
                recording.pause()
            }
            "$className.recorder.resume" -> {
                recording.resume()
            }
        }
    }
}
