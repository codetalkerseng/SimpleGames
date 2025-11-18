package com.monitor.smsnetwork.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_events")
data class DiagnosticEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val category: String, // "sms_failure", "network_issue", "security_alert", "bandwidth_alert"
    val severity: String, // "info", "warning", "error", "critical"
    val title: String,
    val message: String,
    val details: String?, // JSON string with additional details
    val isResolved: Boolean = false,
    val relatedSmsEventId: Long?,
    val relatedNetworkEventId: Long?,
    val relatedTrafficEventId: Long?
)
