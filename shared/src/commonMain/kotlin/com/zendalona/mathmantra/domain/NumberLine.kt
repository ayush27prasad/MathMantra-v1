package com.zendalona.mathmantra.domain

/**
 * A window of eleven numbers on an infinite number line. Stepping past either edge slides the
 * window over so the walker lands on the first number of the next page.
 */
data class NumberLine(val start: Int = -5, val end: Int = 5, val position: Int = 0) {

    init {
        require(position in start..end) { "position $position outside $start..$end" }
    }

    val numbers: IntRange get() = start..end

    fun stepRight(): NumberLine =
        if (position < end) copy(position = position + 1)
        else NumberLine(start = end + 1, end = end + 1 + WIDTH, position = end + 1)

    fun stepLeft(): NumberLine =
        if (position > start) copy(position = position - 1)
        else NumberLine(start = start - 1 - WIDTH, end = start - 1, position = start - 1)

    private companion object {
        const val WIDTH = 10
    }
}
