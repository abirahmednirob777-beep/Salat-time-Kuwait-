package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun QiblaScreen(
    viewModel: KuwaitSalatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedGov by viewModel.selectedGovernorate.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    val qiblaBearing = viewModel.qiblaBearing // approx 243.6°
    val distanceKm = viewModel.distanceToMakkahKm

    var deviceHeading by remember { mutableFloatStateOf(0f) }
    var hasCompassSensor by remember { mutableStateOf(false) }
    var isManualDemoMode by remember { mutableStateOf(false) }
    var manualHeading by remember { mutableFloatStateOf(qiblaBearing) }

    // Sensor listener
    DisposableEffect(isManualDemoMode) {
        if (isManualDemoMode) {
            onDispose { }
        } else {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)

            hasCompassSensor = rotationSensor != null

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        val rotationMatrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuthRad = orientation[0]
                        val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f
                        deviceHeading = azimuthDeg
                    } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                        deviceHeading = (event.values[0] + 360f) % 360f
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            if (rotationSensor != null) {
                sensorManager?.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager?.unregisterListener(listener)
            }
        }
    }

    val activeHeading = if (isManualDemoMode || !hasCompassSensor) manualHeading else deviceHeading

    // Calculate delta to Qibla
    val diffToQibla = abs((qiblaBearing - activeHeading + 180f) % 360f - 180f)
    val isFacingQibla = diffToQibla <= 4f

    // Trigger haptic when facing Qibla
    var lastVibratedFacing by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !lastVibratedFacing) {
            triggerHaptic(context)
        }
        lastVibratedFacing = isFacingQibla
    }

    val animatedRotation by animateFloatAsState(
        targetValue = -activeHeading,
        animationSpec = spring(stiffness = 300f),
        label = "compassRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = if (isArabic) "بوصلة القبلة - الكويت" else "Qibla Compass - Kuwait",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isArabic) {
                "من ${selectedGov.arabicName}: ${String.format(java.util.Locale.US, "%.1f", qiblaBearing)}° جنوب غرب"
            } else {
                "From ${selectedGov.englishName}: ${String.format(java.util.Locale.US, "%.1f", qiblaBearing)}° SW"
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Alignment status card
        val statusBg by animateColorAsState(
            targetValue = if (isFacingQibla) KuwaitEmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
            label = "statusBg"
        )
        val statusText = if (isFacingQibla) {
            if (isArabic) "✓ أنت متوجه مباشرة نحو الكعبة المشرفة!" else "✓ Perfectly Aligned with the Holy Kaaba!"
        } else {
            val directionText = if (qiblaBearing > activeHeading) {
                if (isArabic) "أدر الهاتف يميناً" else "Turn right"
            } else {
                if (isArabic) "أدر الهاتف يساراً" else "Turn left"
            }
            if (isArabic) "$directionText (${diffToQibla.roundToInt()}° للقبلة)"
            else "$directionText (${diffToQibla.roundToInt()}° to Qibla)"
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = statusBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isFacingQibla) Icons.Default.CheckCircle else Icons.Default.Explore,
                    contentDescription = null,
                    tint = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = if (isFacingQibla) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual Compass Dial (280dp)
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            if (isFacingQibla) KuwaitEmeraldPrimary else KuwaitGoldAccent,
                            if (isFacingQibla) KuwaitEmeraldLight else KuwaitGoldLight
                        )
                    ),
                    shape = CircleShape
                )
                .testTag("compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Rose (Dial rotates with device heading)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRotation),
                contentAlignment = Alignment.Center
            ) {
                // Compass markings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw degree tick marks
                    for (i in 0 until 360 step 15) {
                        val angleRad = Math.toRadians(i.toDouble() - 90)
                        val tickLen = if (i % 90 == 0) 14f else if (i % 30 == 0) 9f else 5f
                        val start = Offset(
                            (center.x + (radius - tickLen - 12) * cos(angleRad)).toFloat(),
                            (center.y + (radius - tickLen - 12) * sin(angleRad)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (radius - 12) * cos(angleRad)).toFloat(),
                            (center.y + (radius - 12) * sin(angleRad)).toFloat()
                        )
                        val tickColor = if (i % 90 == 0) Color(0xFFD4A338) else Color(0x66888888)
                        drawLine(
                            color = tickColor,
                            start = start,
                            end = end,
                            strokeWidth = if (i % 90 == 0) 3f else 1.5f
                        )
                    }
                }

                // Cardinal labels (N, E, S, W)
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("N", fontWeight = FontWeight.Bold, color = Color(0xFFD90429), fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text("S", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("E", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("W", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }

                // KAABA NEEDLE & BADGE at qiblaBearing (relative to North on the rotating dial)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(qiblaBearing),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 22.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFacingQibla) KuwaitEmeraldPrimary else KuwaitGoldAccent,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Kaaba",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (isArabic) "الكعبة" else "Kaaba",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Needle line pointing to center
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(56.dp)
                                .background(
                                    if (isFacingQibla) KuwaitEmeraldPrimary else KuwaitGoldAccent
                                )
                        )
                    }
                }
            }

            // Fixed Center Device Crosshair & Hub
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFacingQibla) KuwaitEmeraldPrimary else KuwaitGoldAccent
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Navigation,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Distance and bearing details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "زاوية القبلة" else "Qibla Angle",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.1f", qiblaBearing)}°",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KuwaitGoldAccent
                    )
                }

                VerticalDivider(modifier = Modifier.height(36.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "المسافة إلى مكة" else "Distance to Makkah",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "~$distanceKm km",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                VerticalDivider(modifier = Modifier.height(36.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "اتجاه الهاتف" else "Heading",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${activeHeading.roundToInt()}°",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual simulator slider / test mode for emulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "محاكي البوصلة (للاختبار والمحاكي)" else "Compass Simulator (For Testing)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isManualDemoMode || !hasCompassSensor,
                        onCheckedChange = { isManualDemoMode = it }
                    )
                }

                if (isManualDemoMode || !hasCompassSensor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "حرّك الزاوية لاختبار محاذاة القبلة:" else "Rotate angle to test alignment:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = manualHeading,
                        onValueChange = { manualHeading = it },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { manualHeading = qiblaBearing },
                            colors = ButtonDefaults.buttonColors(containerColor = KuwaitEmeraldPrimary)
                        ) {
                            Text(if (isArabic) "محاذاة القبلة مباشرة" else "Align to Qibla (225°)", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { manualHeading = 0f }
                        ) {
                            Text(if (isArabic) "شمال (0°)" else "North (0°)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private fun triggerHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(50)
        }
    } catch (e: Exception) {
        // Haptics not supported or permitted
    }
}
