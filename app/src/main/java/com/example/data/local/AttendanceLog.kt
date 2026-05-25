package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String, // "CHECK_IN" or "CHECK_OUT"
    val method: String, // "MANUAL", "WIDGET", "LOCK_SCREEN", "GEOFENCE", "WIFI"
    val isSuccess: Boolean,
    val statusMessage: String,
    val syncedWithOdoo: Boolean
)
