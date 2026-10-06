package com.example.calculator

import kotlin.math.*

object QiblaHelper {
    // Kaaba in Makkah Al-Mukarramah
    const val MAKKAH_LAT = 21.422487
    const val MAKKAH_LNG = 39.826206

    /**
     * Calculates the forward azimuth / bearing from a given location to the Kaaba.
     * Returns azimuth in degrees [0, 360).
     */
    fun calculateQiblaBearing(lat: Double, lng: Double): Float {
        val phi1 = Math.toRadians(lat)
        val phi2 = Math.toRadians(MAKKAH_LAT)
        val deltaLambda = Math.toRadians(MAKKAH_LNG - lng)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)

        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360.0) % 360.0
        return bearing.toFloat()
    }

    /**
     * Calculates the great circle distance to Makkah in kilometers.
     */
    fun calculateDistanceToMakkahKm(lat: Double, lng: Double): Int {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(MAKKAH_LAT - lat)
        val dLng = Math.toRadians(MAKKAH_LNG - lng)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat)) * cos(Math.toRadians(MAKKAH_LAT)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).roundToInt()
    }
}
