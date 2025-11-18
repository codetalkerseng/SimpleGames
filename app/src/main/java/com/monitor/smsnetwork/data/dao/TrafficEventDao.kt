package com.monitor.smsnetwork.data.dao

import androidx.room.*
import com.monitor.smsnetwork.data.entity.TrafficEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface TrafficEventDao {
    @Query("SELECT * FROM traffic_events ORDER BY timestamp DESC LIMIT 1000")
    fun getRecentEvents(): Flow<List<TrafficEvent>>

    @Query("SELECT * FROM traffic_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEventsSince(startTime: Long): Flow<List<TrafficEvent>>

    @Query("SELECT * FROM traffic_events WHERE suspiciousScore >= :threshold ORDER BY suspiciousScore DESC, timestamp DESC")
    fun getSuspiciousEvents(threshold: Int = 50): Flow<List<TrafficEvent>>

    @Query("SELECT * FROM traffic_events WHERE appPackage = :packageName ORDER BY timestamp DESC LIMIT 100")
    fun getEventsForApp(packageName: String): Flow<List<TrafficEvent>>

    @Query("SELECT SUM(bytesIn + bytesOut) FROM traffic_events WHERE timestamp >= :since")
    suspend fun getTotalBytes(since: Long): Long?

    @Query("SELECT appPackage, SUM(bytesIn + bytesOut) as total FROM traffic_events WHERE timestamp >= :since GROUP BY appPackage ORDER BY total DESC LIMIT 10")
    suspend fun getTopConsumers(since: Long): List<AppBandwidth>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: TrafficEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<TrafficEvent>)

    @Delete
    suspend fun delete(event: TrafficEvent)

    @Query("DELETE FROM traffic_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

data class AppBandwidth(
    val appPackage: String?,
    val total: Long
)
