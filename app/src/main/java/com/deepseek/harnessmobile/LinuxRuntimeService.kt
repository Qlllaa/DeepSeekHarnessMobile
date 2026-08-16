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
        @JvmStatic
        var runtimeState: RuntimeState = RuntimeState.IDLE
            set(value) {
                field = value
                Log.d(TAG, "State changed to: $value")
            }
    }

    private var job: Job? = null
    private var processRunner: ProcessRunner? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("初始化中..."))
        RuntimeManager.instance = RuntimeManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRuntime()
            ACTION_STOP -> stopRuntime()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopRuntime()
        Log.d(TAG, "Service destroyed")
    }

    private fun startRuntime() {
        if (job != null && job!!.isActive) return
        job = Job()
        CoroutineScope(Dispatchers.IO + job!!).launch {
            try {
                updateState(RuntimeState.INITIALIZING)
                updateNotification("正在初始化...")
                
                val rm = RuntimeManager.instance ?: return@launch
                rm.initializeEnvironment()
                updateState(RuntimeState.UBUNTU_READY)
                updateNotification("Ubuntu 就绪")
                
                rm.startPrroot()
                updateState(RuntimeState.HARNESS_RUNNING)
                updateNotification("DeepSeek Harness 运行中")
                
            } catch (e: Exception) {
                Log.e(TAG, "Runtime error", e)
                updateState(RuntimeState.ERROR)
                updateNotification("错误: ${e.message}")
            }
        }
    }

    private fun stopRuntime() {
        job?.cancel()
        job = null
        processRunner?.destroy()
        processRunner = null
        updateState(RuntimeState.IDLE)
        updateNotification("已停止")
    }

    private fun updateState(state: RuntimeState) {
        runtimeState = state
    }

    private fun updateNotification(content: String) {
        val notification = buildNotification(content)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
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
}

enum class RuntimeState {
    IDLE,
    INITIALIZING,
    UBUNTU_READY,
    HARNESS_RUNNING,
    ERROR
}
