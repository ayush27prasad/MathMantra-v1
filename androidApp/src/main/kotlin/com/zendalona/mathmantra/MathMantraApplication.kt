package com.zendalona.mathmantra

import android.app.Application
import com.zendalona.mathmantra.platform.AndroidSoundPlayer
import com.zendalona.mathmantra.platform.AndroidSpeaker

/** Holds the services that should outlive configuration changes. */
class MathMantraApplication : Application() {
    val speaker by lazy { AndroidSpeaker(this) }
    val sounds by lazy { AndroidSoundPlayer(this) }
}
