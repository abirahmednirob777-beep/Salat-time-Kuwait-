package com.example.model

import java.time.LocalDate
import java.time.LocalTime

enum class KuwaitGovernorate(
    val englishName: String,
    val arabicName: String,
    val latitude: Double,
    val longitude: Double,
    val description: String
) {
    AL_ASIMAH("Al Asimah (Kuwait City)", "العاصمة (مدينة الكويت)", 29.3759, 47.9774, "Capital & Grand Mosque"),
    HAWALLI("Hawalli", "حولي", 29.3328, 48.0286, "Hawalli & Salmiya"),
    FARWANIYA("Farwaniya", "الفروانية", 29.2778, 47.9589, "Kuwait Int'l Airport & Farwaniya"),
    AHMADI("Al Ahmadi", "الأحمدي", 29.0769, 48.0839, "Ahmadi, Fahaheel & Mangaf"),
    JAHRA("Al Jahra", "الجهراء", 29.3375, 47.6581, "Jahra Oasis & Northern Kuwait"),
    MUBARAK_AL_KABEER("Mubarak Al-Kabeer", "مبارك الكبير", 29.2155, 48.0772, "Mubarak Al-Kabeer & Qurain");

    companion object {
        fun findClosest(latitude: Double, longitude: Double): KuwaitGovernorate {
            return values().minByOrNull { gov ->
                val dLat = gov.latitude - latitude
                val dLng = gov.longitude - longitude
                dLat * dLat + dLng * dLng
            } ?: AL_ASIMAH
        }
    }
}

enum class PrayerType(
    val englishName: String,
    val arabicName: String,
    val isFard: Boolean
) {
    FAJR("Fajr", "الفجر", true),
    SUNRISE("Sunrise", "الشروق", false),
    DHUHR("Dhuhr", "الظهر", true),
    ASR("Asr", "العصر", true),
    MAGHRIB("Maghrib", "المغرب", true),
    ISHA("Isha", "العشاء", true),
    MIDNIGHT("Midnight", "منتصف الليل", false),
    LAST_THIRD("Qiyam (Last 3rd)", "قيام الليل", false)
}

data class DailyPrayerTimes(
    val date: LocalDate,
    val governorate: KuwaitGovernorate,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val midnight: LocalTime,
    val lastThird: LocalTime,
    val hijriDay: Int,
    val hijriMonthName: String,
    val hijriMonthNameAr: String,
    val hijriYear: Int
) {
    fun getTimeFor(prayer: PrayerType): LocalTime = when (prayer) {
        PrayerType.FAJR -> fajr
        PrayerType.SUNRISE -> sunrise
        PrayerType.DHUHR -> dhuhr
        PrayerType.ASR -> asr
        PrayerType.MAGHRIB -> maghrib
        PrayerType.ISHA -> isha
        PrayerType.MIDNIGHT -> midnight
        PrayerType.LAST_THIRD -> lastThird
    }
}

data class NextPrayerInfo(
    val currentPrayer: PrayerType,
    val nextPrayer: PrayerType,
    val nextPrayerTime: LocalTime,
    val remainingSeconds: Long,
    val totalPeriodSeconds: Long,
    val progress: Float
)
