package com.monitor.smsnetwork.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsDeliveredReceiver : BroadcastReceiver() {
    private val TAG = "SmsDeliveredReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: "Unknown"

        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "SMS delivered successfully to $phoneNumber")
                // Update the SMS event in database to mark as delivered
                CoroutineScope(Dispatchers.IO).launch {
                    // In a real implementation, you'd find the specific event and update it
                    // For now, we just log it
                    Log.d(TAG, "SMS delivery confirmed")
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.e(TAG, "SMS delivery failed to $phoneNumber")
            }
        }
    }
}
