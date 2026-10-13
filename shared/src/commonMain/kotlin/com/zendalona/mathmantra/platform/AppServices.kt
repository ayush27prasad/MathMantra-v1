package com.zendalona.mathmantra.platform

import androidx.compose.runtime.staticCompositionLocalOf
import com.zendalona.mathmantra.domain.AccelerationSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Text-to-speech. A new utterance interrupts the current one. */
interface Speaker {
    fun speak(text: String, rate: Float = 1f)
    fun stop()
}

enum class Sound(val path: String) {
    Bell("files/bell_ring.wav"),
    Drum("files/drum.wav"),
}

interface SoundPlayer {
    fun play(sound: Sound)
}

interface MotionSensor {
    val isAvailable: Boolean
    fun accelerometer(): Flow<AccelerationSample>
}

sealed interface SpeechResult {
    data class Heard(val text: String) : SpeechResult
    data class Failed(val reason: String) : SpeechResult
}

interface SpeechRecognizer {
    val isAvailable: Boolean

    /** Asks for microphone (and, on iOS, speech) permission if needed. Returns whether it was granted. */
    suspend fun requestPermission(): Boolean

    /** Listens until the speaker pauses, then returns what was heard. Must be called from the main thread. */
    suspend fun listen(): SpeechResult
}

/** Platform capabilities, created once per platform entry point and handed to [com.zendalona.mathmantra.App]. */
class AppServices(
    val speaker: Speaker,
    val sounds: SoundPlayer,
    val motion: MotionSensor,
    val speech: SpeechRecognizer,
) {
    companion object {
        /** No-op services for previews and tests. */
        val Silent = AppServices(
            speaker = object : Speaker {
                override fun speak(text: String, rate: Float) = Unit
                override fun stop() = Unit
            },
            sounds = object : SoundPlayer {
                override fun play(sound: Sound) = Unit
            },
            motion = object : MotionSensor {
                override val isAvailable = false
                override fun accelerometer() = emptyFlow<AccelerationSample>()
            },
            speech = object : SpeechRecognizer {
                override val isAvailable = false
                override suspend fun requestPermission() = false
                override suspend fun listen(): SpeechResult = SpeechResult.Failed("unavailable")
            },
        )
    }
}

val LocalAppServices = staticCompositionLocalOf { AppServices.Silent }
