@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.zendalona.mathmantra.platform

import com.zendalona.mathmantra.domain.AccelerationSample
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.AVFAudio.AVAudioApplication
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechUtteranceDefaultSpeechRate
import platform.AVFAudio.setActive
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSData
import platform.Foundation.NSLocale
import platform.Foundation.NSOperationQueue
import platform.Foundation.create
import platform.Speech.SFSpeechAudioBufferRecognitionRequest
import platform.Speech.SFSpeechRecognitionTask
import platform.Speech.SFSpeechRecognizer
import platform.Speech.SFSpeechRecognizerAuthorizationStatus
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val STANDARD_GRAVITY = 9.80665f

/** Entry point used by the iOS app's `MainViewController`. */
fun iosAppServices(): AppServices {
    AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryAmbient, error = null)
    return AppServices(
        speaker = IosSpeaker(),
        sounds = IosSoundPlayer(),
        motion = IosMotionSensor(),
        speech = IosSpeechRecognizer(),
    )
}

private class IosSpeaker : Speaker {
    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(text: String, rate: Float) {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-IN")
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * rate
        synthesizer.speakUtterance(utterance)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
}

private class IosSoundPlayer : SoundPlayer {
    private val players = mutableMapOf<Sound, AVAudioPlayer>()

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            Sound.entries.forEach { sound ->
                val bytes = runCatching { sound.readBytes() }.getOrNull() ?: return@forEach
                val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
                AVAudioPlayer(data = data, error = null).let { player ->
                    player.prepareToPlay()
                    players[sound] = player
                }
            }
        }
    }

    override fun play(sound: Sound) {
        val player = players[sound] ?: return
        player.currentTime = 0.0
        player.play()
    }
}

private class IosMotionSensor : MotionSensor {
    override val isAvailable: Boolean = CMMotionManager().accelerometerAvailable

    override fun accelerometer(): Flow<AccelerationSample> = callbackFlow {
        val manager = CMMotionManager().apply { accelerometerUpdateInterval = 0.02 }
        manager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
            data ?: return@startAccelerometerUpdatesToQueue
            val millis = (data.timestamp * 1000).toLong()
            data.acceleration.useContents {
                trySend(
                    AccelerationSample(
                        x = x.toFloat() * STANDARD_GRAVITY,
                        y = y.toFloat() * STANDARD_GRAVITY,
                        z = z.toFloat() * STANDARD_GRAVITY,
                        timestampMillis = millis,
                    ),
                )
            }
        }
        awaitClose { manager.stopAccelerometerUpdates() }
    }
}

private class IosSpeechRecognizer : SpeechRecognizer {
    private val recognizer = SFSpeechRecognizer(locale = NSLocale(localeIdentifier = "en-IN"))

    override val isAvailable: Boolean get() = recognizer.isAvailable()

    override suspend fun requestPermission(): Boolean {
        val speech = suspendCoroutine { continuation ->
            SFSpeechRecognizer.requestAuthorization { status ->
                continuation.resume(status == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized)
            }
        }
        if (!speech) return false
        return suspendCoroutine { continuation ->
            AVAudioApplication.requestRecordPermissionWithCompletionHandler { granted -> continuation.resume(granted) }
        }
    }

    private sealed interface Event {
        data class Partial(val text: String) : Event
        data class Final(val text: String) : Event
        data object Error : Event
    }

    /** SFSpeechRecognizer never stops by itself, so end the audio after a pause in the child's counting. */
    override suspend fun listen(): SpeechResult = withContext(Dispatchers.Main) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, mode = AVAudioSessionModeMeasurement, options = 0u, error = null)
        session.setActive(true, error = null)

        val events = Channel<Event>(Channel.UNLIMITED)
        val engine = AVAudioEngine()
        val request = SFSpeechAudioBufferRecognitionRequest().apply { shouldReportPartialResults = true }
        val input = engine.inputNode
        input.installTapOnBus(0u, bufferSize = 1024u, format = input.outputFormatForBus(0u)) { buffer, _ ->
            buffer?.let { request.appendAudioPCMBuffer(it) }
        }
        var task: SFSpeechRecognitionTask? = null

        try {
            engine.prepare()
            if (!engine.startAndReturnError(null)) return@withContext SpeechResult.Failed("Microphone unavailable")
            task = recognizer.recognitionTaskWithRequest(request) { result, error ->
                when {
                    result != null && result.isFinal() -> events.trySend(Event.Final(result.bestTranscription.formattedString))
                    result != null -> events.trySend(Event.Partial(result.bestTranscription.formattedString))
                    error != null -> events.trySend(Event.Error)
                }
            }

            var latest = ""
            while (true) {
                val timeout = if (latest.isEmpty()) 8_000L else 2_500L
                when (val event = withTimeoutOrNull(timeout) { events.receive() }) {
                    is Event.Partial -> latest = event.text
                    is Event.Final -> return@withContext SpeechResult.Heard(event.text)
                    Event.Error, null -> {
                        request.endAudio()
                        val final = withTimeoutOrNull(1_500L) {
                            var text: String? = null
                            while (text == null) {
                                val e = events.receive()
                                text = if (e is Event.Final) e.text else if (e is Event.Error) latest else null
                            }
                            text
                        } ?: latest
                        return@withContext if (final.isBlank()) SpeechResult.Failed("Nothing heard") else SpeechResult.Heard(final)
                    }
                }
            }
            @Suppress("UNREACHABLE_CODE")
            SpeechResult.Failed("unreachable")
        } finally {
            engine.stop()
            input.removeTapOnBus(0u)
            task?.cancel()
            session.setCategory(AVAudioSessionCategoryAmbient, error = null)
        }
    }
}
