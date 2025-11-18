package com.monitor.smsnetwork.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.monitor.smsnetwork.R
import com.monitor.smsnetwork.receiver.NetworkStateReceiver
import com.monitor.smsnetwork.receiver.SmsReceiver
import com.monitor.smsnetwork.receiver.MmsReceiver
import com.monitor.smsnetwork.ui.MainActivity

class SmsMonitorService : Service() {
    private val CHANNEL_ID = "SMSMonitorChannel"
    private val NOTIFICATION_ID = 1001

    private val smsReceiver = SmsReceiver()
    private val mmsReceiver = MmsReceiver()
    private val networkReceiver = NetworkStateReceiver()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Register broadcast receivers
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))

        val mmsFilter = IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED")
        mmsFilter.addDataType("application/vnd.wap.mms-message")
        registerReceiver(mmsReceiver, mmsFilter)

        val networkFilter = IntentFilter().apply {
            addAction("android.net.conn.CONNECTIVITY_CHANGE")
            addAction("android.net.wifi.WIFI_STATE_CHANGED")
            addAction("android.net.wifi.STATE_CHANGE")
        }
        registerReceiver(networkReceiver, networkFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(smsReceiver)
            unregisterReceiver(mmsReceiver)
            unregisterReceiver(networkReceiver)
        } catch (e: Exception) {
            // Receivers may not be registered
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "SMS/MMS Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors SMS/MMS messages and network state"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS/MMS Monitor")
            .setContentText("Monitoring SMS/MMS messages and network state")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SmsMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SmsMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
