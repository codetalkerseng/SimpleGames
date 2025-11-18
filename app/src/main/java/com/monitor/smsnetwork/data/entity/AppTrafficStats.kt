package com.monitor.smsnetwork.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_traffic_stats")
data class AppTrafficStats(
    @PrimaryKey
    val appPackage: String,
    val appName: String,
    val totalBytesIn: Long,
    val totalBytesOut: Long,
    val totalPackets: Long,
    val connectionCount: Int,
    val firstSeen: Long,
    val lastSeen: Long,
    val suspiciousActivityCount: Int,
    val blockedCount: Int
)
