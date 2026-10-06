package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.KuwaitGovernorate
import com.example.model.PrayerType
import java.time.LocalDate

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("kuwait_salat_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_GOVERNORATE = "governorate"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"
        private const val KEY_IS_12_HOUR = "is_12_hour"
        private const val KEY_HANAFI_ASR = "hanafi_asr"
        private const val KEY_IS_ARABIC = "is_arabic"
        private const val KEY_TASBIH_LIFETIME = "tasbih_lifetime_count"
        private const val KEY_AUDIO_MODE = "audio_mode" // "ADHAN", "CHIME", "VIBRATE", "SILENT"
        private const val KEY_AUTO_LOCATION = "auto_location_enabled"
    }

    var isAutoLocationEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LOCATION, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LOCATION, value).apply()

    var selectedGovernorate: KuwaitGovernorate
        get() {
            val name = prefs.getString(KEY_GOVERNORATE, KuwaitGovernorate.AL_ASIMAH.name)
            return try {
                KuwaitGovernorate.valueOf(name ?: KuwaitGovernorate.AL_ASIMAH.name)
            } catch (e: Exception) {
                KuwaitGovernorate.AL_ASIMAH
            }
        }
        set(value) {
            prefs.edit().putString(KEY_GOVERNORATE, value.name).apply()
        }

    var hijriOffsetDays: Int
        get() = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HIJRI_OFFSET, value).apply()

    var is12HourFormat: Boolean
        get() = prefs.getBoolean(KEY_IS_12_HOUR, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_12_HOUR, value).apply()

    var isHanafiAsr: Boolean
        get() = prefs.getBoolean(KEY_HANAFI_ASR, false)
        set(value) = prefs.edit().putBoolean(KEY_HANAFI_ASR, value).apply()

    var isArabicLanguage: Boolean
        get() = prefs.getBoolean(KEY_IS_ARABIC, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ARABIC, value).apply()

    var audioMode: String
        get() = prefs.getString(KEY_AUDIO_MODE, "ADHAN") ?: "ADHAN"
        set(value) = prefs.edit().putString(KEY_AUDIO_MODE, value).apply()

    var lifetimeTasbihCount: Long
        get() = prefs.getLong(KEY_TASBIH_LIFETIME, 0L)
        set(value) = prefs.edit().putLong(KEY_TASBIH_LIFETIME, value).apply()

    fun isPrayerNotificationEnabled(prayer: PrayerType): Boolean {
        return prefs.getBoolean("notif_${prayer.name}", true)
    }

    fun setPrayerNotificationEnabled(prayer: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("notif_${prayer.name}", enabled).apply()
    }

    // Daily prayer completed tracker
    fun isPrayerCompleted(date: LocalDate, prayer: PrayerType): Boolean {
        val key = "done_${date}_${prayer.name}"
        return prefs.getBoolean(key, false)
    }

    fun setPrayerCompleted(date: LocalDate, prayer: PrayerType, completed: Boolean) {
        val key = "done_${date}_${prayer.name}"
        prefs.edit().putBoolean(key, completed).apply()
    }

    fun getDailyCompletedCount(date: LocalDate): Int {
        val fardPrayers = listOf(PrayerType.FAJR, PrayerType.DHUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)
        return fardPrayers.count { isPrayerCompleted(date, it) }
    }
}
