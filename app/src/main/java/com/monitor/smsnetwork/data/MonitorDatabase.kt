package com.monitor.smsnetwork.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.monitor.smsnetwork.data.dao.*
import com.monitor.smsnetwork.data.entity.*

@Database(
    entities = [
        SmsEvent::class,
        NetworkEvent::class,
        TrafficEvent::class,
        DiagnosticEvent::class,
        AppTrafficStats::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MonitorDatabase : RoomDatabase() {
    abstract fun smsEventDao(): SmsEventDao
    abstract fun networkEventDao(): NetworkEventDao
    abstract fun trafficEventDao(): TrafficEventDao
    abstract fun diagnosticEventDao(): DiagnosticEventDao
    abstract fun appTrafficStatsDao(): AppTrafficStatsDao

    companion object {
        @Volatile
        private var INSTANCE: MonitorDatabase? = null

        fun getDatabase(context: Context): MonitorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MonitorDatabase::class.java,
                    "monitor_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
