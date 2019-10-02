package com.waes.palazares.scalablewebkotlin.domain

data class DifferenceResult(val type: DifferenceType, val message: String)

enum class DifferenceType {
    EQUALS, DIFFERENT_SIZE, DIFFERENT_CONTENT
}