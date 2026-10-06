package com.example

import com.example.calculator.HijriCalendarHelper
import com.example.calculator.KuwaitPrayerCalculator
import com.example.calculator.QiblaHelper
import com.example.data.AthkarRepository
import com.example.model.KuwaitGovernorate
import com.example.model.PrayerType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ExampleUnitTest {

    @Test
    fun testKuwaitPrayerTimeCalculations() {
        val testDate = LocalDate.of(2026, 10, 6)
        val times = KuwaitPrayerCalculator.calculatePrayerTimes(
            date = testDate,
            governorate = KuwaitGovernorate.AL_ASIMAH
        )

        assertNotNull(times.fajr)
        assertNotNull(times.sunrise)
        assertNotNull(times.dhuhr)
        assertNotNull(times.asr)
        assertNotNull(times.maghrib)
        assertNotNull(times.isha)

        // Verify logical chronological order of prayers
        assertTrue(times.fajr.isBefore(times.sunrise))
        assertTrue(times.sunrise.isBefore(times.dhuhr))
        assertTrue(times.dhuhr.isBefore(times.asr))
        assertTrue(times.asr.isBefore(times.maghrib))
        assertTrue(times.maghrib.isBefore(times.isha))

        // In Kuwait, Dhuhr is always around 11:30 - 12:15 UTC+3
        assertTrue(times.dhuhr.hour in 11..12)
    }

    @Test
    fun testAllSixGovernoratesCalculateSuccessfully() {
        val testDate = LocalDate.of(2026, 10, 6)
        KuwaitGovernorate.values().forEach { gov ->
            val times = KuwaitPrayerCalculator.calculatePrayerTimes(testDate, gov)
            assertNotNull("Fajr should not be null for ${gov.englishName}", times.fajr)
            assertNotNull("Dhuhr should not be null for ${gov.englishName}", times.dhuhr)
            assertNotNull("Isha should not be null for ${gov.englishName}", times.isha)
            assertTrue("Fajr before Sunrise for ${gov.englishName}", times.fajr.isBefore(times.sunrise))
            assertTrue("Sunset/Maghrib before Isha for ${gov.englishName}", times.maghrib.isBefore(times.isha))
        }
    }

    @Test
    fun testNextPrayerDetermination() {
        val testDate = LocalDate.of(2026, 10, 6)
        val times = KuwaitPrayerCalculator.calculatePrayerTimes(testDate, KuwaitGovernorate.AL_ASIMAH)

        // Case 1: Early morning before Fajr -> next is Fajr
        val earlyMorning = times.fajr.minusMinutes(30)
        val next1 = KuwaitPrayerCalculator.getNextPrayer(times, earlyMorning)
        assertEquals(PrayerType.FAJR, next1.nextPrayer)
        assertTrue(next1.remainingSeconds > 0)

        // Case 2: Right between Dhuhr and Asr -> next is Asr
        val midday = times.dhuhr.plusMinutes(15)
        val next2 = KuwaitPrayerCalculator.getNextPrayer(times, midday)
        assertEquals(PrayerType.ASR, next2.nextPrayer)
        assertTrue(next2.progress in 0f..1f)
    }

    @Test
    fun testKuwaitQiblaBearing() {
        val bearing = QiblaHelper.calculateQiblaBearing(
            KuwaitGovernorate.AL_ASIMAH.latitude,
            KuwaitGovernorate.AL_ASIMAH.longitude
        )

        // Qibla from Kuwait towards Makkah is approximately 224-226° SW
        assertTrue("Bearing should be approx 220-230 degrees, got $bearing", bearing in 220.0f..230.0f)

        val distance = QiblaHelper.calculateDistanceToMakkahKm(
            KuwaitGovernorate.AL_ASIMAH.latitude,
            KuwaitGovernorate.AL_ASIMAH.longitude
        )
        // Distance is ~1,200 km
        assertTrue("Distance should be around 1150-1250 km, got $distance", distance in 1100..1300)
    }

    @Test
    fun testHijriCalendar() {
        val testDate = LocalDate.of(2026, 10, 6)
        val hijri = HijriCalendarHelper.getHijriDate(testDate)
        assertTrue(hijri.day in 1..30)
        assertTrue(hijri.month in 1..12)
        assertTrue(hijri.year >= 1445)
        assertTrue(hijri.monthNameEn.isNotEmpty())
        assertTrue(hijri.monthNameAr.isNotEmpty())

        // Test offset
        val hijriOffsetPlus1 = HijriCalendarHelper.getHijriDate(testDate, 1)
        val hijriOffsetMinus1 = HijriCalendarHelper.getHijriDate(testDate, -1)
        assertNotNull(hijriOffsetPlus1)
        assertNotNull(hijriOffsetMinus1)
    }

    @Test
    fun testAthkarRepository() {
        val athkar = AthkarRepository.getAthkar()
        assertTrue("Should have rich athkar list", athkar.isNotEmpty())
        assertTrue("Should include morning athkar", athkar.any { it.category == com.example.model.DhikrCategory.MORNING })
        assertTrue("Should include after prayer athkar", athkar.any { it.category == com.example.model.DhikrCategory.POST_PRAYER })
        assertTrue("Each dhikr item has valid Arabic text", athkar.all { it.arabicText.isNotEmpty() })
        assertTrue("Each dhikr item has valid target count >= 1", athkar.all { it.targetCount >= 1 })
    }

    @Test
    fun testFindClosestGovernorate() {
        // Kuwait City (Grand Mosque) area
        val capital = KuwaitGovernorate.findClosest(29.378, 47.978)
        assertEquals(KuwaitGovernorate.AL_ASIMAH, capital)

        // Jahra Oasis area
        val jahra = KuwaitGovernorate.findClosest(29.340, 47.660)
        assertEquals(KuwaitGovernorate.JAHRA, jahra)

        // Ahmadi Oil town & Fahaheel
        val ahmadi = KuwaitGovernorate.findClosest(29.075, 48.080)
        assertEquals(KuwaitGovernorate.AHMADI, ahmadi)

        // Hawalli & Salmiya
        val hawalli = KuwaitGovernorate.findClosest(29.330, 48.030)
        assertEquals(KuwaitGovernorate.HAWALLI, hawalli)
    }
}
