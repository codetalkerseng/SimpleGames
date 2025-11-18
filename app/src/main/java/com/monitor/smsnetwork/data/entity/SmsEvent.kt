package com.monitor.smsnetwork.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_events")
data class SmsEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val direction: String, // "incoming" or "outgoing"
    val phoneNumber: String,
    val messageBody: String?,
    val status: String, // "received", "sent", "failed", "delivered"
    val failureReason: String?,
    val networkType: String, // "WIFI", "LTE", "5G", "3G", etc.
    val signalStrength: Int,
    val isWifiCalling: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val simSlot: Int,
    val messageType: String // "SMS" or "MMS"
)
