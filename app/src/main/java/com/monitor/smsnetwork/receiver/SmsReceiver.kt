package com.monitor.smsnetwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.TelephonyManager
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.SmsEvent
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "SMS received: ${intent.action}")

        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val networkHelper = NetworkStateHelper(context)

            messages?.forEach { smsMessage ->
                val event = SmsEvent(
                    timestamp = System.currentTimeMillis(),
                    direction = "incoming",
                    phoneNumber = smsMessage.displayOriginatingAddress ?: "Unknown",
                    messageBody = smsMessage.messageBody,
                    status = "received",
                    failureReason = null,
                    networkType = networkHelper.getCurrentNetworkType(),
                    signalStrength = networkHelper.getSignalStrength(),
                    isWifiCalling = networkHelper.isWifiCallingActive(),
                    latitude = null,
                    longitude = null,
                    simSlot = 0,
                    messageType = "SMS"
                )

                // Save to database
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = MonitorDatabase.getDatabase(context)
                        db.smsEventDao().insert(event)
                        Log.d(TAG, "SMS event saved: ${event.phoneNumber}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving SMS event", e)
                    }
                }
            }
        }
    }
}
