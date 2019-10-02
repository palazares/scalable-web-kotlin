package com.waes.palazares.scalablewebkotlin

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class OffsetsTest {

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowUnsupportedOperationWhenRightIsEmpty() {
        getOffsetsMessage(ByteArray(0), ByteArray(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowUnsupportedOperationWhenLeftIsEmpty() {
        getOffsetsMessage(ByteArray(1), ByteArray(0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun shouldThrowUnsupportedOperationWhenDifferentSize() {
        getOffsetsMessage(ByteArray(1), ByteArray(2))
    }

    @Test
    @Parameters(method = "parametersToTestOffsets")
    fun shouldReturnResultWhenArraysAreSpecified(left: String, right: String, result: String) {
        //given
        val leftContent = left.toByteArray()
        val rightContent = right.toByteArray()
        //when
        val offsetsMessage = getOffsetsMessage(leftContent, rightContent)
        //then
        assertEquals(result, offsetsMessage)
    }

    private fun parametersToTestOffsets(): Array<Any> {
        return arrayOf(
                arrayOf<Any>("equals", "equals", "Arrays are equals"),
                arrayOf<Any>("12as34as56", "12er34er56", "Offsets [(index,length),..] : [(2, 2),(6, 2)]"),
                arrayOf<Any>("12as34as56as", "12er34er56er", "Offsets [(index,length),..] : [(2, 2),(6, 2),(10, 2)]"),
                arrayOf<Any>("e1", "s1", "Offsets [(index,length),..] : [(0, 1)]"),
                arrayOf<Any>("e1e", "s1s", "Offsets [(index,length),..] : [(0, 1),(2, 1)]"),
                arrayOf<Any>("1e", "1s", "Offsets [(index,length),..] : [(1, 1)]"),
                arrayOf<Any>("1ee", "1ss", "Offsets [(index,length),..] : [(1, 2)]"),
                arrayOf<Any>("123456", "abcdef", "Offsets [(index,length),..] : [(0, 6)]"),
                arrayOf<Any>("12asas56", "12erer56", "Offsets [(index,length),..] : [(2, 4)]"))
    }
}