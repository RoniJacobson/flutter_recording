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
        this.intent = intent
        println("in the service?")
        val filter = IntentFilter()
        filter.addAction("$className.recorder.stop")
        filter.addAction("$className.recorder.pause")
        broadcastReceiver = RecorderBroadcastReceiver(this, className!!)
        registerReceiver(broadcastReceiver, filter)
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

    override fun onDestroy() {
        endNotification()
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }
}