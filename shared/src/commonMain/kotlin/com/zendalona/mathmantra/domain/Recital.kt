package com.zendalona.mathmantra.domain

/** The result of checking what a child said against the range they were asked to count. */
data class RecitalCheck(val correct: Boolean, val missing: List<Int>)

/**
 * Speech recognisers return counting as "12 13 14", "12, 13, 14" or "121314". Comparing only the
 * digits accepts all of those; [RecitalCheck.missing] lists numbers that weren't heard as separate words.
 */
fun checkRecital(expected: IntRange, spoken: String): RecitalCheck {
    val expectedDigits = expected.joinToString("")
    val correct = spoken.filter(Char::isDigit) == expectedDigits
    if (correct) return RecitalCheck(correct = true, missing = emptyList())

    val heard = Regex("\\d+").findAll(spoken).mapNotNull { it.value.toIntOrNull() }.toSet()
    return RecitalCheck(correct = false, missing = expected.filterNot { it in heard })
}
