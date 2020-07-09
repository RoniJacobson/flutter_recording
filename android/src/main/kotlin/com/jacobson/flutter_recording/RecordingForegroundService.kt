package com.jacobson.flutter_recording

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class RecordingForegroundService : Service() {
    private val channelID = "ForegroundServiceChannel"
    private var notificationBuilder: NotificationCompat.Builder
    private val notificationID = 1
    private var intent: Intent? = null
    private var notificationManager: NotificationManager? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var recorder: RecordingInterface? = null

    companion object {
        var state = RecorderState.Stopped
    }

    init {
        notificationBuilder = NotificationCompat.Builder(this, channelID)
                .setContentTitle("Recording")
                .setSmallIcon(android.R.drawable.presence_audio_online)
                .setOnlyAlertOnce(true)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val launchIntent: Intent? = applicationContext.packageManager?.getLaunchIntentForPackage(packageName)
        val className = launchIntent?.component?.className
        var pendingIntent: PendingIntent? = null
        if (className != null) {
            val appLaunchIntent = Intent(this, Class.forName(className))
            appLaunchIntent.action = Intent.ACTION_VIEW
            appLaunchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            pendingIntent = PendingIntent.getActivity(this, 0, appLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder = notificationBuilder.setContentIntent(pendingIntent)
        }
        state = RecorderState.Recording
        println("in the service?")
        val filter = IntentFilter()
        filter.addAction("$className.recorder.stop")
        filter.addAction("$className.recorder.pause")
        filter.addAction("$className.recorder.resume")
        broadcastReceiver = RecorderBroadcastReceiver(this, className!!)
        registerReceiver(broadcastReceiver, filter)
        val fileName: String? = intent?.getStringExtra("fileName")
        val sampleRate: Int? = intent?.getIntExtra("sampleRate", 44100)
        val bitRate: Int? = intent?.getIntExtra("bitRate", 32000)
        val callbackRate: Long? = intent?.getIntExtra("callbackRate", 200)?.toLong()
        val timestampBufferLength: Int? = intent?.getIntExtra("timestampBufferLength", 10)
        if (fileName != null && sampleRate != null && bitRate != null && callbackRate != null && timestampBufferLength != null) {
            recorder = MP3Recorder(fileName,
                    sampleRate,
                    bitRate, 5, this::updateNotification, callbackRate, timestampBufferLength)
        }
        recorder?.startRecording()
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    channelID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun updateNotification(text: String) {
        val notification = notificationBuilder.setContentText(text)
                .setVibrate(null)
                .build()
        startForeground(notificationID, notification)
    }

    private fun endNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)
    }

    fun pause() {
        recorder?.pauseRecording()
        state = RecorderState.Paused
    }

    fun resume() {
        recorder?.resumeRecoding()
        state = RecorderState.Recording
    }

    override fun onDestroy() {
        endNotification()
        recorder?.stopRecording()
        unregisterReceiver(broadcastReceiver)
        state = RecorderState.Stopped
        super.onDestroy()
    }
}

enum class RecorderState {
    Recording, Paused, Stopped
}