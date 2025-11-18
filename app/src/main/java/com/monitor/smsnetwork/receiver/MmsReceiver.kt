package com.monitor.smsnetwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.SmsEvent
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MmsReceiver : BroadcastReceiver() {
    private val TAG = "MmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "MMS received: ${intent.action}")

        // MMS handling is more complex and requires WAP push handling
        val networkHelper = NetworkStateHelper(context)

        val event = SmsEvent(
            timestamp = System.currentTimeMillis(),
            direction = "incoming",
            phoneNumber = "Unknown", // MMS doesn't provide this in WAP push
            messageBody = null, // MMS body requires further parsing
            status = "received",
            failureReason = null,
            networkType = networkHelper.getCurrentNetworkType(),
            signalStrength = networkHelper.getSignalStrength(),
            isWifiCalling = networkHelper.isWifiCallingActive(),
            latitude = null,
            longitude = null,
            simSlot = 0,
            messageType = "MMS"
        )

        // Save to database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MonitorDatabase.getDatabase(context)
                db.smsEventDao().insert(event)
                Log.d(TAG, "MMS event saved")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving MMS event", e)
            }
        }
    }
}
