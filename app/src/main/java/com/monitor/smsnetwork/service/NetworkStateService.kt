package com.monitor.smsnetwork.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.*
import androidx.core.app.NotificationCompat
import com.monitor.smsnetwork.R
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.NetworkEvent
import com.monitor.smsnetwork.ui.MainActivity
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NetworkStateService : Service() {
    private val CHANNEL_ID = "NetworkStateChannel"
    private val NOTIFICATION_ID = 1003

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var networkHelper: NetworkStateHelper
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            logSignalChange()
        }

        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            super.onDataConnectionStateChanged(state, networkType)
            logNetworkChange("data_connection_change")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        networkHelper = NetworkStateHelper(this)

        // Start listening to phone state changes
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS or
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE)
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
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        serviceJob.cancel()
    }

    private fun logSignalChange() {
        logNetworkChange("signal_change")
    }

    private fun logNetworkChange(eventType: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val event = NetworkEvent(
                    timestamp = System.currentTimeMillis(),
                    eventType = eventType,
                    networkType = networkHelper.getCurrentNetworkType(),
                    subType = networkHelper.getCurrentNetworkType(),
                    isConnected = true, // Simplified
                    isRoaming = networkHelper.isRoaming(),
                    signalStrength = networkHelper.getSignalStrength(),
                    signalLevel = networkHelper.getSignalLevel(),
                    wifiSsid = networkHelper.getWifiSsid(),
                    wifiFrequency = networkHelper.getWifiFrequency(),
                    cellId = networkHelper.getCellId(),
                    lac = networkHelper.getLac(),
                    isWifiCalling = networkHelper.isWifiCallingActive(),
                    isAirplaneMode = networkHelper.isAirplaneModeOn()
                )

                val db = MonitorDatabase.getDatabase(applicationContext)
                db.networkEventDao().insert(event)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Network State Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors network state changes"
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
            .setContentTitle("Network State Monitor")
            .setContentText("Monitoring network state changes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, NetworkStateService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NetworkStateService::class.java)
            context.stopService(intent)
        }
    }
}
