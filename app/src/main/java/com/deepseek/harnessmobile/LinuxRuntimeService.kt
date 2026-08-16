package com.deepseek.harnessmobile

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LinuxRuntimeService : Service() {

    companion object {
        private const val TAG = "LinuxRuntimeService"
        private const val CHANNEL_ID = "deepseek_harness_runtime"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.deepseek.harnessmobile.ACTION_START"
        const val ACTION_STOP = "com.deepseek.harnessmobile.ACTION_STOP"
    }

    private var runtimeManager: RuntimeManager? = null
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Initializing..."))
        runtimeManager = RuntimeManager(this)
        job = Job()
        CoroutineScope(Dispatchers.IO + job!!).launch {
            runtimeManager?.start()
            updateNotification("Ubuntu Running")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                Log.d(TAG, "Starting runtime...")
                job?.let {
                    CoroutineScope(Dispatchers.IO + it).launch {
                        runtimeManager?.start()
                        updateNotification("Ubuntu Running")
                    }
                }
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping runtime...")
                runtimeManager?.stop()
                updateNotification("Stopped")
                job?.cancel()
                job = null
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        runtimeManager?.stop()
        job?.cancel()
        job = null
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Linux Runtime",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "DeepSeek Harness Ubuntu runtime"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeepSeek Harness")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = buildNotification(content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
