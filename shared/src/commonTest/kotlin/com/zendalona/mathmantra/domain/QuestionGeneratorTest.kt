package com.zendalona.mathmantra.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuestionGeneratorTest {
    private val generator = QuestionGenerator(Random(42))

    @Test
    fun answersAreArithmeticallyCorrect() = repeatForAll { operation, difficulty ->
        val q = generator.question(operation, difficulty)
        val expected = when (operation) {
            Operation.Add -> q.left + q.right
            Operation.Subtract -> q.left - q.right
            Operation.Multiply -> q.left * q.right
            Operation.Divide -> q.left / q.right
        }
        assertEquals(expected, q.answer, "$q")
    }

    @Test
    fun subtractionNeverGoesNegative() = repeatForAll(Operation.Subtract) { _, difficulty ->
        assertTrue(generator.question(Operation.Subtract, difficulty).answer >= 0)
    }

    @Test
    fun divisionIsAlwaysExact() = repeatForAll(Operation.Divide) { _, difficulty ->
        val q = generator.question(Operation.Divide, difficulty)
        assertTrue(q.right > 0)
        assertEquals(0, q.left % q.right, "$q")
    }

    @Test
    fun choicesAreFourDistinctNonNegativeValuesIncludingTheAnswer() = repeatForAll { operation, difficulty ->
        val q = generator.question(operation, difficulty)
        val choices = generator.choices(q)
        assertEquals(4, choices.size)
        assertEquals(4, choices.toSet().size, "duplicates in $choices")
        assertTrue(q.answer in choices)
        assertTrue(choices.all { it >= 0 })
    }

    @Test
    fun countTargetIsBetweenTwoAndSeventeen() = repeat(500) {
        assertTrue(generator.countTarget() in 2..17)
    }

    @Test
    fun countingRangeHasSevenToSeventeenNumbers() = repeat(500) {
        val range = generator.countingRange(upperBound = 50)
        assertTrue(range.first in 0 until 40)
        assertTrue(range.count() in 7..17, "$range")
    }

    @Test
    fun spokenFormUsesWords() {
        assertEquals("What is 7 times 3?", Question(7, 3, Operation.Multiply, 21).spoken)
        assertEquals("7 × 3", Question(7, 3, Operation.Multiply, 21).text)
    }

    private fun repeatForAll(
        vararg operations: Operation = Operation.entries.toTypedArray(),
        block: (Operation, Difficulty) -> Unit,
    ) = operations.forEach { op -> Difficulty.entries.forEach { d -> repeat(200) { block(op, d) } } }
}
