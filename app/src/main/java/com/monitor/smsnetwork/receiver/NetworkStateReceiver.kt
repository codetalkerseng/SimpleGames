package com.monitor.smsnetwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.NetworkEvent
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetworkStateReceiver : BroadcastReceiver() {
    private val TAG = "NetworkStateReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Network state changed: ${intent.action}")

        val networkHelper = NetworkStateHelper(context)
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }

        val isConnected = capabilities != null
        val networkType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WIFI"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "MOBILE"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ETHERNET"
            else -> "NONE"
        }

        val event = NetworkEvent(
            timestamp = System.currentTimeMillis(),
            eventType = "connection_change",
            networkType = networkType,
            subType = networkHelper.getCurrentNetworkType(),
            isConnected = isConnected,
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

        // Save to database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MonitorDatabase.getDatabase(context)
                db.networkEventDao().insert(event)
                Log.d(TAG, "Network event saved: $networkType")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving network event", e)
            }
        }
    }
}
