package com.zendalona.mathmantra.domain

import kotlin.random.Random

enum class Difficulty { Easy, Medium, Hard }

enum class Operation(val symbol: String, val spoken: String) {
    Add("+", "plus"),
    Subtract("−", "minus"),
    Multiply("×", "times"),
    Divide("÷", "divided by"),
}

data class Question(val left: Int, val right: Int, val operation: Operation, val answer: Int) {
    /** Visual form, e.g. `7 × 3`. */
    val text: String get() = "$left ${operation.symbol} $right"

    /** Form read aloud by text-to-speech, e.g. `What is 7 times 3?`. */
    val spoken: String get() = "What is $left ${operation.spoken} $right?"
}

/** Every random number the games need. Seed [random] in tests for reproducible output. */
class QuestionGenerator(private val random: Random = Random.Default) {

    fun randomOperation(): Operation = Operation.entries.random(random)

    fun question(operation: Operation, difficulty: Difficulty = Difficulty.Easy): Question = when (operation) {
        Operation.Add -> {
            val (a, b) = pair(difficulty.additiveRange)
            Question(a, b, operation, a + b)
        }
        Operation.Subtract -> {
            val (a, b) = pair(difficulty.additiveRange)
            Question(maxOf(a, b), minOf(a, b), operation, maxOf(a, b) - minOf(a, b))
        }
        Operation.Multiply -> {
            val (a, b) = pair(difficulty.multiplicativeRange)
            Question(a, b, operation, a * b)
        }
        Operation.Divide -> {
            // Built from a product so the answer is always a whole number and the divisor is never zero.
            val divisor = random.nextInt(1, difficulty.multiplicativeRange.last)
            val quotient = difficulty.multiplicativeRange.random(random)
            Question(divisor * quotient, divisor, operation, quotient)
        }
    }

    /** Four distinct, non-negative choices containing [Question.answer], in random order. */
    fun choices(question: Question, count: Int = 4): List<Int> {
        val answer = question.answer
        val spread = maxOf(5, answer / 5)
        val distractors = mutableSetOf<Int>()
        while (distractors.size < count - 1) {
            val candidate = answer + random.nextInt(-spread, spread + 1)
            if (candidate >= 0 && candidate != answer) distractors += candidate
        }
        return (distractors + answer).shuffled(random)
    }

    /** How many times to ring the bell / tap the drum, or how far to walk on the number line. */
    fun countTarget(): Int = random.nextInt(2, 18)

    /** A run of 7–17 consecutive numbers starting below [upperBound] − 10, e.g. `12..20`. */
    fun countingRange(upperBound: Int = 50): IntRange {
        val start = random.nextInt(upperBound - 10)
        return start..start + random.nextInt(6, 17)
    }

    fun coinFlip(): Boolean = random.nextBoolean()

    private fun pair(range: IntRange) = range.random(random) to range.random(random)

    private val Difficulty.additiveRange
        get() = when (this) {
            Difficulty.Easy -> 1..10
            Difficulty.Medium -> 10..59
            Difficulty.Hard -> 17..516
        }

    private val Difficulty.multiplicativeRange
        get() = when (this) {
            Difficulty.Easy -> 1..10
            Difficulty.Medium -> 1..20
            Difficulty.Hard -> 1..50
        }
}
