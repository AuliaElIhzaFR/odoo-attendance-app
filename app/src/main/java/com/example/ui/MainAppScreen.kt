package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OdooAudioPlayer
import com.example.data.local.AttendanceLog
import com.example.data.local.OdooConfig
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sin

// Theme Colors - Elegant Dark Palette
val MidnightBg = Color(0xFF1C1B1F) // Charcoal Background
val CardSlate = Color(0xFF2B2930) // Dark violet-gray Container
val BorderCyan = Color(0xFFD0BCFF) // Signature Lavender Accent (Replaces cyan)
val PulseYellow = Color(0xFF332D41) // Dark Purple element context
val ActiveGreen = Color(0xFF4ADE80) // Soft success green
val ErrorRed = Color(0xFFF2B8B5) // Soft Material Red
val LightText = Color(0xFFE6E1E5) // Ivory Light Text
val DimText = Color(0xFF938F99) // Secondary Dim Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val configState by viewModel.configFlow.collectAsStateWithLifecycle(initialValue = null)
    val logsState by viewModel.logsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val isWifiSimulated by viewModel.isSimulatedWifiConnected.collectAsStateWithLifecycle()
    val isLocationSimulated by viewModel.isSimulatedInsideOffice.collectAsStateWithLifecycle()
    val isOdooProcessing by viewModel.isOdooProcessing.collectAsStateWithLifecycle()
    val eventMsg by viewModel.attendanceEventMsg.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }

    // Toast/Snackbar notification for Odoo Response message
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(eventMsg) {
        eventMsg?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearEventMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBg),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .background(MidnightBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Odoo Connect",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightText,
                            letterSpacing = (-0.5).sp
                        )
                        val subtitleText = remember(configState) {
                            val config = configState
                            if (config != null) {
                                val cleanServer = config.serverUrl
                                    .replace("https://", "")
                                    .replace("http://", "")
                                    .split("/")
                                    .firstOrNull() ?: "odoo.company.com"
                                val cleanUser = config.username.split("@").firstOrNull() ?: "User"
                                "$cleanServer • $cleanUser"
                            } else {
                                "odoo.company.com • User"
                            }
                        }
                        Text(
                            text = subtitleText,
                            fontSize = 12.sp,
                            color = DimText
                        )
                    }
                    
                    // Profile/Initials Avatar representation from Elegant Dark theme design
                    val initials = remember(configState) {
                        val config = configState
                        if (config != null) {
                            val user = config.username
                            val cleanUser = user.split("@").firstOrNull() ?: "AS"
                            if (cleanUser.contains(".")) {
                                val parts = cleanUser.split(".")
                                val first = parts.getOrNull(0)?.firstOrNull() ?: 'U'
                                val second = parts.getOrNull(1)?.firstOrNull() ?: 'S'
                                "$first$second".uppercase()
                            } else if (cleanUser.length >= 2) {
                                cleanUser.take(2).uppercase()
                            } else {
                                cleanUser.uppercase()
                            }
                        } else {
                            "AS"
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BorderCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color(0xFF381E72),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(color = Color(0xFF49454F), thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = Color(0xFF49454F), thickness = 1.dp)
                NavigationBar(
                    containerColor = MidnightBg,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = "Main Presensi") },
                        label = { Text("Presensi") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE8DEF8),
                            selectedTextColor = Color(0xFFE8DEF8),
                            indicatorColor = Color(0xFF332D41),
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(imageVector = Icons.Filled.List, contentDescription = "Riwayat Bulanan") },
                        label = { Text("Riwayat") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE8DEF8),
                            selectedTextColor = Color(0xFFE8DEF8),
                            indicatorColor = Color(0xFF332D41),
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = "Pengaturan Akun & Area") },
                        label = { Text("Pengaturan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE8DEF8),
                            selectedTextColor = Color(0xFFE8DEF8),
                            indicatorColor = Color(0xFF332D41),
                            unselectedIconColor = Color(0xFFCAC4D0),
                            unselectedTextColor = Color(0xFFCAC4D0)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MidnightBg)
        ) {
            configState?.let { config ->
                when (selectedTab) {
                    0 -> AttendanceTab(
                        config = config,
                        logs = logsState,
                        isWifiSimulated = isWifiSimulated,
                        isLocationSimulated = isLocationSimulated,
                        isProcessing = isOdooProcessing,
                        onSimulateWifiToggle = { viewModel.setSimulatedWifiConnected(it) },
                        onSimulateLocationToggle = { viewModel.setSimulatedInsideOffice(it) },
                        onManualAttendance = { action -> viewModel.performManualAttendance(action) },
                        onTriggerReminder = { viewModel.triggerSimulatedForgotNotification() }
                    )
                    1 -> HistoryTab(
                        logs = logsState,
                        onClearLogs = { viewModel.clearHistory() }
                    )
                    2 -> SettingsTab(
                        config = config,
                        onSaveConfig = { viewModel.saveOdooConfig(it) }
                    )
                }
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BorderCyan)
            }
        }
    }
}

// ==================== TAB 1: PRESENSI ====================
@Composable
fun AttendanceTab(
    config: OdooConfig,
    logs: List<AttendanceLog>,
    isWifiSimulated: Boolean,
    isLocationSimulated: Boolean,
    isProcessing: Boolean,
    onSimulateWifiToggle: (Boolean) -> Unit,
    onSimulateLocationToggle: (Boolean) -> Unit,
    onManualAttendance: (String) -> Unit,
    onTriggerReminder: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Circular Orb Widget for core action matching Elegant Dark card shape to perfection
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSlate), // Elegant Card dark bg
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFF49454F))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Tap trigger element with nice ring shadow/outline from HTML spec
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (config.isCheckedIn) ActiveGreen.copy(alpha = 0.15f) else BorderCyan.copy(alpha = 0.15f)
                            )
                            .border(8.dp, if (config.isCheckedIn) ActiveGreen.copy(alpha = 0.1f) else BorderCyan.copy(alpha = 0.1f), CircleShape)
                            .clickable(enabled = !isProcessing) {
                                val action = if (config.isCheckedIn) "CHECK_OUT" else "CHECK_IN"
                                onManualAttendance(action)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(if (config.isCheckedIn) ActiveGreen else BorderCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = Color(0xFF381E72),
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (config.isCheckedIn) Icons.Filled.Check else Icons.Filled.PlayArrow,
                                    contentDescription = "Attendance Status Action",
                                    tint = Color(0xFF381E72),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (config.isCheckedIn) "Sudah Presensi Masuk" else "Ready to Check In",
                        color = LightText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val locMsg = if (isLocationSimulated) "Terdeteksi di Area Kantor" else "Di Luar Jangkauan Kantor"
                    Text(
                        text = "Lokasi: $locMsg",
                        color = if (isLocationSimulated) ActiveGreen else DimText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (config.isCheckedIn) "Sentuh tombol untuk Check-Out" else "Sentuh tombol untuk Check-In cepat",
                        color = DimText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 2-Column Grid of Automated Cards matching Design HTML
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // WiFi Card (Smart WiFi)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "WiFi Status",
                                tint = if (isWifiSimulated) BorderCyan else DimText,
                                modifier = Modifier.size(20.dp)
                            )
                            Switch(
                                checked = isWifiSimulated,
                                onCheckedChange = { onSimulateWifiToggle(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF381E72),
                                    checkedTrackColor = BorderCyan,
                                    uncheckedThumbColor = DimText,
                                    uncheckedTrackColor = Color(0xFF131215)
                                ),
                                modifier = Modifier
                                    .scale(0.85f)
                                    .testTag("simulate_wifi_switch")
                            )
                        }

                        Column {
                            Text(
                                text = "Smart WiFi",
                                color = LightText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val wifiLabel = if (isWifiSimulated) config.officeWifiSsid else "Nonaktif"
                            Text(
                                text = wifiLabel,
                                color = DimText,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Geofence Card (Auto Geofence)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF49454F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Geofence Status",
                                tint = if (isLocationSimulated) BorderCyan else DimText,
                                modifier = Modifier.size(20.dp)
                            )
                            Switch(
                                checked = isLocationSimulated,
                                onCheckedChange = { onSimulateLocationToggle(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF381E72),
                                    checkedTrackColor = BorderCyan,
                                    uncheckedThumbColor = DimText,
                                    uncheckedTrackColor = Color(0xFF131215)
                                ),
                                modifier = Modifier
                                    .scale(0.85f)
                                    .testTag("simulate_location_switch")
                            )
                        }

                        Column {
                            Text(
                                text = "Auto Geofence",
                                color = LightText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            val radiusLabel = if (config.isAutoGeofenceEnabled) "Radius ${config.officeRadiusMeters.toInt()}m" else "Nonaktif"
                            Text(
                                text = radiusLabel,
                                color = DimText,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Lockscreen Lupa Absensi Notification Trigger Widget
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTriggerReminder() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ErrorRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Fictional Reminder Bell Icon",
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Pengingat Lock Screen",
                            color = LightText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Kirim notitifikasi lupa presensi agar bisa langsung absen di lockscreen.",
                            color = DimText,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Menu Chevron Right Decorator",
                        tint = DimText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Last activity log preview layout
        val latestLog = logs.firstOrNull()
        if (latestLog != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Aktivitas Terakhir:",
                        color = DimText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val dateStr = SimpleDateFormat("HH:mm - d MMM", Locale.getDefault()).format(Date(latestLog.timestamp))
                    Text(
                        text = "${latestLog.actionType} via ${latestLog.method} ($dateStr)",
                        color = if (latestLog.isSuccess) ActiveGreen else ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==================== TAB 2: RIWAYAT ====================
@Composable
fun HistoryTab(
    logs: List<AttendanceLog>,
    onClearLogs: () -> Unit
) {
    var filterType by remember { mutableStateOf("ALL") } // "ALL", "CHECK_IN", "CHECK_OUT"

    val filteredLogs = remember(logs, filterType) {
        when (filterType) {
            "CHECK_IN" -> logs.filter { it.actionType == "CHECK_IN" }
            "CHECK_OUT" -> logs.filter { it.actionType == "CHECK_OUT" }
            else -> logs
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOG PRESENSI BULANAN",
                fontSize = 14.sp,
                color = LightText,
                fontWeight = FontWeight.Bold
            )
            // Clear History Button
            Button(
                onClick = onClearLogs,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus Riwayat Presensi",
                    tint = ErrorRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filters UI styled exactly like the mock HTML template
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "Semua", "CHECK_IN" to "Masuk", "CHECK_OUT" to "Keluar").forEach { (type, label) ->
                val isSelected = filterType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF332D41) else CardSlate)
                        .border(
                            1.dp,
                            if (isSelected) BorderCyan else Color(0xFF49454F),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { filterType = type }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFFE8DEF8) else LightText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Metric Summary represent '92% Attendance' or totals from HTML mockup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardSlate)
                .border(1.dp, Color(0xFF49454F), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(BorderCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Logs Count Status",
                        tint = BorderCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Konfirmasi Kehadiran",
                    color = LightText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "${logs.size} Terdaftar",
                color = BorderCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Empty Records Info List",
                        tint = DimText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Riwayat Presensi",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coba lakukan check-in pertama di halaman depan.",
                        color = DimText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredLogs) { log ->
                    AttendanceHistoryItem(log)
                }
            }
        }
    }
}

@Composable
fun AttendanceHistoryItem(log: AttendanceLog) {
    val dateStr = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date(log.timestamp))
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (log.isSuccess) Color(0xFF49454F) else ErrorRed.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sleek green/orange active indicator dot from HTML design mockup
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (log.isSuccess) {
                                if (log.actionType == "CHECK_IN") ActiveGreen else BorderCyan
                            } else {
                                ErrorRed
                            }
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (log.actionType == "CHECK_IN") "Masuk (${timeStr})" else "Keluar (${timeStr})",
                        color = LightText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        color = DimText,
                        fontSize = 11.sp
                    )
                }
            }

            // Beautiful method capsule badge styled like secondaryContainer of the Elegant Dark HTML
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF332D41))
                    .border(0.5.dp, BorderCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = log.method,
                    color = Color(0xFFE8DEF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== TAB 3: PENGATURAN ====================
@Composable
fun SettingsTab(
    config: OdooConfig,
    onSaveConfig: (OdooConfig) -> Unit
) {
    val context = LocalContext.current

    // Forms Setup Strings
    var serverUrl by remember { mutableStateOf(config.serverUrl) }
    var dbName by remember { mutableStateOf(config.dbName) }
    var username by remember { mutableStateOf(config.username) }
    var password by remember { mutableStateOf(config.password) }
    var employeeIdStr by remember { mutableStateOf(config.employeeId.toString()) }

    var isAutoGeofence by remember { mutableStateOf(config.isAutoGeofenceEnabled) }
    var officeWifiSsid by remember { mutableStateOf(config.officeWifiSsid) }
    var isAutoWifi by remember { mutableStateOf(config.isAutoWifiEnabled) }

    var soundName by remember { mutableStateOf(config.customSoundName) }
    var simulateOdoo by remember { mutableStateOf(config.simulateOdoo) }

    // Map Picker Coordinates
    var officeLat by remember { mutableStateOf(config.officeLatitude) }
    var officeLng by remember { mutableStateOf(config.officeLongitude) }
    var officeRadius by remember { mutableStateOf(config.officeRadiusMeters) }

    var passwordVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "PENGATURAN ASISTEN",
                fontSize = 13.sp,
                color = BorderCyan,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Konfigurasi Odoo & Area",
                fontSize = 20.sp,
                color = LightText,
                fontWeight = FontWeight.Bold
            )
        }

        // Section 1: Server Credentials
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Kredensial Server Odoo",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Server URL Odoo") },
                        placeholder = { Text("e.g. https://yourcompany.odoo.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dbName,
                        onValueChange = { dbName = it },
                        label = { Text("Database Odoo") },
                        placeholder = { Text("e.g. odoo_db") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username / Email") },
                        placeholder = { Text("e.g. name@company.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Toggle password visibility",
                                    tint = DimText
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = employeeIdStr,
                        onValueChange = { employeeIdStr = it },
                        label = { Text("ID Karyawan (Odoo Employee ID)") },
                        placeholder = { Text("e.g. 1" ) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Toggle Simulation Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Simulasi Odoo",
                                color = LightText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gunakan respons demo untuk mempermudah tes.",
                                color = DimText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = simulateOdoo,
                            onCheckedChange = { simulateOdoo = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightBg,
                                checkedTrackColor = BorderCyan,
                                uncheckedThumbColor = DimText,
                                uncheckedTrackColor = Color(0xFF0F172A)
                            )
                        )
                    }
                }
            }
        }

        // Section 2: WiFi Automation Setup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Otomatis WiFi Kantor",
                                color = LightText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Auto check-in bilamana terhubung ke WiFi",
                                color = DimText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isAutoWifi,
                            onCheckedChange = { isAutoWifi = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightBg,
                                checkedTrackColor = BorderCyan,
                                uncheckedThumbColor = DimText,
                                uncheckedTrackColor = Color(0xFF0F172A)
                            )
                        )
                    }

                    OutlinedTextField(
                        value = officeWifiSsid,
                        onValueChange = { officeWifiSsid = it },
                        label = { Text("SSID WiFi Kantor") },
                        placeholder = { Text("WiFi Kantor Utama") },
                        enabled = isAutoWifi,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BorderCyan,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = BorderCyan,
                            unfocusedLabelColor = DimText,
                            focusedTextColor = LightText,
                            unfocusedTextColor = LightText,
                            disabledContainerColor = Color.Transparent,
                            disabledBorderColor = Color(0xFF334155),
                            disabledTextColor = DimText,
                            disabledLabelColor = DimText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 3: Geofence Locator with Interactive Canvas Map
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Otomatis Geofence Kantor",
                                color = LightText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Presensi otomatis saat memasuki radius area",
                                color = DimText,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isAutoGeofence,
                            onCheckedChange = { isAutoGeofence = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightBg,
                                checkedTrackColor = BorderCyan,
                                uncheckedThumbColor = DimText,
                                uncheckedTrackColor = Color(0xFF0F172A)
                            )
                        )
                    }

                    if (isAutoGeofence) {
                        Text(
                            text = "Atur Titik Kantor pada Peta Radar (Ketuk untuk Set):",
                            color = LightText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Beautiful grid canvas representing interactive Map Coordinate Picker
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MidnightBg)
                                .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { offset ->
                                            val normalizedX = (offset.x / size.width) - 0.5
                                            val normalizedY = (offset.y / size.height) - 0.5
                                            officeLat = -6.2000 + (normalizedY * 0.01)
                                            officeLng = 106.8166 + (normalizedX * 0.01)
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw Radar Sweep gridlines
                                val strokeColor = Color(0xFF334155)
                                drawLine(strokeColor, Offset(0f, h/2), Offset(w, h/2), strokeWidth = 1f)
                                drawLine(strokeColor, Offset(w/2, 0f), Offset(w/2, h), strokeWidth = 1f)
                                drawCircle(Color(0xFF1E293B), w/4f, Offset(w/2f, h/2f), style = Stroke(width = 1f))
                                drawCircle(Color(0xFF1E293B), w/2f, Offset(w/2f, h/2f), style = Stroke(width = 1f))

                                // Jakarta Office Marker Location Translate back
                                val normX = ((officeLng - 106.8166) / 0.01) + 0.5
                                val normY = ((officeLat - (-6.2000)) / 0.01) + 0.5

                                val markerX = (normX * w).toFloat().coerceIn(0f, w)
                                val markerY = (normY * h).toFloat().coerceIn(0f, h)

                                // Draw office radius bubble representation
                                drawCircle(
                                    color = BorderCyan.copy(alpha = 0.2f),
                                    radius = (officeRadius.toFloat() * 1.5f).coerceIn(10f, 100f),
                                    center = Offset(markerX, markerY)
                                )
                                // Pulsing core center of radar coordinate
                                drawCircle(
                                    color = BorderCyan,
                                    radius = 8.dp.toPx(),
                                    center = Offset(markerX, markerY)
                                )
                            }

                            // Dynamic location badge label
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CardSlate.copy(alpha = 0.8f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format("Lat: %.5f, Lng: %.5f", officeLat, officeLng),
                                    color = BorderCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Radius configuration slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Radius: ${officeRadius.toInt()}m",
                                color = LightText,
                                fontSize = 12.sp,
                                modifier = Modifier.width(90.dp)
                            )
                            Slider(
                                value = officeRadius.toFloat(),
                                onValueChange = { officeRadius = it.toDouble() },
                                valueRange = 30f..500f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BorderCyan,
                                    activeTrackColor = BorderCyan,
                                    inactiveTrackColor = Color(0xFF0F172A)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Sound Notifications Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Suara Notifikasi Kustom",
                        color = LightText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Suara khusus akan diputar otomatis begitu proses presensi sukses tersinkronisasi.",
                        color = DimText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Chirp", "Bell", "Piano", "None").forEach { sName ->
                            val isChosen = soundName.equals(sName, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) BorderCyan else MidnightBg)
                                    .border(
                                        1.dp,
                                        if (isChosen) BorderCyan else Color(0xFF475569),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { soundName = sName }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sName,
                                    color = if (isChosen) MidnightBg else LightText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Sound Preview Playback Button
                    Button(
                        onClick = { OdooAudioPlayer.play(context, soundName) },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderCyan.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Preview Sound Playback",
                            tint = BorderCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Uji Coba Putar Suara: $soundName",
                            color = BorderCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Section 5: Save Settings Trigger
        item {
            Button(
                onClick = {
                    val finalConfig = config.copy(
                        serverUrl = serverUrl,
                        dbName = dbName,
                        username = username,
                        password = password,
                        employeeId = employeeIdStr.toIntOrNull() ?: 0,
                        officeLatitude = officeLat,
                        officeLongitude = officeLng,
                        officeRadiusMeters = officeRadius,
                        isAutoGeofenceEnabled = isAutoGeofence,
                        officeWifiSsid = officeWifiSsid,
                        isAutoWifiEnabled = isAutoWifi,
                        customSoundName = soundName,
                        simulateOdoo = simulateOdoo
                    )
                    onSaveConfig(finalConfig)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BorderCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_config_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Simpan Setelan",
                    tint = MidnightBg
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIMPAN KONFIGURASI",
                    color = MidnightBg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
