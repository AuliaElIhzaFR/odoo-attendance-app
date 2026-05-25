package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM odoo_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<OdooConfig?>

    @Query("SELECT * FROM odoo_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): OdooConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: OdooConfig)

    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<AttendanceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AttendanceLog)

    @Query("DELETE FROM attendance_logs")
    suspend fun clearLogs()
}
