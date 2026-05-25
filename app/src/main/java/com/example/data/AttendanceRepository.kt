package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceLog
import com.example.data.local.OdooConfig
import com.example.data.network.OdooClient
import com.example.data.network.OdooResult
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.attendanceDao()
    private val odooClient = OdooClient()

    val configFlow: Flow<OdooConfig?> = dao.getConfigFlow()
    val allLogsFlow: Flow<List<AttendanceLog>> = dao.getAllLogsFlow()

    suspend fun getConfig(): OdooConfig {
        return dao.getConfig() ?: OdooConfig().also {
            dao.saveConfig(it)
        }
    }

    suspend fun saveConfig(config: OdooConfig) {
        dao.saveConfig(config)
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    /**
     * Executes check-in or check-out, updates local configuration status, logs
     * the transaction history, and plays a synthesized sound feedback.
     */
    suspend fun performOdooAttendance(
        actionType: String,
        method: String,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ): Boolean {
        val currentConfig = getConfig()
        
        val result = odooClient.executeAttendance(currentConfig, actionType)
        
        return when (result) {
            is OdooResult.Success -> {
                val isCheckIn = actionType == "CHECK_IN"
                saveConfig(currentConfig.copy(isCheckedIn = isCheckIn))

                val successLog = AttendanceLog(
                    actionType = actionType,
                    method = method,
                    isSuccess = true,
                    statusMessage = result.message,
                    syncedWithOdoo = !currentConfig.simulateOdoo
                )
                dao.insertLog(successLog)

                // Try playing the custom synthesized sound
                try {
                    OdooAudioPlayer.play(context, currentConfig.customSoundName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Send a broadcast or trigger widget update
                OdooWidgetProvider.updateWidget(context, isCheckIn)

                onSuccess(result.message)
                true
            }
            is OdooResult.Error -> {
                val failureLog = AttendanceLog(
                    actionType = actionType,
                    method = method,
                    isSuccess = false,
                    statusMessage = result.errorMsg,
                    syncedWithOdoo = false
                )
                dao.insertLog(failureLog)
                onFailure(result.errorMsg)
                false
            }
        }
    }
}
