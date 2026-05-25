package com.example.data.network

import android.util.Log
import com.example.data.local.OdooConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class OdooResult {
    data class Success(val message: String, val name: String) : OdooResult()
    data class Error(val errorMsg: String) : OdooResult()
}

class OdooClient {
    private val TAG = "OdooClient"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    
    private val cookieJar = InMemoryCookieJar()
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
     * Executes the Odoo check-in/out sequence:
     * 1. Authenticate & obtain session
     * 2. Call hr.employee attendance action or hr.attendance direct log
     */
    suspend fun executeAttendance(config: OdooConfig, action: String): OdooResult = withContext(Dispatchers.IO) {
        if (config.simulateOdoo) {
            // Emulate Odoo performance with a short delay
            kotlinx.coroutines.delay(1000)
            val formatAction = if (action == "CHECK_IN") "Check-In" else "Check-Out"
            return@withContext OdooResult.Success(
                message = "Berhasil $formatAction otomatis (Simulasi Odoo)",
                name = if (config.username.isNotBlank()) config.username.substringBefore("@") else "User Demo"
            )
        }

        val baseUrl = config.serverUrl.trim().removeSuffix("/")
        
        // 1. Authenticate
        val authUrl = "$baseUrl/web/session/authenticate"
        val authPayload = """
            {
                "jsonrpc": "2.0",
                "method": "call",
                "params": {
                    "db": "${config.dbName}",
                    "login": "${config.username}",
                    "password": "${config.password}"
                }
            }
        """.trimIndent()

        val authRequest = Request.Builder()
            .url(authUrl)
            .post(authPayload.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        try {
            val authResponse = client.newCall(authRequest).execute()
            if (!authResponse.isSuccessful) {
                return@withContext OdooResult.Error("Gagal otentikasi server Odoo (Code: ${authResponse.code})")
            }

            val authBody = authResponse.body?.string() ?: ""
            Log.d(TAG, "Auth response: $authBody")
            
            if (authBody.contains("\"error\"") || !authBody.contains("\"result\"")) {
                return@withContext OdooResult.Error("Koneksi gagal: Username/Password salah atau DB salah.")
            }

            // Extract User ID / Employee details
            // We'll perform a generic attendance check-in or out
            // Odoo uses `/web/dataset/call_kw` to perform attendance operations
            // We use method config.employeeId or query employee if not specified,
            // or perform standard check_in check_out toggle.
            // In Odoo, Employee Attendances can be modified via model `hr.employee` method `attendance_manual`
            // API Schema: model="hr.employee", method="attendance_manual", args=[[employee_id], "hr_attendance.attendance_action_change"]
            
            val employeeId = if (config.employeeId > 0) config.employeeId else {
                // If not defined, try to extract from result or default to 1.
                // In actual production Odoo, the user needs their hr.employee ID
                1
            }

            val attendanceUrl = "$baseUrl/web/dataset/call_kw"
            val attendanceAction = if (action == "CHECK_IN") "sign_in" else "sign_out"
            
            // Format for hr.employee.attendance_manual (Odoo v12+ standard attendance module format)
            val attendancePayload = """
                {
                    "jsonrpc": "2.0",
                    "method": "call",
                    "params": {
                        "model": "hr.employee",
                        "method": "attendance_manual",
                        "args": [[$employeeId], "hr_attendance.attendance_action_change"],
                        "kwargs": {}
                    }
                }
            """.trimIndent()

            val attendanceRequest = Request.Builder()
                .url(attendanceUrl)
                .post(attendancePayload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val attResponse = client.newCall(attendanceRequest).execute()
            if (!attResponse.isSuccessful) {
                return@withContext OdooResult.Error("Gagal mengirim presensi (HTTP Code: ${attResponse.code})")
            }

            val attBody = attResponse.body?.string() ?: ""
            Log.d(TAG, "Attendance action response: $attBody")

            if (attBody.contains("\"error\"")) {
                // Let's try alternative direct hr.attendance insert or tell them it failed
                return@withContext OdooResult.Error("Odoo DB Error: Pastikan modul 'HR Attendance' terpasang dan ID Karyawan ($employeeId) valid.")
            }

            val label = if (action == "CHECK_IN") "Check-In" else "Check-Out"
            return@withContext OdooResult.Success(
                message = "Berhasil melakukan $label ke Odoo!",
                name = config.username.substringBefore("@")
            )

        } catch (e: Exception) {
            Log.e(TAG, "Odoo API exception", e)
            return@withContext OdooResult.Error("Kesalahan jaringan: ${e.localizedMessage}")
        }
    }
}

class InMemoryCookieJar : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }
}
