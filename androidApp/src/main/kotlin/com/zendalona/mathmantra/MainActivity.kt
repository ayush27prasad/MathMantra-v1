package com.zendalona.mathmantra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zendalona.mathmantra.platform.AndroidMotionSensor
import com.zendalona.mathmantra.platform.AndroidSpeechRecognizer
import com.zendalona.mathmantra.platform.AppServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val app = application as MathMantraApplication
        val services = AppServices(
            speaker = app.speaker,
            sounds = app.sounds,
            motion = AndroidMotionSensor(this),
            speech = AndroidSpeechRecognizer(this),
        )
        setContent { App(services) }
    }
}
