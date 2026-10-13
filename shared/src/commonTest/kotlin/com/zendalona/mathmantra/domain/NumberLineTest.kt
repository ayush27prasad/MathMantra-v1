package com.zendalona.mathmantra.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberLineTest {
    @Test
    fun stepsWithinTheWindow() {
        assertEquals(NumberLine(-5, 5, 1), NumberLine().stepRight())
        assertEquals(NumberLine(-5, 5, -1), NumberLine().stepLeft())
    }

    @Test
    fun walkingOffTheRightEdgeOpensTheNextWindow() {
        assertEquals(NumberLine(6, 16, 6), NumberLine(-5, 5, 5).stepRight())
    }

    @Test
    fun walkingOffTheLeftEdgeOpensThePreviousWindow() {
        assertEquals(NumberLine(-16, -6, -6), NumberLine(-5, 5, -5).stepLeft())
    }

    @Test
    fun positionIsAlwaysOneStepAway() {
        var line = NumberLine()
        repeat(40) { line = line.stepRight() }
        assertEquals(40, line.position)
        repeat(80) { line = line.stepLeft() }
        assertEquals(-40, line.position)
    }
}
