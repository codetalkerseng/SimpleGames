package com.monitor.smsnetwork.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_events")
data class NetworkEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val eventType: String, // "connection_change", "wifi_toggle", "signal_change"
    val networkType: String, // "WIFI", "MOBILE", "NONE"
    val subType: String?, // "LTE", "5G", "WIFI_6", etc.
    val isConnected: Boolean,
    val isRoaming: Boolean,
    val signalStrength: Int,
    val signalLevel: Int, // 0-4
    val wifiSsid: String?,
    val wifiFrequency: Int?,
    val cellId: Int?,
    val lac: Int?, // Location Area Code
    val isWifiCalling: Boolean,
    val isAirplaneMode: Boolean
)
