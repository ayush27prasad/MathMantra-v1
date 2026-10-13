package com.zendalona.mathmantra.platform

import com.zendalona.mathmantra.domain.AccelerationSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

/** Desktop has no accelerometer or built-in speech recognition, so those games fall back to tapping. */
fun desktopAppServices(): AppServices = AppServices(
    speaker = DesktopSpeaker(),
    sounds = DesktopSoundPlayer(),
    motion = object : MotionSensor {
        override val isAvailable = false
        override fun accelerometer(): Flow<AccelerationSample> = emptyFlow()
    },
    speech = object : SpeechRecognizer {
        override val isAvailable = false
        override suspend fun requestPermission() = false
        override suspend fun listen(): SpeechResult = SpeechResult.Failed("Speech recognition is not supported on desktop")
    },
)

/** Speaks through the operating system's own speech engine. */
private class DesktopSpeaker : Speaker {
    private val os = System.getProperty("os.name").lowercase()
    private var process: Process? = null

    override fun speak(text: String, rate: Float) {
        stop()
        val command = when {
            "mac" in os -> listOf("say", "-r", "${(180 * rate).toInt()}", text)
            "win" in os -> listOf(
                "powershell", "-NoProfile", "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                    "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "\$s.Rate = ${((rate - 1) * 10).toInt()}; \$s.Speak('${text.replace("'", "''")}')",
            )
            else -> listOf("spd-say", "--wait", "--rate", "${((rate - 1) * 100).toInt()}", text)
        }
        process = runCatching {
            ProcessBuilder(command).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start()
        }.getOrNull()
    }

    override fun stop() {
        process?.destroy()
        process = null
    }
}

private class DesktopSoundPlayer : SoundPlayer {
    private val clips = ConcurrentHashMap<Sound, Clip>()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            Sound.entries.forEach { sound ->
                runCatching {
                    val stream = AudioSystem.getAudioInputStream(ByteArrayInputStream(sound.readBytes()))
                    clips[sound] = AudioSystem.getClip().apply { open(stream) }
                }
            }
        }
    }

    override fun play(sound: Sound) {
        clips[sound]?.apply {
            stop()
            framePosition = 0
            start()
        }
    }
}
