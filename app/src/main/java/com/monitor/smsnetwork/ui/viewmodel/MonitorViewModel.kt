package com.monitor.smsnetwork.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.AppTrafficStats
import com.monitor.smsnetwork.data.entity.DiagnosticEvent
import com.monitor.smsnetwork.data.entity.SmsEvent
import com.monitor.smsnetwork.util.NetworkStateHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val db = MonitorDatabase.getDatabase(application)
    private val networkHelper = NetworkStateHelper(application)

    // SMS Events
    val recentSmsEvents: StateFlow<List<SmsEvent>> = db.smsEventDao()
        .getEventsSince(System.currentTimeMillis() - 24 * 60 * 60 * 1000) // Last 24 hours
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Failed SMS events
    val failedSmsEvents: StateFlow<List<SmsEvent>> = db.smsEventDao()
        .getFailedEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // WiFi calling failures
    val wifiCallingFailures: StateFlow<List<SmsEvent>> = db.smsEventDao()
        .getWifiCallingFailures()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Diagnostic events
    val diagnosticEvents: StateFlow<List<DiagnosticEvent>> = db.diagnosticEventDao()
        .getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Unresolved diagnostic events
    val unresolvedEvents: StateFlow<List<DiagnosticEvent>> = db.diagnosticEventDao()
        .getUnresolvedEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Critical events
    val criticalEvents: StateFlow<List<DiagnosticEvent>> = db.diagnosticEventDao()
        .getCriticalEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Top apps by traffic
    val topApps: StateFlow<List<AppTrafficStats>> = db.appTrafficStatsDao()
        .getTopApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Suspicious apps
    val suspiciousApps: StateFlow<List<AppTrafficStats>> = db.appTrafficStatsDao()
        .getSuspiciousApps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Network status
    val networkStatus: StateFlow<String> = flow {
        while (true) {
            val status = buildString {
                appendLine("Network Type: ${networkHelper.getCurrentNetworkType()}")
                appendLine("Signal Strength: ${networkHelper.getSignalStrength()} dBm")
                appendLine("Signal Level: ${networkHelper.getSignalLevel()}/4")
                appendLine("WiFi Calling: ${if (networkHelper.isWifiCallingActive()) "Active" else "Inactive"}")
                appendLine("Roaming: ${if (networkHelper.isRoaming()) "Yes" else "No"}")
                appendLine("Airplane Mode: ${if (networkHelper.isAirplaneModeOn()) "On" else "Off"}")

                val wifiSsid = networkHelper.getWifiSsid()
                if (wifiSsid != null) {
                    appendLine("WiFi SSID: $wifiSsid")
                    val freq = networkHelper.getWifiFrequency()
                    if (freq != null) {
                        appendLine("WiFi Frequency: $freq MHz")
                    }
                }

                val cellId = networkHelper.getCellId()
                if (cellId != null) {
                    appendLine("Cell ID: $cellId")
                }

                val lac = networkHelper.getLac()
                if (lac != null) {
                    appendLine("LAC: $lac")
                }
            }
            emit(status)
            kotlinx.coroutines.delay(5000) // Update every 5 seconds
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Loading..."
    )

    // Statistics
    val smsFailureCount: StateFlow<Int> = db.smsEventDao()
        .getFailureCount(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markDiagnosticAsResolved(id: Long) {
        viewModelScope.launch {
            db.diagnosticEventDao().markAsResolved(id)
        }
    }

    fun cleanOldData(daysToKeep: Int = 7) {
        viewModelScope.launch {
            val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            db.smsEventDao().deleteOlderThan(cutoffTime)
            db.networkEventDao().deleteOlderThan(cutoffTime)
            db.trafficEventDao().deleteOlderThan(cutoffTime)
            db.diagnosticEventDao().deleteOlderThan(cutoffTime)
        }
    }
}
