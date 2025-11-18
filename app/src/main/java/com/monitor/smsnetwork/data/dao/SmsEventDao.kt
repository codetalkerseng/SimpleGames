package com.monitor.smsnetwork.data.dao

import androidx.room.*
import com.monitor.smsnetwork.data.entity.SmsEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsEventDao {
    @Query("SELECT * FROM sms_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SmsEvent>>

    @Query("SELECT * FROM sms_events WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEventsSince(startTime: Long): Flow<List<SmsEvent>>

    @Query("SELECT * FROM sms_events WHERE status = 'failed' ORDER BY timestamp DESC")
    fun getFailedEvents(): Flow<List<SmsEvent>>

    @Query("SELECT * FROM sms_events WHERE isWifiCalling = 1 AND status = 'failed' ORDER BY timestamp DESC")
    fun getWifiCallingFailures(): Flow<List<SmsEvent>>

    @Query("SELECT * FROM sms_events WHERE id = :id")
    suspend fun getEventById(id: Long): SmsEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SmsEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SmsEvent>)

    @Delete
    suspend fun delete(event: SmsEvent)

    @Query("DELETE FROM sms_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM sms_events WHERE status = 'failed' AND timestamp >= :since")
    fun getFailureCount(since: Long): Flow<Int>
}
