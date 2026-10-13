package com.zendalona.mathmantra.ui.counting

import androidx.lifecycle.ViewModel
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.domain.QuestionGenerator
import com.zendalona.mathmantra.platform.Sound
import com.zendalona.mathmantra.platform.SoundPlayer
import com.zendalona.mathmantra.platform.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class Instrument(val sound: Sound) {
    Bell(Sound.Bell),
    Drum(Sound.Drum),
}

data class CountingUiState(
    val prompt: String,
    val target: Int,
    val count: Int = 0,
    val feedback: Feedback? = null,
)

/** "Ring the bell N times" / "Tap the drum N times". */
class CountingGameViewModel(
    val instrument: Instrument,
    private val speaker: Speaker,
    private val sounds: SoundPlayer,
    private val generator: QuestionGenerator = QuestionGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow(newRound())
    val state: StateFlow<CountingUiState> = _state.asStateFlow()

    init {
        speakPrompt()
    }

    fun speakPrompt() = speaker.speak(_state.value.prompt)

    fun hit() {
        if (_state.value.feedback != null) return
        sounds.play(instrument.sound)
        _state.update { state ->
            val count = state.count + 1
            if (count < state.target) return@update state.copy(count = count)
            val detail = when (instrument) {
                Instrument.Bell -> "You rang the bell $count times."
                Instrument.Drum -> "You played $count beats."
            }
            state.copy(count = count, feedback = Feedback.correct(detail))
        }
        _state.value.feedback?.let { speaker.speak(it.spoken) }
    }

    fun nextRound() {
        _state.value = newRound()
        speakPrompt()
    }

    private fun newRound(): CountingUiState {
        val target = generator.countTarget()
        val prompt = when (instrument) {
            Instrument.Bell -> "Ring the bell $target times"
            Instrument.Drum -> "Tap the drum $target times"
        }
        return CountingUiState(prompt = prompt, target = target)
    }
}
