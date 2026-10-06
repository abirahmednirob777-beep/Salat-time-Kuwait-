package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.KuwaitGovernorate
import com.example.model.PrayerType
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: KuwaitSalatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dailyTimes by viewModel.dailyPrayerTimes.collectAsState()
    val nextInfo by viewModel.nextPrayerInfo.collectAsState()
    val selectedGov by viewModel.selectedGovernorate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val is12Hour by viewModel.is12HourFormat.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val prayerLog by viewModel.prayerLog.collectAsState()
    val isAutoLocation by viewModel.isAutoLocationEnabled.collectAsState()

    var showGovSheet by remember { mutableStateOf(false) }
    var showMonthlySheet by remember { mutableStateOf(false) }
    var showExtraTimes by remember { mutableStateOf(false) }
    var isDetectingLocation by remember { mutableStateOf(false) }

    val isToday = selectedDate == LocalDate.now()

    // Location Permission Request Flow
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            isDetectingLocation = true
            viewModel.detectAndApplyLocation(context) { success, msg ->
                isDetectingLocation = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
            }
        } else {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    if (isArabic) "تم رفض إذن الموقع. يمكنك اختيار المحافظة يدوياً."
                    else "Location permission denied. You can select your governorate manually."
                )
            }
        }
    }

    val requestLocationDetection = {
        if (viewModel.hasLocationPermission(context)) {
            isDetectingLocation = true
            viewModel.detectAndApplyLocation(context) { success, msg ->
                isDetectingLocation = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(msg)
                }
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("prayer_times_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
        // Hero Mosque Banner Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("hero_prayer_card")
            ) {
                // Background Mosque Hero Image
                Image(
                    painter = painterResource(id = R.drawable.kuwait_grand_mosque_hero),
                    contentDescription = "Kuwait Grand Mosque",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentScale = ContentScale.Crop
                )

                // Atmospheric Gradient Overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x77071F1B),
                                    Color(0xDD071F1B),
                                    Color(0xF5061614)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Governorate selector chip + Quick Auto-Detect + Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                onClick = { showGovSheet = true },
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x33FFFFFF),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.linearGradient(
                                        listOf(Color(0x66FFFFFF), Color(0x22FFFFFF))
                                    )
                                ),
                                modifier = Modifier.testTag("governorate_selector_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isAutoLocation) KuwaitEmeraldLight else KuwaitGoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) selectedGov.arabicName else selectedGov.englishName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Quick GPS Locate Me Button
                            Surface(
                                onClick = requestLocationDetection,
                                shape = CircleShape,
                                color = if (isAutoLocation) KuwaitEmeraldPrimary.copy(alpha = 0.6f) else Color(0x33FFFFFF),
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("auto_detect_location_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDetectingLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = KuwaitGoldAccent
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = "Auto Detect Location",
                                            tint = if (isAutoLocation) Color.White else KuwaitGoldAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Hijri Calendar Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = KuwaitGoldAccent.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isArabic) {
                                    "${dailyTimes.hijriDay} ${dailyTimes.hijriMonthNameAr} ${dailyTimes.hijriYear} هـ"
                                } else {
                                    "${dailyTimes.hijriDay} ${dailyTimes.hijriMonthName} ${dailyTimes.hijriYear} AH"
                                },
                                color = KuwaitGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Next Prayer countdown banner
                    if (isToday) {
                        Text(
                            text = if (isArabic) "الصلاة القادمة: ${nextInfo.nextPrayer.arabicName}"
                            else "NEXT PRAYER: ${nextInfo.nextPrayer.englishName.uppercase()}",
                            color = KuwaitGoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = viewModel.formatSecondsToCountdown(nextInfo.remainingSeconds),
                                color = Color.White,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )

                            Text(
                                text = viewModel.formatPrayerTime(nextInfo.nextPrayerTime),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Period progress bar
                        LinearProgressIndicator(
                            progress = { nextInfo.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = KuwaitGoldAccent,
                            trackColor = Color(0x33FFFFFF)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "الحالية: ${nextInfo.currentPrayer.arabicName}"
                                else "Current: ${nextInfo.currentPrayer.englishName}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (isArabic) "المتبقي للأذان" else "Until Adhan",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // Browsing different date banner
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                text = if (isArabic) "مواقيت تاريخ محدد" else "PRAYER TIMES FOR",
                                color = KuwaitGoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { viewModel.resetToToday() },
                                colors = ButtonDefaults.textButtonColors(contentColor = KuwaitGoldLight),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "العودة إلى اليوم" else "Return to Today")
                            }
                        }
                    }
                }
            }
        }

        // Date navigation bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.changeDate(-1) },
                        modifier = Modifier.testTag("prev_date_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isToday) {
                                if (isArabic) "اليوم" else "Today"
                            } else {
                                selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showMonthlySheet = true },
                            modifier = Modifier.testTag("calendar_monthly_button")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Monthly Schedule", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { viewModel.changeDate(1) },
                            modifier = Modifier.testTag("next_date_button")
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }
                }
            }
        }

        // Section Title: Daily Prayers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الصلوات الخمس والشروق" else "Daily Prayers & Sunrise",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (is12Hour) "12-hour" else "24-hour",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { viewModel.toggle12HourFormat() }
                )
            }
        }

        // The 6 main prayer items
        val mainPrayers = listOf(
            PrayerType.FAJR,
            PrayerType.SUNRISE,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        )

        items(mainPrayers) { prayer ->
            val prayerTime = dailyTimes.getTimeFor(prayer)
            val isNext = isToday && nextInfo.nextPrayer == prayer
            val isCurrent = isToday && nextInfo.currentPrayer == prayer
            val isCompleted = prayerLog[prayer] == true
            val notifEnabled = viewModel.isNotificationEnabled(prayer)

            PrayerCardItem(
                prayer = prayer,
                timeString = viewModel.formatPrayerTime(prayerTime),
                isNext = isNext,
                isCurrent = isCurrent,
                isCompleted = isCompleted,
                notifEnabled = notifEnabled,
                isArabic = isArabic,
                onToggleComplete = { viewModel.togglePrayerCompleted(prayer) },
                onToggleNotification = { viewModel.togglePrayerNotification(prayer) }
            )
        }

        // Extra timings (Midnight & Qiyam / Last third) expandable toggle
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExtraTimes = !showExtraTimes }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isArabic) "أوقات الليل وقيام الليل" else "Night & Qiyam Times",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Icon(
                            if (showExtraTimes) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showExtraTimes) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "منتصف الليل الإسلامي" else "Islamic Midnight",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatPrayerTime(dailyTimes.midnight),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "الثلث الأخير (وقت القيام)" else "Last Third (Qiyam al-Layl)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatPrayerTime(dailyTimes.lastThird),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KuwaitGoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    // Governorate Bottom Sheet
    if (showGovSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGovSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = if (isArabic) "اختر المحافظة في الكويت" else "Select Kuwait Governorate",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // GPS Auto-detect Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable {
                            showGovSheet = false
                            requestLocationDetection()
                        }
                        .testTag("sheet_auto_detect_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAutoLocation) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, KuwaitGoldAccent)
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(KuwaitEmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "تحديد تلقائي عبر GPS" else "Auto-Detect via GPS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isArabic) "مطابقة إحداثيات موقعك مع المحافظات الست"
                                    else "Finds closest Kuwait governorate automatically",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isAutoLocation) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = KuwaitEmeraldPrimary
                            ) {
                                Text(
                                    text = if (isArabic) "مفعل" else "ACTIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isArabic) "أو اختر يدوياً:" else "Or select manually:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                KuwaitGovernorate.values().forEach { gov ->
                    val isSelected = gov == selectedGov && !isAutoLocation
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.selectGovernorate(gov)
                                viewModel.setAutoLocationEnabled(false)
                                showGovSheet = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) gov.arabicName else gov.englishName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = gov.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Monthly timetable preview bottom sheet
    if (showMonthlySheet) {
        ModalBottomSheet(
            onDismissRequest = { showMonthlySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            MonthlyScheduleSheet(
                viewModel = viewModel,
                currentDate = selectedDate,
                onSelectDate = { date ->
                    viewModel.setDate(date)
                    showMonthlySheet = false
                }
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 80.dp)
    )
    }
}

@Composable
fun PrayerCardItem(
    prayer: PrayerType,
    timeString: String,
    isNext: Boolean,
    isCurrent: Boolean,
    isCompleted: Boolean,
    notifEnabled: Boolean,
    isArabic: Boolean,
    onToggleComplete: () -> Unit,
    onToggleNotification: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isNext -> KuwaitGoldAccent
            isCurrent -> KuwaitEmeraldPrimary
            else -> Color.Transparent
        },
        animationSpec = tween(300)
    )

    val containerColor = when {
        isNext -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(
                width = if (isNext || isCurrent) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("prayer_card_${prayer.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (prayer) {
                                PrayerType.FAJR -> FajrBlue.copy(alpha = 0.15f)
                                PrayerType.SUNRISE -> SunriseOrange.copy(alpha = 0.15f)
                                PrayerType.DHUHR -> DhuhrGold.copy(alpha = 0.15f)
                                PrayerType.ASR -> AsrTeal.copy(alpha = 0.15f)
                                PrayerType.MAGHRIB -> MaghribCrimson.copy(alpha = 0.15f)
                                PrayerType.ISHA -> IshaIndigo.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (prayer) {
                        PrayerType.FAJR -> Icons.Default.NightsStay
                        PrayerType.SUNRISE -> Icons.Default.WbTwilight
                        PrayerType.DHUHR -> Icons.Default.WbSunny
                        PrayerType.ASR -> Icons.Default.LightMode
                        PrayerType.MAGHRIB -> Icons.Default.WbTwilight
                        PrayerType.ISHA -> Icons.Default.Nightlight
                        else -> Icons.Default.AccessTime
                    }
                    val iconTint = when (prayer) {
                        PrayerType.FAJR -> FajrBlue
                        PrayerType.SUNRISE -> SunriseOrange
                        PrayerType.DHUHR -> DhuhrGold
                        PrayerType.ASR -> AsrTeal
                        PrayerType.MAGHRIB -> MaghribCrimson
                        PrayerType.ISHA -> IshaIndigo
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) prayer.arabicName else prayer.englishName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (isNext) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KuwaitGoldAccent
                            ) {
                                Text(
                                    text = if (isArabic) "التالي" else "NEXT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isArabic) prayer.englishName else prayer.arabicName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Time + Actions (Bell & Completed Checkmark)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeString,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Notification Bell
                IconButton(
                    onClick = onToggleNotification,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        if (notifEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Toggle Notification",
                        tint = if (notifEnabled) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Completed Checkmark (for Fard prayers)
                if (prayer.isFard) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Prayer Completed",
                            tint = if (isCompleted) KuwaitEmeraldPrimary else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyScheduleSheet(
    viewModel: KuwaitSalatViewModel,
    currentDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    val selectedGov by viewModel.selectedGovernorate.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val daysInMonth = currentDate.lengthOfMonth()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (isArabic) "جدول مواقيت الصلاة الشهري - الكويت" else "Monthly Prayer Timetable - Kuwait",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp)
        ) {
            items((1..daysInMonth).toList()) { day ->
                val date = currentDate.withDayOfMonth(day)
                val times = com.example.calculator.KuwaitPrayerCalculator.calculatePrayerTimes(
                    date = date,
                    governorate = selectedGov
                )
                val isSelected = date == currentDate

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent
                        )
                        .clickable { onSelectDate(date) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${date.dayOfMonth} ${date.format(DateTimeFormatter.ofPattern("EEE"))}",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.width(60.dp)
                    )
                    Text("F: ${viewModel.formatPrayerTime(times.fajr)}", fontSize = 11.sp)
                    Text("D: ${viewModel.formatPrayerTime(times.dhuhr)}", fontSize = 11.sp)
                    Text("A: ${viewModel.formatPrayerTime(times.asr)}", fontSize = 11.sp)
                    Text("M: ${viewModel.formatPrayerTime(times.maghrib)}", fontSize = 11.sp)
                    Text("I: ${viewModel.formatPrayerTime(times.isha)}", fontSize = 11.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    }
}
