package com.monitor.smsnetwork.data.dao

import androidx.room.*
import com.monitor.smsnetwork.data.entity.NetworkEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkEventDao {
    @Query("SELECT * FROM network_events ORDER BY timestamp DESC LIMIT 100")
    fun getRecentEvents(): Flow<List<NetworkEvent>>

    @Query("SELECT * FROM network_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEventsSince(startTime: Long): Flow<List<NetworkEvent>>

    @Query("SELECT * FROM network_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEvent(): NetworkEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: NetworkEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<NetworkEvent>)

    @Delete
    suspend fun delete(event: NetworkEvent)

    @Query("DELETE FROM network_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT * FROM network_events WHERE eventType = 'connection_change' ORDER BY timestamp DESC LIMIT 20")
    fun getConnectionChanges(): Flow<List<NetworkEvent>>
}
