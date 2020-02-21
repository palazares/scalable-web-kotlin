package com.waes.palazares.scalablewebkotlin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class OffsetsTest {

    @Test
    fun shouldThrowUnsupportedOperationWhenRightIsEmpty() {
        assertThrows(IllegalArgumentException::class.java) { getOffsetsMessage(ByteArray(0), ByteArray(1)) }
    }

    @Test
    fun shouldThrowUnsupportedOperationWhenLeftIsEmpty() {
        assertThrows(IllegalArgumentException::class.java) { getOffsetsMessage(ByteArray(1), ByteArray(0)) }
    }

    @Test
    fun shouldThrowUnsupportedOperationWhenDifferentSize() {
        assertThrows(IllegalArgumentException::class.java) { getOffsetsMessage(ByteArray(1), ByteArray(2)) }
    }

    @ParameterizedTest(name = "{0}, {1} = {2}")
    @CsvSource(
            "equals-equals-Arrays are equals",
            "12as34as56-12er34er56-Offsets [(index,length),..] : [(2, 2),(6, 2)]",
            "12as34as56as-12er34er56er-Offsets [(index,length),..] : [(2, 2),(6, 2),(10, 2)]",
            "e1-s1-Offsets [(index,length),..] : [(0, 1)]",
            "e1e-s1s-Offsets [(index,length),..] : [(0, 1),(2, 1)]",
            "1e-1s-Offsets [(index,length),..] : [(1, 1)]",
            "1ee-1ss-Offsets [(index,length),..] : [(1, 2)]",
            "123456-abcdef-Offsets [(index,length),..] : [(0, 6)]",
            "12asas56-12erer56-Offsets [(index,length),..] : [(2, 4)]", delimiter = '-'
    )
    fun shouldReturnResultWhenArraysAreSpecified(left: String, right: String, result: String) {
        //given
        val leftContent = left.toByteArray()
        val rightContent = right.toByteArray()
        //when
        val offsetsMessage = getOffsetsMessage(leftContent, rightContent)
        //then
        assertEquals(result, offsetsMessage)
    }
}