package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.KuwaitSalatViewModel
import com.example.ui.screens.AthkarScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.TrackerAndSettingsScreen
import com.example.ui.theme.KuwaitSalatTheme

enum class AppScreen(
    val titleEn: String,
    val titleAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PRAYER_TIMES("Prayers", "المواقيت", Icons.Filled.AccessTime, Icons.Outlined.AccessTime),
    QIBLA("Qibla", "القبلة", Icons.Filled.Explore, Icons.Outlined.Explore),
    ATHKAR("Athkar", "الأذكار", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    TASBIH("Tasbih", "المسبحة", Icons.Filled.RadioButtonChecked, Icons.Outlined.RadioButtonUnchecked),
    TRACKER_SETTINGS("Settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: KuwaitSalatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KuwaitSalatTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: KuwaitSalatViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.PRAYER_TIMES) }
    val isArabic by viewModel.isArabic.collectAsState()

    // Handle back button when not on home screen
    if (currentScreen != AppScreen.PRAYER_TIMES) {
        BackHandler {
            currentScreen = AppScreen.PRAYER_TIMES
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.titleEn
                            )
                        },
                        label = {
                            Text(
                                text = if (isArabic) screen.titleAr else screen.titleEn,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentScreen) {
            AppScreen.PRAYER_TIMES -> PrayerTimesScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.QIBLA -> QiblaScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.ATHKAR -> AthkarScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.TASBIH -> TasbihScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.TRACKER_SETTINGS -> TrackerAndSettingsScreen(viewModel = viewModel, modifier = modifier)
        }
    }
}
