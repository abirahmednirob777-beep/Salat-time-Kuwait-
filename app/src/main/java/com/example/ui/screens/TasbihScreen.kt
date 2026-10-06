package com.example.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.theme.*

@Composable
fun TasbihScreen(
    viewModel: KuwaitSalatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val count by viewModel.tasbihCurrent.collectAsState()
    val target by viewModel.tasbihTarget.collectAsState()
    val lifetime by viewModel.tasbihLifetime.collectAsState()
    val phraseIndex by viewModel.tasbihPhraseIndex.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }

    val currentPhrase = viewModel.tasbihPhrases[phraseIndex]

    val progress = if (target > 0) (count.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 1f

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "buttonScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("tasbih_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "المسبحة الإلكترونية" else "Digital Tasbih",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = { soundEnabled = !soundEnabled }) {
                    Icon(
                        if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Click Sound",
                        tint = if (soundEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(onClick = { vibrationEnabled = !vibrationEnabled }) {
                    Icon(
                        if (vibrationEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                        contentDescription = "Toggle Vibration",
                        tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(onClick = { showResetDialog = true }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Count",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dhikr Phrase Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.prevTasbihPhrase() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Phrase")
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentPhrase.first,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentPhrase.second,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                IconButton(onClick = { viewModel.nextTasbihPhrase() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Phrase")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Selector Chips (33, 99, 100, Unlimited)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(33, 99, 100, 0).forEach { tgt ->
                val isSelected = target == tgt
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setTasbihTarget(tgt) },
                    label = {
                        Text(
                            text = if (tgt == 0) (if (isArabic) "مفتوح" else "∞ Unlimited") else "$tgt",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KuwaitEmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Big Tactile Tasbih Button (220dp with circular progress ring)
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(buttonScale)
                .testTag("tasbih_counter_button"),
            contentAlignment = Alignment.Center
        ) {
            // Circular progress background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 8.dp.toPx()
                val radius = (size.minDimension - strokeW) / 2
                val topLeft = Offset(strokeW / 2, strokeW / 2)
                val canvasSize = Size(size.width - strokeW, size.height - strokeW)

                // Track
                drawArc(
                    color = Color(0x22888888),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = canvasSize,
                    style = Stroke(width = strokeW)
                )

                // Progress Arc
                if (target > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(KuwaitGoldAccent, KuwaitEmeraldPrimary, KuwaitGoldAccent)
                        ),
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = canvasSize,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            // Central Touch Surface
            Box(
                modifier = Modifier
                    .size(208.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                KuwaitEmeraldLight,
                                KuwaitEmeraldPrimary,
                                Color(0xFF08332A)
                            )
                        )
                    )
                    .border(3.dp, KuwaitGoldAccent.copy(alpha = 0.6f), CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = Color.White)
                    ) {
                        if (vibrationEnabled) triggerVibration(context)
                        if (soundEnabled) playClickTone()
                        viewModel.incrementTasbih()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$count",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (target > 0) {
                        Text(
                            text = "/ $target",
                            color = KuwaitGoldLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "اضغط للتسبيح" else "TAP",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Lifetime dhikr total card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Stars,
                        contentDescription = null,
                        tint = KuwaitGoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "إجمالي التسبيحات المحفوظة" else "Lifetime Tasbih Total",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$lifetime",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.resetTasbih() },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(if (isArabic) "تصفير" else "Zero", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(if (isArabic) "إعادة ضبط العداد؟" else "Reset Counter?") },
            text = {
                Text(
                    if (isArabic) "هل ترغب في إعادة ضبط العداد الحالي إلى صفر؟"
                    else "Do you want to reset current counter to 0?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTasbih()
                        showResetDialog = false
                    }
                ) {
                    Text(if (isArabic) "نعم، تصفير" else "Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

private fun triggerVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(30)
        }
    } catch (e: Exception) {
        // Ignored
    }
}

private fun playClickTone() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
    } catch (e: Exception) {
        // Ignored
    }
}
