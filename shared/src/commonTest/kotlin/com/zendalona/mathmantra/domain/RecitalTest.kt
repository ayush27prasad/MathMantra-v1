package com.zendalona.mathmantra.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class RecitalTest {
    @Test
    fun acceptsCommonRecogniserFormats() {
        listOf("12 13 14 15", "12, 13, 14, 15", "12-13-14-15", "12131415").forEach {
            assertEquals(RecitalCheck(correct = true, missing = emptyList()), checkRecital(12..15, it), it)
        }
    }

    @Test
    fun reportsMissingNumbers() {
        assertEquals(RecitalCheck(correct = false, missing = listOf(13, 15)), checkRecital(12..15, "12 14"))
    }

    @Test
    fun outOfOrderIsWrongButNothingMissing() {
        assertEquals(RecitalCheck(correct = false, missing = emptyList()), checkRecital(1..3, "3 2 1"))
    }
}
