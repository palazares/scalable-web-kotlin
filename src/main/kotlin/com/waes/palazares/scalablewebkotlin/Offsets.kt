package com.waes.palazares.scalablewebkotlin

import java.util.*

/**
 * Method makes size checks and throws UnsupportedOperationException if one of the arrays is empty or they have different sizes.
 * Arguments checked for null
 *
 * @param left first byte array
 * @param right second byte array
 * @return message containing list of differences having starting index and length for each difference subset - [(index,length),..]
 */
fun getOffsetsMessage(left: ByteArray, right: ByteArray): String {
    if (left.isEmpty() || right.isEmpty() || left.size != right.size) {
        throw UnsupportedOperationException("Can't compare offsets with zero or different length")
    }

    if (left.contentEquals(right)) {
        return "Arrays are equals"
    }

    val offsets = ArrayList<Offset>()
    var startIndex = 0
    var curLength = if (left[0] == right[0]) 0 else 1

    for (i in 1 until left.size) {
        if (left[i] != right[i]) {
            if (left[i - 1] == right[i - 1]) {
                startIndex = i
            }
            curLength++
        } else {
            if (left[i - 1] != right[i - 1]) {
                offsets.add(Offset(startIndex, curLength))
                curLength = 0
                startIndex = 0
            }
        }//left[i] == right[i]
    }

    if (curLength != 0) {
        offsets.add(Offset(startIndex, curLength))
    }

    return "Offsets [(index,length),..] : [" + offsets.joinToString(",") { it.toString() } + "]"
}

private data class Offset(val startIndex: Int = 0, val length: Int = 0) {
    override fun toString(): String {
        return "(" + startIndex +
                ", " + length + ')'.toString()
    }
}