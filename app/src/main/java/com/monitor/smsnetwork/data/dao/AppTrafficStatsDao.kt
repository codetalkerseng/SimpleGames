package com.monitor.smsnetwork.data.dao

import androidx.room.*
import com.monitor.smsnetwork.data.entity.AppTrafficStats
import kotlinx.coroutines.flow.Flow

@Dao
interface AppTrafficStatsDao {
    @Query("SELECT * FROM app_traffic_stats ORDER BY totalBytesIn + totalBytesOut DESC")
    fun getAllStats(): Flow<List<AppTrafficStats>>

    @Query("SELECT * FROM app_traffic_stats ORDER BY totalBytesIn + totalBytesOut DESC LIMIT 10")
    fun getTopApps(): Flow<List<AppTrafficStats>>

    @Query("SELECT * FROM app_traffic_stats WHERE suspiciousActivityCount > 0 ORDER BY suspiciousActivityCount DESC")
    fun getSuspiciousApps(): Flow<List<AppTrafficStats>>

    @Query("SELECT * FROM app_traffic_stats WHERE appPackage = :packageName")
    suspend fun getStatsForApp(packageName: String): AppTrafficStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: AppTrafficStats)

    @Update
    suspend fun update(stats: AppTrafficStats)

    @Delete
    suspend fun delete(stats: AppTrafficStats)

    @Query("DELETE FROM app_traffic_stats")
    suspend fun deleteAll()
}
