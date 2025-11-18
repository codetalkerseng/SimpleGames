package com.monitor.smsnetwork.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monitor.smsnetwork.service.NetworkMonitorVpnService
import com.monitor.smsnetwork.service.NetworkStateService
import com.monitor.smsnetwork.service.SmsMonitorService
import com.monitor.smsnetwork.ui.theme.SMSMMSMonitorTheme
import com.monitor.smsnetwork.ui.viewmodel.MonitorViewModel

class MainActivity : ComponentActivity() {
    private val permissionRequestCode = 100
    private val vpnRequestCode = 101

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        } else {
            Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions
        checkAndRequestPermissions()

        setContent {
            SMSMMSMonitorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MonitorDashboard(
                        onStartVpn = { requestVpnPermission() },
                        onStartMonitoring = { startMonitoringServices() },
                        onStopMonitoring = { stopMonitoringServices() }
                    )
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                permissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val intent = Intent(this, NetworkMonitorVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "VPN monitoring started", Toast.LENGTH_SHORT).show()
    }

    private fun startMonitoringServices() {
        SmsMonitorService.start(this)
        NetworkStateService.start(this)
        Toast.makeText(this, "Monitoring services started", Toast.LENGTH_SHORT).show()
    }

    private fun stopMonitoringServices() {
        SmsMonitorService.stop(this)
        NetworkStateService.stop(this)
        val vpnIntent = Intent(this, NetworkMonitorVpnService::class.java)
        stopService(vpnIntent)
        Toast.makeText(this, "Monitoring services stopped", Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorDashboard(
    onStartVpn: () -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    viewModel: MonitorViewModel = viewModel()
) {
    val smsEvents by viewModel.recentSmsEvents.collectAsState()
    val diagnosticEvents by viewModel.diagnosticEvents.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val topApps by viewModel.topApps.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "SMS/MMS", "Network", "Traffic", "Alerts")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS/MMS Network Monitor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Control buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onStartMonitoring) {
                    Text("Start Monitoring")
                }
                Button(onClick = onStartVpn) {
                    Text("Start VPN")
                }
                Button(onClick = onStopMonitoring) {
                    Text("Stop All")
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> OverviewTab(networkStatus, diagnosticEvents.take(5))
                1 -> SmsTab(smsEvents)
                2 -> NetworkTab(networkStatus)
                3 -> TrafficTab(topApps)
                4 -> AlertsTab(diagnosticEvents)
            }
        }
    }
}

@Composable
fun OverviewTab(networkStatus: String, recentAlerts: List<com.monitor.smsnetwork.data.entity.DiagnosticEvent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Network Status", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(networkStatus, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        item {
            Text(
                "Recent Alerts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(recentAlerts) { alert ->
            AlertCard(alert)
        }
    }
}

@Composable
fun SmsTab(events: List<com.monitor.smsnetwork.data.entity.SmsEvent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "SMS/MMS Events",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(events) { event ->
            SmsEventCard(event)
        }
    }
}

@Composable
fun SmsEventCard(event: com.monitor.smsnetwork.data.entity.SmsEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (event.status) {
                "failed" -> MaterialTheme.colorScheme.errorContainer
                "sent", "received" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "${event.direction.uppercase()} ${event.messageType}",
                style = MaterialTheme.typography.labelMedium
            )
            Text("${event.phoneNumber}", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${event.status}", style = MaterialTheme.typography.bodySmall)
            Text("Network: ${event.networkType}", style = MaterialTheme.typography.bodySmall)
            if (event.isWifiCalling) {
                Text("WiFi Calling: Yes", style = MaterialTheme.typography.bodySmall)
            }
            Text("Signal: ${event.signalStrength} dBm", style = MaterialTheme.typography.bodySmall)
            if (event.failureReason != null) {
                Text(
                    "Failure: ${event.failureReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NetworkTab(status: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Network Status", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(status, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun TrafficTab(topApps: List<com.monitor.smsnetwork.data.entity.AppTrafficStats>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "Top Bandwidth Consumers",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(topApps) { app ->
            AppTrafficCard(app)
        }
    }
}

@Composable
fun AppTrafficCard(app: com.monitor.smsnetwork.data.entity.AppTrafficStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(app.appName, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Total: ${formatBytes(app.totalBytesIn + app.totalBytesOut)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Down: ${formatBytes(app.totalBytesIn)} / Up: ${formatBytes(app.totalBytesOut)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (app.suspiciousActivityCount > 0) {
                Text(
                    "Suspicious activities: ${app.suspiciousActivityCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AlertsTab(alerts: List<com.monitor.smsnetwork.data.entity.DiagnosticEvent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                "Diagnostic Alerts",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(alerts) { alert ->
            AlertCard(alert)
        }
    }
}

@Composable
fun AlertCard(alert: com.monitor.smsnetwork.data.entity.DiagnosticEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                "critical" -> MaterialTheme.colorScheme.errorContainer
                "error" -> MaterialTheme.colorScheme.errorContainer
                "warning" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                alert.title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(alert.message, style = MaterialTheme.typography.bodyMedium)
            if (alert.details != null) {
                Text(alert.details, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Category: ${alert.category} | Severity: ${alert.severity}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_000_000_000 -> "%.2f GB".format(bytes / 1_000_000_000.0)
        bytes >= 1_000_000 -> "%.2f MB".format(bytes / 1_000_000.0)
        bytes >= 1_000 -> "%.2f KB".format(bytes / 1_000.0)
        else -> "$bytes B"
    }
}
