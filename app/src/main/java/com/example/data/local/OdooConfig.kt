package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "odoo_config")
data class OdooConfig(
    @PrimaryKey val id: Int = 1,
    val serverUrl: String = "https://demo.odoo.com",
    val dbName: String = "demo_db",
    val username: String = "user@example.com",
    val password: String = "password123",
    val employeeId: Int = 0,
    val officeLatitude: Double = -6.2000, // Defaut Jakarta
    val officeLongitude: Double = 106.8166,
    val officeRadiusMeters: Double = 100.0,
    val isAutoGeofenceEnabled: Boolean = false,
    val officeWifiSsid: String = "WiFi Kantor",
    val isAutoWifiEnabled: Boolean = false,
    val customSoundName: String = "Bell", // "Chirp", "Bell", "Piano", "None"
    val isCheckedIn: Boolean = false, // Local attendance status
    val simulateOdoo: Boolean = true // Simulation default for testing in sandbox
)
