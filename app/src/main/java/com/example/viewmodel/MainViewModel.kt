package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AttendanceRepository
import com.example.data.OdooNotificationReceiver
import com.example.data.local.AttendanceLog
import com.example.data.local.OdooConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AttendanceRepository(application)

    val configFlow = repository.configFlow
    val logsFlow = repository.allLogsFlow

    // Simulated states for easy demoing inside the AI Studio web streaming emulator
    private val _isSimulatedWifiConnected = MutableStateFlow(false)
    val isSimulatedWifiConnected = _isSimulatedWifiConnected.asStateFlow()

    private val _isSimulatedInsideOffice = MutableStateFlow(false)
    val isSimulatedInsideOffice = _isSimulatedInsideOffice.asStateFlow()

    private val _isOdooProcessing = MutableStateFlow(false)
    val isOdooProcessing = _isOdooProcessing.asStateFlow()

    private val _attendanceEventMsg = MutableStateFlow<String?>(null)
    val attendanceEventMsg = _attendanceEventMsg.asStateFlow()

    init {
        // Ensure default configurations are populated in Room
        viewModelScope.launch {
            repository.getConfig()
        }
    }

    fun saveOdooConfig(config: OdooConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun clearEventMessage() {
        _attendanceEventMsg.value = null
    }

    /**
     * Triggers simulated "Lupa Absensi" Reminder lockscreen notification
     */
    fun triggerSimulatedForgotNotification() {
        viewModelScope.launch {
            val config = repository.getConfig()
            val reason = if (config.isCheckedIn) {
                "Anda masih tercatat CHECK-IN di kantor! Klik di bawah untuk langsung CHECK-OUT."
            } else {
                "Sudah jam sibuk masuk kantor tapi belum CHECK-IN! Klik di bawah untuk CHECK-IN."
            }
            OdooNotificationReceiver.showReminderNotification(getApplication(), reason)
        }
    }

    /**
     * Manual clock action trigger
     */
    fun performManualAttendance(actionType: String) {
        viewModelScope.launch {
            _isOdooProcessing.value = true
            repository.performOdooAttendance(
                actionType = actionType,
                method = "MANUAL",
                onSuccess = { msg ->
                    _attendanceEventMsg.value = msg
                    _isOdooProcessing.value = false
                },
                onFailure = { err ->
                    _attendanceEventMsg.value = "Gagal: $err"
                    _isOdooProcessing.value = false
                }
            )
        }
    }

    /**
     * Toggles simulated Wi-Fi connection state and triggers automatic clock-in/out
     * if the configuration has isAutoWifiEnabled checked.
     */
    fun setSimulatedWifiConnected(connected: Boolean) {
        viewModelScope.launch {
            _isSimulatedWifiConnected.value = connected
            val config = repository.getConfig()
            if (config.isAutoWifiEnabled) {
                if (connected && !config.isCheckedIn) {
                    // Auto clock in!
                    _isOdooProcessing.value = true
                    repository.performOdooAttendance(
                        actionType = "CHECK_IN",
                        method = "WIFI",
                        onSuccess = { msg ->
                            _attendanceEventMsg.value = "Auto WiFi Check-In: $msg"
                            _isOdooProcessing.value = false
                        },
                        onFailure = { err ->
                            _attendanceEventMsg.value = "WiFi Auto Check-In Gagal: $err"
                            _isOdooProcessing.value = false
                        }
                    )
                } else if (!connected && config.isCheckedIn) {
                    // Auto clock out!
                    _isOdooProcessing.value = true
                    repository.performOdooAttendance(
                        actionType = "CHECK_OUT",
                        method = "WIFI",
                        onSuccess = { msg ->
                            _attendanceEventMsg.value = "Auto WiFi Check-Out: $msg"
                            _isOdooProcessing.value = false
                        },
                        onFailure = { err ->
                            _attendanceEventMsg.value = "WiFi Auto Check-Out Gagal: $err"
                            _isOdooProcessing.value = false
                        }
                    )
                }
            }
        }
    }

    /**
     * Toggles simulated Geofencing location state and triggers automatic clock-in/out
     * if the configuration has isAutoGeofenceEnabled checked.
     */
    fun setSimulatedInsideOffice(inside: Boolean) {
        viewModelScope.launch {
            _isSimulatedInsideOffice.value = inside
            val config = repository.getConfig()
            if (config.isAutoGeofenceEnabled) {
                if (inside && !config.isCheckedIn) {
                    // Auto clock in!
                    _isOdooProcessing.value = true
                    repository.performOdooAttendance(
                        actionType = "CHECK_IN",
                        method = "GEOFENCE",
                        onSuccess = { msg ->
                            _attendanceEventMsg.value = "Auto Geofence Check-In: $msg"
                            _isOdooProcessing.value = false
                        },
                        onFailure = { err ->
                            _attendanceEventMsg.value = "Geofence Auto Check-In Gagal: $err"
                            _isOdooProcessing.value = false
                        }
                    )
                } else if (!inside && config.isCheckedIn) {
                    // Auto clock out!
                    _isOdooProcessing.value = true
                    repository.performOdooAttendance(
                        actionType = "CHECK_OUT",
                        method = "GEOFENCE",
                        onSuccess = { msg ->
                            _attendanceEventMsg.value = "Auto Geofence Check-Out: $msg"
                            _isOdooProcessing.value = false
                        },
                        onFailure = { err ->
                            _attendanceEventMsg.value = "Geofence Auto Check-Out Gagal: $err"
                            _isOdooProcessing.value = false
                        }
                    )
                }
            }
        }
    }
}
