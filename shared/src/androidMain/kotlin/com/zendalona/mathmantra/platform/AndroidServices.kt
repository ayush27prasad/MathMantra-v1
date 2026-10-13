package com.zendalona.mathmantra.platform

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.zendalona.mathmantra.domain.AccelerationSample
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Locale
import kotlin.coroutines.resume
import android.speech.SpeechRecognizer as PlatformRecognizer

/** App-scoped text-to-speech. Utterances requested before the engine is ready are spoken once it is. */
class AndroidSpeaker(context: Context) : Speaker {
    private var ready = false
    private var pending: Pair<String, Float>? = null
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status != TextToSpeech.SUCCESS) return@TextToSpeech
        val result = tts.setLanguage(Locale.forLanguageTag("en-IN"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.US)
        ready = true
        pending?.let { (text, rate) -> speak(text, rate) }
        pending = null
    }

    override fun speak(text: String, rate: Float) {
        if (!ready) {
            pending = text to rate
            return
        }
        tts.setSpeechRate(rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    override fun stop() {
        pending = null
        if (ready) tts.stop()
    }
}

/** App-scoped sound effects. WAVs are copied out of the Compose resources once, then played through [SoundPool]. */
class AndroidSoundPlayer(context: Context) : SoundPlayer {
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val ids = mutableMapOf<Sound, Int>()

    init {
        val cache = context.applicationContext.cacheDir
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            Sound.entries.forEach { sound ->
                runCatching {
                    val file = File(cache, "sound_${sound.name}.wav").apply { writeBytes(sound.readBytes()) }
                    val id = pool.load(file.path, 1)
                    synchronized(ids) { ids[sound] = id }
                }
            }
        }
    }

    override fun play(sound: Sound) {
        val id = synchronized(ids) { ids[sound] } ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }
}

class AndroidMotionSensor(context: Context) : MotionSensor {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    override val isAvailable: Boolean = sensor != null

    override fun accelerometer(): Flow<AccelerationSample> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y, z) = event.values
                trySend(AccelerationSample(x, y, z, event.timestamp / 1_000_000))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { manager?.unregisterListener(listener) }
    }
}

/**
 * Wraps the platform recogniser. Must be constructed before [activity] is started, since it
 * registers the microphone permission launcher.
 */
class AndroidSpeechRecognizer(private val activity: ComponentActivity) : SpeechRecognizer {
    private var permissionResult: CompletableDeferred<Boolean>? = null
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionResult?.complete(granted)
            permissionResult = null
        }

    override val isAvailable: Boolean get() = PlatformRecognizer.isRecognitionAvailable(activity)

    override suspend fun requestPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) return true
        val result = CompletableDeferred<Boolean>().also { permissionResult = it }
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        return result.await()
    }

    override suspend fun listen(): SpeechResult = suspendCancellableCoroutine { continuation ->
        val recognizer = PlatformRecognizer.createSpeechRecognizer(activity)
        fun finish(result: SpeechResult) {
            recognizer.destroy()
            if (continuation.isActive) continuation.resume(result)
        }
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val text = results.getStringArrayList(PlatformRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                finish(if (text.isNullOrBlank()) SpeechResult.Failed("Nothing heard") else SpeechResult.Heard(text))
            }

            override fun onError(error: Int) = finish(SpeechResult.Failed("Recognition error $error"))
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            // Counting has natural pauses between numbers; don't cut the child off too early.
            .putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2_500L)
        recognizer.startListening(intent)
        continuation.invokeOnCancellation { activity.runOnUiThread { recognizer.destroy() } }
    }
}
