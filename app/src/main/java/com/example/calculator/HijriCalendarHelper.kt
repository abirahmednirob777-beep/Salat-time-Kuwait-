package com.example.calculator

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

object HijriCalendarHelper {

    private val MONTH_NAMES_EN = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val MONTH_NAMES_AR = arrayOf(
        "محرّم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    data class HijriDateResult(
        val day: Int,
        val month: Int,
        val year: Int,
        val monthNameEn: String,
        val monthNameAr: String
    ) {
        fun formatEnglish(): String = "$day $monthNameEn $year AH"
        fun formatArabic(): String = "$day $monthNameAr $year هـ"
    }

    fun getHijriDate(date: LocalDate, dayOffset: Int = 0): HijriDateResult {
        return try {
            val adjustedDate = date.plusDays(dayOffset.toLong())
            val hijrahDate = HijrahDate.from(adjustedDate)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val year = hijrahDate.get(ChronoField.YEAR)

            val monthIndex = (month - 1).coerceIn(0, 11)
            HijriDateResult(
                day = day,
                month = month,
                year = year,
                monthNameEn = MONTH_NAMES_EN[monthIndex],
                monthNameAr = MONTH_NAMES_AR[monthIndex]
            )
        } catch (e: Exception) {
            // Algorithmic fallback if HijrahDate throws
            fallbackHijri(date, dayOffset)
        }
    }

    private fun fallbackHijri(date: LocalDate, dayOffset: Int): HijriDateResult {
        // Approximate calculation: Julian Day based
        val epochDay = date.toEpochDay() + dayOffset + 2440588 // approx JD
        val l = epochDay - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val month = ((24 * l3) / 709).toInt()
        val day = (l3 - ((709 * month) / 24)).toInt()
        val year = (30 * n + j - 30).toInt()
        val monthIndex = (month - 1).coerceIn(0, 11)

        return HijriDateResult(
            day = day.coerceIn(1, 30),
            month = month.coerceIn(1, 12),
            year = year,
            monthNameEn = MONTH_NAMES_EN[monthIndex],
            monthNameAr = MONTH_NAMES_AR[monthIndex]
        )
    }
}
