package com.example.calculator

import com.example.model.DailyPrayerTimes
import com.example.model.KuwaitGovernorate
import com.example.model.NextPrayerInfo
import com.example.model.PrayerType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.*

object KuwaitPrayerCalculator {

    const val TIMEZONE_KUWAIT = 3.0 // UTC+3 Arabian Standard Time

    // Standard Kuwait Ministry of Awqaf & Islamic Affairs parameters
    var fajrAngle: Double = 18.0      // 18.0° standard Kuwait Awqaf
    var ishaAngle: Double = 17.5      // 17.5° standard Kuwait Awqaf
    var isHanafiAsr: Boolean = false  // false = Shafi'i/Hanbali standard (1x shadow)
    var use90MinIsha: Boolean = false // false = astronomical angle (Kuwait default)
    var fajrAdjustmentMinutes: Int = 0
    var dhuhrAdjustmentMinutes: Int = 1 // 1 min ihtiyat
    var asrAdjustmentMinutes: Int = 0
    var maghribAdjustmentMinutes: Int = 2 // 2 min after sunset
    var ishaAdjustmentMinutes: Int = 0

    fun calculatePrayerTimes(
        date: LocalDate,
        governorate: KuwaitGovernorate,
        hijriOffset: Int = 0
    ): DailyPrayerTimes {
        val lat = governorate.latitude
        val lng = governorate.longitude

        val jd = toJulianDate(date.year, date.monthValue, date.dayOfMonth)
        val d = jd - 2451545.0

        // Mean solar anomaly
        val g = fixAngle(357.529 + 0.98560028 * d)
        // Mean longitude of Sun
        val q = fixAngle(280.459 + 0.98564736 * d)
        // Ecliptic longitude of Sun
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        // Obliquity of ecliptic
        val e = 23.439 - 0.00000036 * d
        val sinL = sin(Math.toRadians(l))
        val cosL = cos(Math.toRadians(l))
        val sinE = sin(Math.toRadians(e))
        val cosE = cos(Math.toRadians(e))

        // Right ascension
        var ra = Math.toDegrees(atan2(cosE * sinL, cosL)) / 15.0
        ra = fixHour(ra)

        // Declination
        val delta = Math.toDegrees(asin(sinE * sinL))

        // Equation of Time in hours
        val eqT = (q / 15.0) - fixHour(ra)

        // Solar noon in local time hours
        val noon = fixHour(12.0 + TIMEZONE_KUWAIT - (lng / 15.0) - eqT)

        // Sunrise & Sunset angle with atmospheric refraction (-0.833°)
        val sunriseHourAngle = calculateHourAngle(-0.833, lat, delta)
        val fajrHourAngle = calculateHourAngle(-fajrAngle, lat, delta)
        val ishaHourAngle = calculateHourAngle(-ishaAngle, lat, delta)

        // Asr angle
        val asrFactor = if (isHanafiAsr) 2.0 else 1.0
        val asrAltitude = Math.toDegrees(
            atan(1.0 / (asrFactor + tan(Math.toRadians(abs(lat - delta)))))
        )
        val asrHourAngle = calculateHourAngle(asrAltitude, lat, delta)

        // Astronomical prayer times in decimal hours
        val fajrDecimal = noon - fajrHourAngle / 15.0 + (fajrAdjustmentMinutes / 60.0)
        val sunriseDecimal = noon - sunriseHourAngle / 15.0
        val dhuhrDecimal = noon + (dhuhrAdjustmentMinutes / 60.0)
        val asrDecimal = noon + asrHourAngle / 15.0 + (asrAdjustmentMinutes / 60.0)
        val sunsetDecimal = noon + sunriseHourAngle / 15.0
        val maghribDecimal = sunsetDecimal + (maghribAdjustmentMinutes / 60.0)

        val ishaDecimal = if (use90MinIsha) {
            maghribDecimal + 1.5 // 90 mins
        } else {
            noon + ishaHourAngle / 15.0 + (ishaAdjustmentMinutes / 60.0)
        }

        val fajrTime = decimalHoursToLocalTime(fajrDecimal)
        val sunriseTime = decimalHoursToLocalTime(sunriseDecimal)
        val dhuhrTime = decimalHoursToLocalTime(dhuhrDecimal)
        val asrTime = decimalHoursToLocalTime(asrDecimal)
        val maghribTime = decimalHoursToLocalTime(maghribDecimal)
        val ishaTime = decimalHoursToLocalTime(ishaDecimal)

        // Next Fajr for Midnight & Last Third calculations (approximate next morning)
        val nextFajrDecimal = fajrDecimal + 24.0
        val nightDuration = nextFajrDecimal - maghribDecimal
        val midnightDecimal = maghribDecimal + (nightDuration / 2.0)
        val lastThirdDecimal = maghribDecimal + (2.0 * nightDuration / 3.0)

        val midnightTime = decimalHoursToLocalTime(midnightDecimal % 24.0)
        val lastThirdTime = decimalHoursToLocalTime(lastThirdDecimal % 24.0)

        val hijriDate = HijriCalendarHelper.getHijriDate(date, hijriOffset)

        return DailyPrayerTimes(
            date = date,
            governorate = governorate,
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            midnight = midnightTime,
            lastThird = lastThirdTime,
            hijriDay = hijriDate.day,
            hijriMonthName = hijriDate.monthNameEn,
            hijriMonthNameAr = hijriDate.monthNameAr,
            hijriYear = hijriDate.year
        )
    }

    fun getNextPrayer(daily: DailyPrayerTimes, nowTime: LocalTime): NextPrayerInfo {
        val prayers = listOf(
            PrayerType.FAJR to daily.fajr,
            PrayerType.SUNRISE to daily.sunrise,
            PrayerType.DHUHR to daily.dhuhr,
            PrayerType.ASR to daily.asr,
            PrayerType.MAGHRIB to daily.maghrib,
            PrayerType.ISHA to daily.isha
        )

        // Find which window nowTime is in
        val currentPrayer: PrayerType
        val nextPrayer: PrayerType
        val prevTime: LocalTime
        val nextTime: LocalTime

        when {
            nowTime.isBefore(daily.fajr) -> {
                currentPrayer = PrayerType.ISHA
                nextPrayer = PrayerType.FAJR
                prevTime = daily.isha
                nextTime = daily.fajr
            }
            nowTime.isBefore(daily.sunrise) -> {
                currentPrayer = PrayerType.FAJR
                nextPrayer = PrayerType.SUNRISE
                prevTime = daily.fajr
                nextTime = daily.sunrise
            }
            nowTime.isBefore(daily.dhuhr) -> {
                currentPrayer = PrayerType.SUNRISE
                nextPrayer = PrayerType.DHUHR
                prevTime = daily.sunrise
                nextTime = daily.dhuhr
            }
            nowTime.isBefore(daily.asr) -> {
                currentPrayer = PrayerType.DHUHR
                nextPrayer = PrayerType.ASR
                prevTime = daily.dhuhr
                nextTime = daily.asr
            }
            nowTime.isBefore(daily.maghrib) -> {
                currentPrayer = PrayerType.ASR
                nextPrayer = PrayerType.MAGHRIB
                prevTime = daily.asr
                nextTime = daily.maghrib
            }
            nowTime.isBefore(daily.isha) -> {
                currentPrayer = PrayerType.MAGHRIB
                nextPrayer = PrayerType.ISHA
                prevTime = daily.maghrib
                nextTime = daily.isha
            }
            else -> {
                currentPrayer = PrayerType.ISHA
                nextPrayer = PrayerType.FAJR
                prevTime = daily.isha
                nextTime = daily.fajr
            }
        }

        val nowSeconds = nowTime.toSecondOfDay()
        val nextSeconds = nextTime.toSecondOfDay()
        val remainingSeconds = if (nextSeconds >= nowSeconds) {
            (nextSeconds - nowSeconds).toLong()
        } else {
            (86400 - nowSeconds + nextSeconds).toLong()
        }

        val prevSeconds = prevTime.toSecondOfDay()
        val totalPeriodSeconds = if (nextSeconds >= prevSeconds) {
            (nextSeconds - prevSeconds).toLong()
        } else {
            (86400 - prevSeconds + nextSeconds).toLong()
        }.coerceAtLeast(1L)

        val elapsed = (totalPeriodSeconds - remainingSeconds).coerceAtLeast(0L)
        val progress = (elapsed.toFloat() / totalPeriodSeconds.toFloat()).coerceIn(0f, 1f)

        return NextPrayerInfo(
            currentPrayer = currentPrayer,
            nextPrayer = nextPrayer,
            nextPrayerTime = nextTime,
            remainingSeconds = remainingSeconds,
            totalPeriodSeconds = totalPeriodSeconds,
            progress = progress
        )
    }

    private fun calculateHourAngle(alpha: Double, lat: Double, delta: Double): Double {
        val sinAlpha = sin(Math.toRadians(alpha))
        val sinLat = sin(Math.toRadians(lat))
        val cosLat = cos(Math.toRadians(lat))
        val sinDelta = sin(Math.toRadians(delta))
        val cosDelta = cos(Math.toRadians(delta))

        val cosHA = (sinAlpha - sinLat * sinDelta) / (cosLat * cosDelta)
        val clampedCosHA = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosHA))
    }

    private fun decimalHoursToLocalTime(hoursDecimal: Double): LocalTime {
        val totalSeconds = (hoursDecimal * 3600.0).roundToInt()
        val normalized = ((totalSeconds % 86400) + 86400) % 86400
        val h = normalized / 3600
        val m = (normalized % 3600) / 60
        val s = normalized % 60
        return LocalTime.of(h, m, s)
    }

    private fun toJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(angle: Double): Double {
        var a = angle % 360.0
        if (a < 0) a += 360.0
        return a
    }

    private fun fixHour(hour: Double): Double {
        var h = hour % 24.0
        if (h < 0) h += 24.0
        return h
    }
}
