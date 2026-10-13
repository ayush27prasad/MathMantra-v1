package com.zendalona.mathmantra.ui

import com.zendalona.mathmantra.domain.QuestionGenerator
import com.zendalona.mathmantra.platform.Sound
import com.zendalona.mathmantra.platform.SoundPlayer
import com.zendalona.mathmantra.platform.Speaker
import com.zendalona.mathmantra.ui.counting.CountingGameViewModel
import com.zendalona.mathmantra.ui.counting.Instrument
import com.zendalona.mathmantra.ui.numberline.NumberLineViewModel
import com.zendalona.mathmantra.ui.quiz.QuizMode
import com.zendalona.mathmantra.ui.quiz.QuizViewModel
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class RecordingSpeaker : Speaker {
    val spoken = mutableListOf<String>()
    override fun speak(text: String, rate: Float) { spoken += text }
    override fun stop() = Unit
}

private class RecordingSounds : SoundPlayer {
    val played = mutableListOf<Sound>()
    override fun play(sound: Sound) { played += sound }
}

class ViewModelTest {
    @Test
    fun countingGameSucceedsExactlyAtTheTarget() {
        val speaker = RecordingSpeaker()
        val sounds = RecordingSounds()
        val vm = CountingGameViewModel(Instrument.Drum, speaker, sounds, QuestionGenerator(Random(1)))
        val target = vm.state.value.target
        assertEquals("Tap the drum $target times", speaker.spoken.single())

        repeat(target - 1) { vm.hit() }
        assertNull(vm.state.value.feedback)
        vm.hit()
        assertTrue(vm.state.value.feedback!!.correct)
        assertEquals(target, sounds.played.size)

        vm.hit() // ignored while the dialog is up
        assertEquals(target, vm.state.value.count)

        vm.nextRound()
        assertEquals(0, vm.state.value.count)
        assertNull(vm.state.value.feedback)
    }

    @Test
    fun quizKeepsTheQuestionAfterAWrongAnswer() {
        val vm = QuizViewModel(QuizMode.Choices, RecordingSpeaker(), QuestionGenerator(Random(7)))
        val question = vm.state.value.question
        val wrong = vm.state.value.choices.first { it != question.answer }

        vm.choose(wrong)
        assertEquals(false, vm.state.value.feedback?.correct)
        vm.dismissFeedback()
        assertEquals(question, vm.state.value.question)
        assertTrue(wrong in vm.state.value.rejected)

        vm.choose(question.answer)
        assertEquals(1, vm.state.value.score)
        vm.dismissFeedback()
        assertTrue(vm.state.value.rejected.isEmpty())
    }

    @Test
    fun typedQuizIgnoresNonDigitsAndEmptySubmit() {
        val vm = QuizViewModel(QuizMode.Typed, RecordingSpeaker(), QuestionGenerator(Random(3)))
        vm.submit()
        assertNull(vm.state.value.feedback)
        vm.onInputChange("4a2-")
        assertEquals("42", vm.state.value.input)
    }

    @Test
    fun numberLineCelebratesWhenTheWalkerReachesTheAnswer() {
        val vm = NumberLineViewModel(RecordingSpeaker(), QuestionGenerator(Random(5)))
        val question = vm.state.value.question
        repeat(question.steps) { if (question.movesRight) vm.stepRight() else vm.stepLeft() }
        assertEquals(question.answer, vm.state.value.line.position)
        assertTrue(vm.state.value.feedback!!.correct)

        vm.dismissFeedback()
        assertEquals(question.answer, vm.state.value.question.from)
    }
}
