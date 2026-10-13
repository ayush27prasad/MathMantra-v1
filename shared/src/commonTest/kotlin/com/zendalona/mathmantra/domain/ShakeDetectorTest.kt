package com.zendalona.mathmantra.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShakeDetectorTest {
    private fun sample(t: Long, v: Float) = AccelerationSample(v, v, v, t)

    @Test
    fun stillDeviceNeverShakes() {
        val detector = ShakeDetector()
        val shakes = (0L..5_000L step 20).count { detector.onSample(sample(it, 9.8f / 3)) }
        assertEquals(0, shakes)
    }

    @Test
    fun suddenJoltIsAShake() {
        val detector = ShakeDetector()
        assertFalse(detector.onSample(sample(0, 0f)))
        assertTrue(detector.onSample(sample(150, 15f)))
    }

    @Test
    fun oneFlickCountsOnceThanksToCooldown() {
        val detector = ShakeDetector()
        detector.onSample(sample(0, 0f))
        assertTrue(detector.onSample(sample(110, 15f)))
        assertFalse(detector.onSample(sample(220, -15f)))
        assertTrue(detector.onSample(sample(500, 15f)))
    }
}
