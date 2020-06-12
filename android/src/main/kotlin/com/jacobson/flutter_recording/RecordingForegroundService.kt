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
    private val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelID)
            .setContentTitle("Recording")
            .setSmallIcon(android.R.drawable.presence_audio_online)
    private val notificationID = 1
    private var intent: Intent? = null
    private var notificationManager: NotificationManager? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        this.intent = intent
        println("in the service?")
        updateNotification("hi")
        updateNotification("also hi")
        val filter = IntentFilter()
        filter.addAction("flutter.recorder.stop")
        filter.addAction("flutter.recorder.pause")
        broadcastReceiver = RecorderBroadcastReceiver(this)
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
        val notificationIntent = Intent(this, intent?.javaClass)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0)
        val notification = notificationBuilder.setContentIntent(pendingIntent)
                .setContentText(text)
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