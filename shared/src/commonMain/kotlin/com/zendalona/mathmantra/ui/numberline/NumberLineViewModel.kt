package com.zendalona.mathmantra.ui.numberline

import androidx.lifecycle.ViewModel
import com.zendalona.mathmantra.domain.Feedback
import com.zendalona.mathmantra.domain.NumberLine
import com.zendalona.mathmantra.domain.Operation
import com.zendalona.mathmantra.domain.QuestionGenerator
import com.zendalona.mathmantra.platform.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** "What is 3 + 7?" — answered by walking 7 steps right from 3. */
data class NumberLineQuestion(val from: Int, val steps: Int, val operation: Operation) {
    val answer: Int get() = if (operation == Operation.Add) from + steps else from - steps
    val text: String get() = "$from ${operation.symbol} $steps"
    val movesRight: Boolean get() = operation == Operation.Add
    val spoken: String
        get() = "You're standing on $from. What is $from ${operation.spoken} $steps? " +
            "Move $steps steps to the ${if (movesRight) "right" else "left"} on the number line."
}

data class NumberLineUiState(
    val line: NumberLine,
    val question: NumberLineQuestion,
    val feedback: Feedback? = null,
)

class NumberLineViewModel(
    private val speaker: Speaker,
    private val generator: QuestionGenerator = QuestionGenerator(),
) : ViewModel() {

    private val _state = MutableStateFlow(NumberLineUiState(line = NumberLine(), question = questionFrom(0)))
    val state: StateFlow<NumberLineUiState> = _state.asStateFlow()

    init {
        speakQuestion()
    }

    fun speakQuestion() = speaker.speak(_state.value.question.spoken, NARRATION_RATE)

    fun stepLeft() = move(NumberLine::stepLeft)

    fun stepRight() = move(NumberLine::stepRight)

    fun dismissFeedback() {
        val state = _state.value
        if (state.feedback == null) return
        _state.value = state.copy(question = questionFrom(state.line.position), feedback = null)
        speakQuestion()
    }

    private fun move(step: (NumberLine) -> NumberLine) {
        val state = _state.value
        if (state.feedback != null) return
        val line = step(state.line)
        val question = state.question
        val feedback = if (line.position == question.answer) {
            Feedback.correct("${question.from} ${question.operation.spoken} ${question.steps} equals ${question.answer}.")
        } else {
            null
        }
        _state.value = state.copy(line = line, feedback = feedback)
        feedback?.let { speaker.speak(it.spoken, NARRATION_RATE) }
    }

    private fun questionFrom(position: Int) = NumberLineQuestion(
        from = position,
        steps = generator.countTarget(),
        operation = if (generator.coinFlip()) Operation.Add else Operation.Subtract,
    )

    private companion object {
        const val NARRATION_RATE = 0.8f
    }
}
