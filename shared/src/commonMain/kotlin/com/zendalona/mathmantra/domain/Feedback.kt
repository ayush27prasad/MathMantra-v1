package com.zendalona.mathmantra.domain

import kotlin.random.Random

/** What the result dialog shows and says after an answer. */
data class Feedback(val correct: Boolean, val title: String, val detail: String? = null) {
    val spoken: String get() = listOfNotNull(title, detail).joinToString(" ")

    companion object {
        private val praise = listOf("Awesome!", "Great job!", "Well done!", "Correct!", "Bravo!")
        private val encouragement = listOf("Try again!", "Not quite!", "Almost there!", "Let's try once more!")

        fun correct(detail: String? = null, random: Random = Random.Default) =
            Feedback(correct = true, title = praise.random(random), detail = detail)

        fun wrong(detail: String? = null, random: Random = Random.Default) =
            Feedback(correct = false, title = encouragement.random(random), detail = detail)
    }
}
