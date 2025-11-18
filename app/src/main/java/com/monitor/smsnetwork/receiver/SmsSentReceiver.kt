package com.monitor.smsnetwork.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.DiagnosticEvent
import com.monitor.smsnetwork.data.entity.SmsEvent
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {
    private val TAG = "SmsSentReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: "Unknown"
        val messageBody = intent.getStringExtra("messageBody") ?: ""
        val networkHelper = NetworkStateHelper(context)

        val status: String
        val failureReason: String?

        when (resultCode) {
            Activity.RESULT_OK -> {
                status = "sent"
                failureReason = null
                Log.d(TAG, "SMS sent successfully to $phoneNumber")
            }
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                status = "failed"
                failureReason = "Generic failure"
                Log.e(TAG, "SMS send failed: Generic failure")
            }
            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                status = "failed"
                failureReason = "No service"
                Log.e(TAG, "SMS send failed: No service")
            }
            SmsManager.RESULT_ERROR_NULL_PDU -> {
                status = "failed"
                failureReason = "Null PDU"
                Log.e(TAG, "SMS send failed: Null PDU")
            }
            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                status = "failed"
                failureReason = "Radio off"
                Log.e(TAG, "SMS send failed: Radio off")
            }
            else -> {
                status = "failed"
                failureReason = "Unknown error (code: $resultCode)"
                Log.e(TAG, "SMS send failed: Unknown error $resultCode")
            }
        }

        val event = SmsEvent(
            timestamp = System.currentTimeMillis(),
            direction = "outgoing",
            phoneNumber = phoneNumber,
            messageBody = messageBody,
            status = status,
            failureReason = failureReason,
            networkType = networkHelper.getCurrentNetworkType(),
            signalStrength = networkHelper.getSignalStrength(),
            isWifiCalling = networkHelper.isWifiCallingActive(),
            latitude = null,
            longitude = null,
            simSlot = 0,
            messageType = "SMS"
        )

        // Save to database and create diagnostic if failed
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = MonitorDatabase.getDatabase(context)
                val eventId = db.smsEventDao().insert(event)

                // If failed, create a diagnostic event
                if (status == "failed") {
                    val diagnostic = DiagnosticEvent(
                        timestamp = System.currentTimeMillis(),
                        category = "sms_failure",
                        severity = "error",
                        title = "SMS Send Failure",
                        message = "Failed to send SMS to $phoneNumber: $failureReason",
                        details = "Network: ${event.networkType}, WiFi Calling: ${event.isWifiCalling}, Signal: ${event.signalStrength}",
                        relatedSmsEventId = eventId,
                        relatedNetworkEventId = null,
                        relatedTrafficEventId = null
                    )
                    db.diagnosticEventDao().insert(diagnostic)
                }

                Log.d(TAG, "SMS event saved with status: $status")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving SMS event", e)
            }
        }
    }
}
