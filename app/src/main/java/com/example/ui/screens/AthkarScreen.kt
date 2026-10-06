package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DhikrCategory
import com.example.model.DhikrItem
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.theme.*

@Composable
fun AthkarScreen(
    viewModel: KuwaitSalatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedDhikrCategory.collectAsState()
    val allAthkar by viewModel.athkarList.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    val filteredAthkar = remember(selectedCategory, allAthkar) {
        allAthkar.filter { it.category == selectedCategory }
    }

    val completedCount = filteredAthkar.count { it.currentCount >= it.targetCount }
    val totalCount = filteredAthkar.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("athkar_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "الأذكار والأدعية النبوية" else "Athkar & Daily Supplications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) "$completedCount من $totalCount مكتمل" else "$completedCount of $totalCount completed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.resetCategoryDhikr(selectedCategory) },
                    modifier = Modifier.testTag("reset_category_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Category",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Scrollable Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DhikrCategory.values()) { category ->
                    val isSelected = category == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectDhikrCategory(category) },
                        label = {
                            Text(
                                text = if (isArabic) category.titleAr else category.titleEn,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KuwaitEmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Athkar List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(filteredAthkar, key = { it.id }) { item ->
                DhikrCard(
                    item = item,
                    isArabic = isArabic,
                    onTapCount = {
                        triggerLightTap(context)
                        viewModel.incrementDhikrItem(item.id)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DhikrCard(
    item: DhikrItem,
    isArabic: Boolean,
    onTapCount: () -> Unit
) {
    val isComplete = item.currentCount >= item.targetCount
    val cardColor by animateColorAsState(
        targetValue = if (isComplete) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surface,
        label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Arabic Text (Prominent, Elegant)
            Text(
                text = item.arabicText,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 32.sp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Transliteration
            Text(
                text = item.transliteration,
                fontSize = 13.sp,
                color = KuwaitEmeraldPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // English Translation
            Text(
                text = item.englishTranslation,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Source / Hadith virtue
            Text(
                text = "📖 ${item.benefitOrSource}",
                fontSize = 11.sp,
                color = KuwaitGoldAccent,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Counter tap bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress count text
                Text(
                    text = "${item.currentCount} / ${item.targetCount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isComplete) KuwaitEmeraldPrimary else MaterialTheme.colorScheme.onSurface
                )

                // Interactive Tap Button
                Button(
                    onClick = onTapCount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isComplete) KuwaitEmeraldPrimary else KuwaitGoldAccent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (isComplete) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "اكتمل" else "Done", fontSize = 13.sp)
                    } else {
                        Text(
                            text = if (isArabic) "اضغط (${item.targetCount - item.currentCount})"
                            else "Count (${item.targetCount - item.currentCount})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun triggerLightTap(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(25)
        }
    } catch (e: Exception) {
        // Haptic feedback not available
    }
}
