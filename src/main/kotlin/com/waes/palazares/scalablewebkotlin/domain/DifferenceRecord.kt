package com.waes.palazares.scalablewebkotlin.domain

import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "difference")
data class DifferenceRecord(val id: String, val left: ByteArray? = null, val right: ByteArray? = null, val result: DifferenceResult? = null) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DifferenceRecord

        if (id != other.id) return false
        if (left != null) {
            if (other.left == null) return false
            if (!left.contentEquals(other.left)) return false
        } else if (other.left != null) return false
        if (right != null) {
            if (other.right == null) return false
            if (!right.contentEquals(other.right)) return false
        } else if (other.right != null) return false
        if (result != other.result) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = id.hashCode()
        result1 = 31 * result1 + (left?.contentHashCode() ?: 0)
        result1 = 31 * result1 + (right?.contentHashCode() ?: 0)
        result1 = 31 * result1 + (result?.hashCode() ?: 0)
        return result1
    }
}