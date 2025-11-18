package com.monitor.smsnetwork.data.dao

import androidx.room.*
import com.monitor.smsnetwork.data.entity.DiagnosticEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticEventDao {
    @Query("SELECT * FROM diagnostic_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<DiagnosticEvent>>

    @Query("SELECT * FROM diagnostic_events WHERE isResolved = 0 ORDER BY timestamp DESC")
    fun getUnresolvedEvents(): Flow<List<DiagnosticEvent>>

    @Query("SELECT * FROM diagnostic_events WHERE category = :category ORDER BY timestamp DESC")
    fun getEventsByCategory(category: String): Flow<List<DiagnosticEvent>>

    @Query("SELECT * FROM diagnostic_events WHERE severity = 'critical' OR severity = 'error' ORDER BY timestamp DESC")
    fun getCriticalEvents(): Flow<List<DiagnosticEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: DiagnosticEvent): Long

    @Update
    suspend fun update(event: DiagnosticEvent)

    @Delete
    suspend fun delete(event: DiagnosticEvent)

    @Query("UPDATE diagnostic_events SET isResolved = 1 WHERE id = :id")
    suspend fun markAsResolved(id: Long)

    @Query("DELETE FROM diagnostic_events WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
