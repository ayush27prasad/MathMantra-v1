package com.zendalona.mathmantra.ui.quiz

import androidx.lifecycle.ViewModel
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.domain.Question
import com.zendalona.mathmantra.domain.QuestionGenerator
import com.zendalona.mathmantra.platform.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class QuizMode { Choices, Typed }

data class QuizUiState(
    val question: Question,
    val choices: List<Int> = emptyList(),
    val rejected: Set<Int> = emptySet(),
    val input: String = "",
    val score: Int = 0,
    val feedback: Feedback? = null,
)

/** Arithmetic practice: pick one of four answers ([QuizMode.Choices]) or type it in ([QuizMode.Typed]). */
class QuizViewModel(
    private val mode: QuizMode,
    private val speaker: Speaker,
    private val generator: QuestionGenerator = QuestionGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow(newQuestion(score = 0))
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    init {
        speakQuestion()
    }

    fun speakQuestion() = speaker.speak(_state.value.question.spoken)

    fun choose(value: Int) = answer(value)

    fun onInputChange(text: String) {
        _state.update { it.copy(input = text.filter(Char::isDigit).take(MAX_DIGITS)) }
    }

    fun submit() {
        _state.value.input.toIntOrNull()?.let(::answer)
    }

    fun skip() {
        _state.value = newQuestion(score = _state.value.score)
        speakQuestion()
    }

    /** A correct answer moves on; a wrong one keeps the question so the child can try again. */
    fun dismissFeedback() {
        val feedback = _state.value.feedback ?: return
        if (feedback.correct) {
            _state.value = newQuestion(score = _state.value.score)
            speakQuestion()
        } else {
            _state.update { it.copy(feedback = null, input = "") }
        }
    }

    private fun answer(value: Int) {
        val state = _state.value
        if (state.feedback != null || value in state.rejected) return
        val question = state.question
        val feedback = if (value == question.answer) {
            Feedback.correct("${question.left} ${question.operation.spoken} ${question.right} is ${question.answer}.")
        } else {
            Feedback.wrong("$value is not the answer.")
        }
        _state.value = state.copy(
            feedback = feedback,
            score = if (feedback.correct) state.score + 1 else state.score,
            rejected = if (feedback.correct) state.rejected else state.rejected + value,
        )
        speaker.speak(feedback.spoken)
    }

    private fun newQuestion(score: Int): QuizUiState {
        val question = generator.question(generator.randomOperation())
        val choices = if (mode == QuizMode.Choices) generator.choices(question) else emptyList()
        return QuizUiState(question = question, choices = choices, score = score)
    }

    private companion object {
        const val MAX_DIGITS = 6
    }
}
