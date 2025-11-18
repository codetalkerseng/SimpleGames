package com.monitor.smsnetwork.util

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.monitor.smsnetwork.data.MonitorDatabase
import com.monitor.smsnetwork.data.entity.AppTrafficStats
import com.monitor.smsnetwork.data.entity.DiagnosticEvent
import com.monitor.smsnetwork.data.entity.TrafficEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class PacketAnalyzer(private val context: Context) {
    private val TAG = "PacketAnalyzer"
    private val db = MonitorDatabase.getDatabase(context)
    private val packageManager = context.packageManager

    // Known malicious IP patterns (simplified - in production, use threat intelligence feeds)
    private val knownMaliciousIps = setOf(
        // Add known malicious IPs here
    )

    // Known malicious ports
    private val suspiciousPorts = setOf(
        4444, // Metasploit default
        5555, // Android Debug Bridge (ADB) - suspicious if external
        6667, // IRC - often used by botnets
        31337, // Common backdoor port
        12345, // NetBus
        54321 // Back Orifice
    )

    fun analyzePacket(packet: ByteBuffer) {
        try {
            packet.rewind()

            // Parse IP header (minimum 20 bytes for IPv4)
            if (packet.remaining() < 20) return

            val version = (packet.get(0).toInt() shr 4) and 0xF
            if (version != 4) return // Only handle IPv4 for now

            val ihl = (packet.get(0).toInt() and 0xF) * 4
            val protocol = packet.get(9).toInt() and 0xFF
            val totalLength = ((packet.get(2).toInt() and 0xFF) shl 8) or (packet.get(3).toInt() and 0xFF)

            // Extract source and destination IPs
            val sourceIp = extractIpAddress(packet, 12)
            val destIp = extractIpAddress(packet, 16)

            var sourcePort = 0
            var destPort = 0
            var isDns = false
            var dnsQuery: String? = null
            var isEncrypted = false
            var flags: String? = null

            // Parse transport layer
            when (protocol) {
                6 -> { // TCP
                    if (packet.remaining() >= ihl + 4) {
                        sourcePort = ((packet.get(ihl).toInt() and 0xFF) shl 8) or (packet.get(ihl + 1).toInt() and 0xFF)
                        destPort = ((packet.get(ihl + 2).toInt() and 0xFF) shl 8) or (packet.get(ihl + 3).toInt() and 0xFF)

                        // Check for encrypted connections (HTTPS, etc.)
                        isEncrypted = destPort == 443 || sourcePort == 443

                        // Extract TCP flags
                        if (packet.remaining() >= ihl + 14) {
                            val tcpFlags = packet.get(ihl + 13).toInt() and 0xFF
                            flags = parseTcpFlags(tcpFlags)
                        }
                    }
                }
                17 -> { // UDP
                    if (packet.remaining() >= ihl + 4) {
                        sourcePort = ((packet.get(ihl).toInt() and 0xFF) shl 8) or (packet.get(ihl + 1).toInt() and 0xFF)
                        destPort = ((packet.get(ihl + 2).toInt() and 0xFF) shl 8) or (packet.get(ihl + 3).toInt() and 0xFF)

                        // Check if it's a DNS query
                        isDns = destPort == 53 || sourcePort == 53
                        if (isDns) {
                            dnsQuery = extractDnsQuery(packet, ihl + 8)
                        }
                    }
                }
            }

            // Calculate suspicious score
            val suspiciousScore = calculateSuspiciousScore(
                sourceIp, destIp, sourcePort, destPort, protocol, totalLength
            )

            // Determine app package (simplified - would need UID mapping in production)
            val appPackage = determineAppPackage(sourcePort, destPort)

            // Create traffic event
            val event = TrafficEvent(
                timestamp = System.currentTimeMillis(),
                protocol = getProtocolName(protocol),
                sourceIp = sourceIp,
                sourcePort = sourcePort,
                destIp = destIp,
                destPort = destPort,
                bytesIn = if (isIncoming(destIp)) totalLength.toLong() else 0,
                bytesOut = if (!isIncoming(destIp)) totalLength.toLong() else 0,
                packetsIn = if (isIncoming(destIp)) 1 else 0,
                packetsOut = if (!isIncoming(destIp)) 1 else 0,
                appPackage = appPackage,
                appName = getAppName(appPackage),
                isDns = isDns,
                dnsQuery = dnsQuery,
                isEncrypted = isEncrypted,
                suspiciousScore = suspiciousScore,
                flags = flags
            )

            // Save to database
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.trafficEventDao().insert(event)

                    // Update app statistics
                    updateAppStats(appPackage, event)

                    // Create diagnostic alert if suspicious
                    if (suspiciousScore >= 70) {
                        createSecurityAlert(event, suspiciousScore)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving traffic event", e)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing packet", e)
        }
    }

    private fun extractIpAddress(packet: ByteBuffer, offset: Int): String {
        val byte1 = packet.get(offset).toInt() and 0xFF
        val byte2 = packet.get(offset + 1).toInt() and 0xFF
        val byte3 = packet.get(offset + 2).toInt() and 0xFF
        val byte4 = packet.get(offset + 3).toInt() and 0xFF
        return "$byte1.$byte2.$byte3.$byte4"
    }

    private fun parseTcpFlags(flags: Int): String {
        val flagList = mutableListOf<String>()
        if (flags and 0x01 != 0) flagList.add("FIN")
        if (flags and 0x02 != 0) flagList.add("SYN")
        if (flags and 0x04 != 0) flagList.add("RST")
        if (flags and 0x08 != 0) flagList.add("PSH")
        if (flags and 0x10 != 0) flagList.add("ACK")
        if (flags and 0x20 != 0) flagList.add("URG")
        return flagList.joinToString(",")
    }

    private fun extractDnsQuery(packet: ByteBuffer, offset: Int): String? {
        // Simplified DNS query extraction
        // Full implementation would parse DNS protocol properly
        return try {
            "DNS_Query" // Placeholder
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateSuspiciousScore(
        sourceIp: String,
        destIp: String,
        sourcePort: Int,
        destPort: Int,
        protocol: Int,
        size: Int
    ): Int {
        var score = 0

        // Check against known malicious IPs
        if (destIp in knownMaliciousIps || sourceIp in knownMaliciousIps) {
            score += 50
        }

        // Check for suspicious ports
        if (destPort in suspiciousPorts || sourcePort in suspiciousPorts) {
            score += 30
        }

        // Check for unusual packet sizes
        if (size > 60000) {
            score += 10
        }

        // Check for connections to private IPs from internet (potential DNS rebinding)
        if (!isPrivateIp(sourceIp) && isPrivateIp(destIp)) {
            score += 20
        }

        // Check for multiple rapid connections (would need state tracking)
        // Placeholder for now

        return score.coerceIn(0, 100)
    }

    private fun isPrivateIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false

        val first = parts[0].toIntOrNull() ?: return false
        val second = parts[1].toIntOrNull() ?: return false

        return when (first) {
            10 -> true
            172 -> second in 16..31
            192 -> second == 168
            else -> false
        }
    }

    private fun isIncoming(destIp: String): Boolean {
        // Check if packet is incoming by checking if destination is local
        return isPrivateIp(destIp)
    }

    private fun getProtocolName(protocol: Int): String {
        return when (protocol) {
            1 -> "ICMP"
            6 -> "TCP"
            17 -> "UDP"
            else -> "OTHER($protocol)"
        }
    }

    private fun determineAppPackage(sourcePort: Int, destPort: Int): String? {
        // In a real implementation, you would map ports to UIDs to apps
        // This requires reading /proc/net/tcp and /proc/net/udp
        // and mapping UIDs to package names
        return null // Placeholder
    }

    private fun getAppName(packageName: String?): String? {
        if (packageName == null) return null

        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private suspend fun updateAppStats(packageName: String?, event: TrafficEvent) {
        if (packageName == null) return

        try {
            var stats = db.appTrafficStatsDao().getStatsForApp(packageName)

            if (stats == null) {
                stats = AppTrafficStats(
                    appPackage = packageName,
                    appName = event.appName ?: packageName,
                    totalBytesIn = event.bytesIn,
                    totalBytesOut = event.bytesOut,
                    totalPackets = (event.packetsIn + event.packetsOut).toLong(),
                    connectionCount = 1,
                    firstSeen = event.timestamp,
                    lastSeen = event.timestamp,
                    suspiciousActivityCount = if (event.suspiciousScore >= 50) 1 else 0,
                    blockedCount = 0
                )
            } else {
                stats = stats.copy(
                    totalBytesIn = stats.totalBytesIn + event.bytesIn,
                    totalBytesOut = stats.totalBytesOut + event.bytesOut,
                    totalPackets = stats.totalPackets + event.packetsIn + event.packetsOut,
                    connectionCount = stats.connectionCount + 1,
                    lastSeen = event.timestamp,
                    suspiciousActivityCount = stats.suspiciousActivityCount +
                            if (event.suspiciousScore >= 50) 1 else 0
                )
            }

            db.appTrafficStatsDao().insert(stats)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app stats", e)
        }
    }

    private suspend fun createSecurityAlert(event: TrafficEvent, score: Int) {
        val diagnostic = DiagnosticEvent(
            timestamp = System.currentTimeMillis(),
            category = "security_alert",
            severity = if (score >= 80) "critical" else "warning",
            title = "Suspicious Network Activity Detected",
            message = "High risk connection: ${event.sourceIp}:${event.sourcePort} -> ${event.destIp}:${event.destPort}",
            details = "Protocol: ${event.protocol}, Score: $score, App: ${event.appName ?: "Unknown"}",
            relatedSmsEventId = null,
            relatedNetworkEventId = null,
            relatedTrafficEventId = null
        )

        db.diagnosticEventDao().insert(diagnostic)
    }
}
