package com.zendalona.mathmantra.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs

/** One accelerometer reading in m/s². */
data class AccelerationSample(val x: Float, val y: Float, val z: Float, val timestampMillis: Long)

/**
 * Turns raw accelerometer samples into discrete shakes. Samples are taken at most every
 * [sampleIntervalMillis]; after a shake fires, further shakes are ignored for [cooldownMillis]
 * so one flick of the wrist counts once.
 */
class ShakeDetector(
    private val threshold: Float = 2000f,
    private val sampleIntervalMillis: Long = 100,
    private val cooldownMillis: Long = 350,
) {
    private var last: AccelerationSample? = null
    private var lastShakeAt: Long? = null

    fun onSample(sample: AccelerationSample): Boolean {
        val previous = last
        if (previous == null) {
            last = sample
            return false
        }
        val elapsed = sample.timestampMillis - previous.timestampMillis
        if (elapsed <= sampleIntervalMillis) return false
        last = sample

        val delta = abs(sample.x + sample.y + sample.z - previous.x - previous.y - previous.z)
        val speed = delta / elapsed * 10_000
        val coolingDown = lastShakeAt?.let { sample.timestampMillis - it < cooldownMillis } ?: false
        if (speed <= threshold || coolingDown) return false
        lastShakeAt = sample.timestampMillis
        return true
    }
}

fun Flow<AccelerationSample>.shakes(detector: ShakeDetector = ShakeDetector()): Flow<Unit> = flow {
    collect { if (detector.onSample(it)) emit(Unit) }
}
