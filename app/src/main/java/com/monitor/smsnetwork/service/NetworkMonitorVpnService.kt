package com.monitor.smsnetwork.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.monitor.smsnetwork.R
import com.monitor.smsnetwork.ui.MainActivity
import com.monitor.smsnetwork.util.PacketAnalyzer
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

class NetworkMonitorVpnService : VpnService() {
    private val TAG = "NetworkMonitorVpnService"
    private val CHANNEL_ID = "VPNMonitorChannel"
    private val NOTIFICATION_ID = 1002

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceJob: Job? = null
    private lateinit var packetAnalyzer: PacketAnalyzer

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        packetAnalyzer = PacketAnalyzer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_VPN) {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        try {
            // Configure VPN interface
            val builder = Builder()
                .setSession("SMS/MMS Network Monitor")
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")
                .setMtu(1500)

            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                val notification = createNotification()
                startForeground(NOTIFICATION_ID, notification)

                // Start packet processing
                serviceJob = CoroutineScope(Dispatchers.IO).launch {
                    processPackets()
                }

                Log.d(TAG, "VPN started successfully")
            } else {
                Log.e(TAG, "Failed to establish VPN")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopSelf()
        }
    }

    private suspend fun processPackets() {
        val vpnInput = FileInputStream(vpnInterface?.fileDescriptor)
        val vpnOutput = FileOutputStream(vpnInterface?.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)

        try {
            while (isActive && vpnInterface != null) {
                // Read packet from VPN interface
                packet.clear()
                val length = vpnInput.read(packet.array())

                if (length > 0) {
                    packet.limit(length)

                    // Analyze packet
                    packetAnalyzer.analyzePacket(packet)

                    // Forward packet (simplified - in production you'd need proper routing)
                    try {
                        forwardPacket(packet)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error forwarding packet", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing packets", e)
        } finally {
            vpnInput.close()
            vpnOutput.close()
        }
    }

    private fun forwardPacket(packet: ByteBuffer) {
        // This is a simplified version - proper implementation would involve
        // parsing IP header, determining destination, and forwarding appropriately
        // For a production VPN, you'd need to implement proper TCP/UDP handling

        try {
            // Parse basic IP header
            val version = (packet.get(0).toInt() shr 4) and 0xF
            if (version == 4) {
                // IPv4 packet
                val protocol = packet.get(9).toInt() and 0xFF

                when (protocol) {
                    17 -> { // UDP
                        forwardUdpPacket(packet)
                    }
                    6 -> { // TCP
                        forwardTcpPacket(packet)
                    }
                    // Other protocols can be handled here
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing packet", e)
        }
    }

    private fun forwardUdpPacket(packet: ByteBuffer) {
        // Simplified UDP forwarding
        // In production, you'd need to maintain connection state
        try {
            val channel = DatagramChannel.open()
            channel.configureBlocking(false)

            // Parse destination from IP header (simplified)
            // This is just a placeholder - proper implementation needed
            val destAddress = InetSocketAddress("8.8.8.8", 53)

            channel.send(packet, destAddress)
            channel.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding UDP packet", e)
        }
    }

    private fun forwardTcpPacket(packet: ByteBuffer) {
        // TCP forwarding is more complex and requires maintaining connection state
        // This is a placeholder - proper implementation would use a connection pool
        Log.d(TAG, "TCP packet forwarding (not fully implemented)")
    }

    private fun stopVpn() {
        serviceJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
        Log.d(TAG, "VPN stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    override fun onRevoke() {
        super.onRevoke()
        stopVpn()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "VPN Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors network traffic"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NetworkMonitorVpnService::class.java).apply {
            action = ACTION_STOP_VPN
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Network Monitor VPN")
            .setContentText("Monitoring all network traffic")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }

    companion object {
        const val ACTION_STOP_VPN = "com.monitor.smsnetwork.STOP_VPN"
    }
}
