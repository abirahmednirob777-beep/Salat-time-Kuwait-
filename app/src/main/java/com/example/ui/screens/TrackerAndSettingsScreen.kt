package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KuwaitGovernorate
import com.example.model.PrayerType
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.theme.*

@Composable
fun TrackerAndSettingsScreen(
    viewModel: KuwaitSalatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prayerLog by viewModel.prayerLog.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val is12Hour by viewModel.is12HourFormat.collectAsState()
    val selectedGov by viewModel.selectedGovernorate.collectAsState()
    val hijriOffset by viewModel.hijriOffset.collectAsState()
    val audioMode by viewModel.audioMode.collectAsState()
    val isAutoLocation by viewModel.isAutoLocationEnabled.collectAsState()

    var showGovDialog by remember { mutableStateOf(false) }
    var showAwqafInfoDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            viewModel.detectAndApplyLocation(context) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                if (isArabic) "تم رفض إذن الموقع" else "Location permission denied",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestLocationDetection = {
        if (viewModel.hasLocationPermission(context)) {
            viewModel.detectAndApplyLocation(context) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val fardPrayers = listOf(
        PrayerType.FAJR,
        PrayerType.DHUHR,
        PrayerType.ASR,
        PrayerType.MAGHRIB,
        PrayerType.ISHA
    )

    val completedCount = fardPrayers.count { prayerLog[it] == true }
    val progress = (completedCount / 5f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "prayerProgress")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("tracker_settings_screen")
    ) {
        // Section: Daily Prayer Tracker
        Text(
            text = if (isArabic) "متابعة الصلوات اليومية" else "Daily Prayer Tracker",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "سجل صلوات اليوم" else "Today's Fard Prayers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isArabic) "$completedCount من 5 صلوات تم أداؤها" else "$completedCount of 5 prayers completed",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (completedCount == 5) KuwaitEmeraldPrimary else KuwaitGoldAccent
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = KuwaitEmeraldPrimary,
                    trackColor = Color(0x33888888)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5 prayer checkboxes row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    fardPrayers.forEach { prayer ->
                        val isDone = prayerLog[prayer] == true
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { viewModel.togglePrayerCompleted(prayer) }
                                .padding(4.dp)
                        ) {
                            FilledIconToggleButton(
                                checked = isDone,
                                onCheckedChange = { viewModel.togglePrayerCompleted(prayer) },
                                colors = IconButtonDefaults.filledIconToggleButtonColors(
                                    checkedContainerColor = KuwaitEmeraldPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Icon(
                                    if (isDone) Icons.Default.Check else Icons.Default.Circle,
                                    contentDescription = prayer.englishName,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) prayer.arabicName else prayer.englishName,
                                fontSize = 11.sp,
                                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Kuwait Settings
        Text(
            text = if (isArabic) "الإعدادات والمواقيت" else "Settings & Preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Governorate setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGovDialog = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = KuwaitGoldAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isArabic) "المحافظة" else "Kuwait Governorate", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (isArabic) selectedGov.arabicName else selectedGov.englishName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // GPS Auto-detect Governorate toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = KuwaitEmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isArabic) "تحديد المحافظة تلقائياً (GPS)" else "Auto-Detect Governorate (GPS)",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (isAutoLocation) {
                                    if (isArabic) "مفعل (تم تحديد أقرب محافظة)" else "Enabled (matches nearest governorate)"
                                } else {
                                    if (isArabic) "معطل (اختيار يدوي)" else "Disabled (Manual selection)"
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isAutoLocation,
                        onCheckedChange = { checked ->
                            if (checked) {
                                requestLocationDetection()
                            } else {
                                viewModel.setAutoLocationEnabled(false)
                            }
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Time Format Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isArabic) "تنسيق الوقت" else "Time Format", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (is12Hour) "12-Hour (AM / PM)" else "24-Hour (14:30)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = is12Hour,
                        onCheckedChange = { viewModel.toggle12HourFormat() }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Language Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = KuwaitEmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isArabic) "اللغة" else "Language", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (isArabic) "العربية (Arabic)" else "English",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(if (isArabic) "English" else "العربية", fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Audio mode selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = KuwaitGoldAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isArabic) "تنبيه الأذان" else "Adhan Alert Mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                audioMode,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        listOf("ADHAN", "CHIME", "VIBRATE").forEach { mode ->
                            val isSel = audioMode == mode
                            TextButton(
                                onClick = {
                                    viewModel.setAudioMode(mode)
                                    testAudioFeedback(context, mode)
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = if (isSel) KuwaitEmeraldPrimary else Color.Gray
                                )
                            ) {
                                Text(mode, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 11.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                // Hijri date offset adjustment (-2 to +2 days)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(if (isArabic) "تعديل التاريخ الهجري" else "Hijri Date Offset", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (hijriOffset == 0) (if (isArabic) "تلقائي (أم القرى)" else "Standard")
                                else "${if (hijriOffset > 0) "+$hijriOffset" else "$hijriOffset"} ${if (isArabic) "يوم" else "day(s)"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (hijriOffset > -2) viewModel.setHijriOffset(hijriOffset - 1) },
                            enabled = hijriOffset > -2
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text("$hijriOffset", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                        IconButton(
                            onClick = { if (hijriOffset < 2) viewModel.setHijriOffset(hijriOffset + 1) },
                            enabled = hijriOffset < 2
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kuwait Awqaf Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAwqafInfoDialog = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = KuwaitEmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isArabic) "معايير وزارة الأوقاف الكويتية" else "Kuwait Ministry of Awqaf Standards",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isArabic) "معلومات الحساب والمسجد الكبير" else "Calculation standards & Grand Mosque info",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Governorate selector dialog
    if (showGovDialog) {
        AlertDialog(
            onDismissRequest = { showGovDialog = false },
            title = { Text(if (isArabic) "اختر المحافظة" else "Select Governorate") },
            text = {
                Column {
                    KuwaitGovernorate.values().forEach { gov ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectGovernorate(gov)
                                    viewModel.setAutoLocationEnabled(false)
                                    showGovDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gov == selectedGov,
                                onClick = {
                                    viewModel.selectGovernorate(gov)
                                    viewModel.setAutoLocationEnabled(false)
                                    showGovDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) gov.arabicName else gov.englishName,
                                fontWeight = if (gov == selectedGov) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGovDialog = false }) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            }
        )
    }

    // Awqaf Standards Info Dialog
    if (showAwqafInfoDialog) {
        AlertDialog(
            onDismissRequest = { showAwqafInfoDialog = false },
            title = {
                Text(
                    if (isArabic) "دليل مواقيت الصلاة - دولة الكويت"
                    else "Kuwait Prayer Times Guide"
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic)
                            "• يتم حساب المواقيت وفقاً لمعايير وزارة الأوقاف والشؤون الإسلامية بدولة الكويت.\n• زاوية الفجر: 18.0 درجة.\n• زاوية العشاء: 17.5 درجة.\n• المذهب المعتمد لصلاة العصر: الشافعي/الحنبلي (ظل المثل الواحد).\n• التوقيت المعتمد: توقيت شبه الجزيرة العربية (غرينتش +3 بدون توقيت صيفي).\n• جامع الدولة الكبير (المسجد الكبير) يقع في العاصمة ويتسع لأكثر من 10,000 مصلٍ."
                        else
                            "• Times are calculated according to the State of Kuwait Ministry of Awqaf & Islamic Affairs standards.\n• Fajr Angle: 18.0°.\n• Isha Angle: 17.5°.\n• Asr Juristic: Standard Shafi'i/Hanbali (1x shadow length).\n• Timezone: UTC+3 (Arabian Standard Time, no DST).\n• The Grand Mosque of Kuwait (Al-Masjid Al-Kabir) in Kuwait City accommodates over 10,000 worshippers.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAwqafInfoDialog = false }) {
                    Text(if (isArabic) "حسناً" else "Understood")
                }
            }
        )
    }
}

private fun testAudioFeedback(context: Context, mode: String) {
    try {
        if (mode == "CHIME" || mode == "ADHAN") {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
            tone.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
        }
    } catch (e: Exception) {
        // Ignored
    }
}
