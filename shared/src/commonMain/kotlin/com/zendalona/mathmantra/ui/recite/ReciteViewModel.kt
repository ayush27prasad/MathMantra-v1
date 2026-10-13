package com.zendalona.mathmantra.ui.recite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.domain.QuestionGenerator
import com.zendalona.mathmantra.domain.checkRecital
import com.zendalona.mathmantra.platform.Speaker
import com.zendalona.mathmantra.platform.SpeechRecognizer
import com.zendalona.mathmantra.platform.SpeechResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MicStatus { Ready, Listening, PermissionDenied, Unavailable }

data class ReciteUiState(
    val range: IntRange,
    val status: MicStatus,
    val heard: String? = null,
    val feedback: Feedback? = null,
) {
    val prompt: String get() = "Count from ${range.first} to ${range.last}"
}

class ReciteViewModel(
    private val speaker: Speaker,
    private val speech: SpeechRecognizer,
    private val generator: QuestionGenerator = QuestionGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReciteUiState(
            range = generator.countingRange(),
            status = if (speech.isAvailable) MicStatus.Ready else MicStatus.Unavailable,
        ),
    )
    val state: StateFlow<ReciteUiState> = _state.asStateFlow()

    init {
        speakPrompt()
    }

    fun speakPrompt() = speaker.speak(_state.value.prompt)

    fun listen() {
        val status = _state.value.status
        if (status == MicStatus.Listening || status == MicStatus.Unavailable) return
        viewModelScope.launch {
            if (!speech.requestPermission()) {
                _state.update { it.copy(status = MicStatus.PermissionDenied) }
                return@launch
            }
            speaker.stop()
            _state.update { it.copy(status = MicStatus.Listening, heard = null) }

            val result = speech.listen()
            val range = _state.value.range
            val (heard, feedback) = when (result) {
                is SpeechResult.Heard -> {
                    val check = checkRecital(range, result.text)
                    result.text to if (check.correct) {
                        Feedback.correct("You counted from ${range.first} to ${range.last}.")
                    } else {
                        Feedback.wrong(missedDetail(check.missing))
                    }
                }
                is SpeechResult.Failed -> null to Feedback.wrong("I couldn't hear you. Tap the microphone and count again.")
            }
            _state.update { it.copy(status = MicStatus.Ready, heard = heard, feedback = feedback) }
            speaker.speak(feedback.spoken)
        }
    }

    /** A correct answer moves on to a new range; a wrong one lets the child try the same range again. */
    fun dismissFeedback() {
        val correct = _state.value.feedback?.correct ?: return
        _state.update {
            if (correct) it.copy(range = generator.countingRange(), heard = null, feedback = null) else it.copy(feedback = null)
        }
        speakPrompt()
    }

    private fun missedDetail(missing: List<Int>): String = when {
        missing.isEmpty() -> "Count every number in order."
        missing.size == 1 -> "You missed ${missing.single()}."
        missing.size <= 4 -> "You missed ${missing.dropLast(1).joinToString(", ")} and ${missing.last()}."
        else -> "Some numbers were missing. Say each number clearly."
    }
}
