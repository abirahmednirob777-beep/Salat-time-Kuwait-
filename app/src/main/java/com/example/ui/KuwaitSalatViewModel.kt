package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.KuwaitPrayerCalculator
import com.example.calculator.QiblaHelper
import com.example.data.AthkarRepository
import com.example.data.UserPreferencesRepository
import com.example.model.DailyPrayerTimes
import com.example.model.DhikrCategory
import com.example.model.DhikrItem
import com.example.model.KuwaitGovernorate
import com.example.model.NextPrayerInfo
import com.example.model.PrayerType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class KuwaitSalatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferencesRepository(application)

    private val _selectedGovernorate = MutableStateFlow(prefs.selectedGovernorate)
    val selectedGovernorate: StateFlow<KuwaitGovernorate> = _selectedGovernorate.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalTime.now())
    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()

    private val _hijriOffset = MutableStateFlow(prefs.hijriOffsetDays)
    val hijriOffset: StateFlow<Int> = _hijriOffset.asStateFlow()

    private val _is12HourFormat = MutableStateFlow(prefs.is12HourFormat)
    val is12HourFormat: StateFlow<Boolean> = _is12HourFormat.asStateFlow()

    private val _isArabic = MutableStateFlow(prefs.isArabicLanguage)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _audioMode = MutableStateFlow(prefs.audioMode)
    val audioMode: StateFlow<String> = _audioMode.asStateFlow()

    private val _isAutoLocationEnabled = MutableStateFlow(prefs.isAutoLocationEnabled)
    val isAutoLocationEnabled: StateFlow<Boolean> = _isAutoLocationEnabled.asStateFlow()

    private val _dailyPrayerTimes = MutableStateFlow(
        KuwaitPrayerCalculator.calculatePrayerTimes(
            date = LocalDate.now(),
            governorate = prefs.selectedGovernorate,
            hijriOffset = prefs.hijriOffsetDays
        )
    )
    val dailyPrayerTimes: StateFlow<DailyPrayerTimes> = _dailyPrayerTimes.asStateFlow()

    private val _nextPrayerInfo = MutableStateFlow(
        KuwaitPrayerCalculator.getNextPrayer(_dailyPrayerTimes.value, LocalTime.now())
    )
    val nextPrayerInfo: StateFlow<NextPrayerInfo> = _nextPrayerInfo.asStateFlow()

    // Qibla Info
    val qiblaBearing: Float
        get() = QiblaHelper.calculateQiblaBearing(
            _selectedGovernorate.value.latitude,
            _selectedGovernorate.value.longitude
        )

    val distanceToMakkahKm: Int
        get() = QiblaHelper.calculateDistanceToMakkahKm(
            _selectedGovernorate.value.latitude,
            _selectedGovernorate.value.longitude
        )

    // Tasbih State
    private val _tasbihCurrent = MutableStateFlow(0)
    val tasbihCurrent: StateFlow<Int> = _tasbihCurrent.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _tasbihLifetime = MutableStateFlow(prefs.lifetimeTasbihCount)
    val tasbihLifetime: StateFlow<Long> = _tasbihLifetime.asStateFlow()

    private val _tasbihPhraseIndex = MutableStateFlow(0)
    val tasbihPhraseIndex: StateFlow<Int> = _tasbihPhraseIndex.asStateFlow()

    val tasbihPhrases = listOf(
        Pair("سُبْحَانَ اللَّهِ", "Subhanallah (Glory be to Allah)"),
        Pair("الْحَمْدُ لِلَّهِ", "Alhamdulillah (All praise is due to Allah)"),
        Pair("اللَّهُ أَكْبَرُ", "Allahu Akbar (Allah is the Greatest)"),
        Pair("لَا إِلَهَ إِلَّا اللَّهُ", "La ilaha illa Allah (None worthy of worship but Allah)"),
        Pair("أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah (I ask forgiveness from Allah)"),
        Pair("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "Subhanallahi wa bihamdihi"),
        Pair("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "Allahumma salli 'ala Muhammad")
    )

    // Athkar State
    private val _selectedDhikrCategory = MutableStateFlow(DhikrCategory.MORNING)
    val selectedDhikrCategory: StateFlow<DhikrCategory> = _selectedDhikrCategory.asStateFlow()

    private val _athkarList = MutableStateFlow(AthkarRepository.getAthkar())
    val athkarList: StateFlow<List<DhikrItem>> = _athkarList.asStateFlow()

    // Daily Prayer Tracker state
    private val _prayerLog = MutableStateFlow(loadTodayPrayerLog())
    val prayerLog: StateFlow<Map<PrayerType, Boolean>> = _prayerLog.asStateFlow()

    init {
        // Recalculate options from prefs
        KuwaitPrayerCalculator.isHanafiAsr = prefs.isHanafiAsr

        // 1-second ticker coroutine
        viewModelScope.launch {
            while (isActive) {
                val now = LocalTime.now()
                _currentTime.value = now

                // Update countdown if date is today
                if (_selectedDate.value == LocalDate.now()) {
                    _nextPrayerInfo.value = KuwaitPrayerCalculator.getNextPrayer(
                        _dailyPrayerTimes.value,
                        now
                    )
                }
                delay(1000)
            }
        }
    }

    private fun calculateCurrentTimes(): DailyPrayerTimes {
        return KuwaitPrayerCalculator.calculatePrayerTimes(
            date = _selectedDate.value,
            governorate = _selectedGovernorate.value,
            hijriOffset = _hijriOffset.value
        )
    }

    fun selectGovernorate(gov: KuwaitGovernorate) {
        _selectedGovernorate.value = gov
        prefs.selectedGovernorate = gov
        refreshPrayerTimes()
    }

    fun changeDate(offsetDays: Long) {
        _selectedDate.value = _selectedDate.value.plusDays(offsetDays)
        refreshPrayerTimes()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
        refreshPrayerTimes()
    }

    fun resetToToday() {
        _selectedDate.value = LocalDate.now()
        refreshPrayerTimes()
    }

    fun toggle12HourFormat() {
        val newVal = !_is12HourFormat.value
        _is12HourFormat.value = newVal
        prefs.is12HourFormat = newVal
    }

    fun toggleLanguage() {
        val newVal = !_isArabic.value
        _isArabic.value = newVal
        prefs.isArabicLanguage = newVal
    }

    fun setHijriOffset(offset: Int) {
        _hijriOffset.value = offset
        prefs.hijriOffsetDays = offset
        refreshPrayerTimes()
    }

    fun toggleAsrMethod() {
        val newVal = !KuwaitPrayerCalculator.isHanafiAsr
        KuwaitPrayerCalculator.isHanafiAsr = newVal
        prefs.isHanafiAsr = newVal
        refreshPrayerTimes()
    }

    fun setAudioMode(mode: String) {
        _audioMode.value = mode
        prefs.audioMode = mode
    }

    fun togglePrayerNotification(prayer: PrayerType) {
        val current = prefs.isPrayerNotificationEnabled(prayer)
        prefs.setPrayerNotificationEnabled(prayer, !current)
    }

    fun isNotificationEnabled(prayer: PrayerType): Boolean {
        return prefs.isPrayerNotificationEnabled(prayer)
    }

    private fun refreshPrayerTimes() {
        val newTimes = calculateCurrentTimes()
        _dailyPrayerTimes.value = newTimes
        _nextPrayerInfo.value = KuwaitPrayerCalculator.getNextPrayer(newTimes, LocalTime.now())
        _prayerLog.value = loadTodayPrayerLog()
    }

    // Tasbih actions
    fun incrementTasbih() {
        val next = _tasbihCurrent.value + 1
        val target = _tasbihTarget.value
        if (next > target && target > 0) {
            _tasbihCurrent.value = 1
        } else {
            _tasbihCurrent.value = next
        }
        val newLifetime = _tasbihLifetime.value + 1
        _tasbihLifetime.value = newLifetime
        prefs.lifetimeTasbihCount = newLifetime
    }

    fun resetTasbih() {
        _tasbihCurrent.value = 0
    }

    fun setTasbihTarget(target: Int) {
        _tasbihTarget.value = target
        _tasbihCurrent.value = 0
    }

    fun nextTasbihPhrase() {
        _tasbihPhraseIndex.value = (_tasbihPhraseIndex.value + 1) % tasbihPhrases.size
        _tasbihCurrent.value = 0
    }

    fun prevTasbihPhrase() {
        _tasbihPhraseIndex.value =
            (_tasbihPhraseIndex.value - 1 + tasbihPhrases.size) % tasbihPhrases.size
        _tasbihCurrent.value = 0
    }

    // Athkar actions
    fun selectDhikrCategory(category: DhikrCategory) {
        _selectedDhikrCategory.value = category
    }

    fun incrementDhikrItem(id: String) {
        _athkarList.update { list ->
            list.map { item ->
                if (item.id == id) {
                    val nextCount = if (item.currentCount < item.targetCount) item.currentCount + 1 else 0
                    item.copy(currentCount = nextCount)
                } else item
            }
        }
    }

    fun resetCategoryDhikr(category: DhikrCategory) {
        _athkarList.update { list ->
            list.map { item ->
                if (item.category == category) item.copy(currentCount = 0) else item
            }
        }
    }

    // Prayer tracker actions
    private fun loadTodayPrayerLog(): Map<PrayerType, Boolean> {
        val date = _selectedDate.value
        return listOf(
            PrayerType.FAJR,
            PrayerType.DHUHR,
            PrayerType.ASR,
            PrayerType.MAGHRIB,
            PrayerType.ISHA
        ).associateWith { prefs.isPrayerCompleted(date, it) }
    }

    fun togglePrayerCompleted(prayer: PrayerType) {
        val date = _selectedDate.value
        val current = prefs.isPrayerCompleted(date, prayer)
        val updated = !current
        prefs.setPrayerCompleted(date, prayer, updated)
        _prayerLog.update { map ->
            map.toMutableMap().apply { put(prayer, updated) }
        }
    }

    // Helper formatters
    fun formatPrayerTime(time: LocalTime): String {
        return if (_is12HourFormat.value) {
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            time.format(formatter)
        } else {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            time.format(formatter)
        }
    }

    fun formatSecondsToCountdown(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hrs, mins, secs)
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun setAutoLocationEnabled(enabled: Boolean) {
        _isAutoLocationEnabled.value = enabled
        prefs.isAutoLocationEnabled = enabled
    }

    @SuppressLint("MissingPermission")
    fun detectAndApplyLocation(
        context: Context,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (!hasLocationPermission(context)) {
            onComplete(false, if (_isArabic.value) "إذن الموقع غير ممنوح" else "Location permission not granted")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onComplete(false, if (_isArabic.value) "خدمة الموقع غير متاحة" else "Location service unavailable")
            return
        }

        var bestLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore security or disabled provider exceptions
            }
        }

        if (bestLocation != null) {
            val closestGov = KuwaitGovernorate.findClosest(bestLocation.latitude, bestLocation.longitude)
            selectGovernorate(closestGov)
            setAutoLocationEnabled(true)
            val msg = if (_isArabic.value) "تم تحديد المحافظة: ${closestGov.arabicName}"
            else "Detected: ${closestGov.englishName}"
            onComplete(true, msg)
        } else {
            try {
                val candidateProvider = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    LocationManager.NETWORK_PROVIDER
                } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else null

                if (candidateProvider != null) {
                    locationManager.requestSingleUpdate(
                        candidateProvider,
                        object : LocationListener {
                            override fun onLocationChanged(loc: Location) {
                                val closestGov = KuwaitGovernorate.findClosest(loc.latitude, loc.longitude)
                                selectGovernorate(closestGov)
                                setAutoLocationEnabled(true)
                                val msg = if (_isArabic.value) "تم تحديد المحافظة: ${closestGov.arabicName}"
                                else "Detected: ${closestGov.englishName}"
                                onComplete(true, msg)
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                            override fun onProviderEnabled(provider: String) {}
                            override fun onProviderDisabled(provider: String) {}
                        },
                        null
                    )
                } else {
                    onComplete(
                        false,
                        if (_isArabic.value) "يرجى تفعيل خدمة الموقع (GPS) في الهاتف"
                        else "Please enable GPS/Location on your device"
                    )
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to get location")
            }
        }
    }
}
