package com.monitor.smsnetwork.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traffic_events")
data class TrafficEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val protocol: String, // "TCP", "UDP", "ICMP", etc.
    val sourceIp: String,
    val sourcePort: Int,
    val destIp: String,
    val destPort: Int,
    val bytesIn: Long,
    val bytesOut: Long,
    val packetsIn: Int,
    val packetsOut: Int,
    val appPackage: String?,
    val appName: String?,
    val isDns: Boolean,
    val dnsQuery: String?,
    val isEncrypted: Boolean,
    val suspiciousScore: Int, // 0-100, heuristic for malicious activity
    val flags: String? // TCP flags or other protocol-specific info
)
