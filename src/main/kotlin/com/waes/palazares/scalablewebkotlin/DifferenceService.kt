package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import reactor.core.publisher.Mono
import java.lang.System.getLogger
import java.util.*

/**
 * `DifferenceService` interface defines methods to store left and right documents and for getting difference between them
 */
interface DifferenceService {
    /**
     * Puts document as a right side of the difference into the repository. Document contents are decoded
     *
     * @param [id] document id
     * @param [doc] base64 encoded document
     * @return persisted difference record
     */
    fun putRight(id: String, doc: String): Mono<DifferenceRecord>

    /**
     * Puts document as a left side of the difference into the repository. Document contents are decoded
     *
     * @param id document id
     * @param doc base64 encoded document
     * @return persisted difference record
     */
    fun putLeft(id: String, doc: String): Mono<DifferenceRecord>

    /**
     * Gets the difference between left and right documents
     *
     * @param id document id
     * @return difference record with a result
     */
    fun getDifference(id: String): Mono<DifferenceRecord>
}

class DifferenceServiceImpl : DifferenceService {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        @JvmStatic
        private val log = getLogger(javaClass.enclosingClass)
    }

    private val repository: DifferenceRepository? = null

    override fun putRight(id: String, doc: String): Mono<DifferenceRecord> {
        return putRecord(id, doc, false)
    }

    override fun putLeft(id: String, doc: String): Mono<DifferenceRecord> {
        return putRecord(id, doc, true)
    }

    /**
     * Checks that record already has a result and returns it without further processing if it already exists
     *
     * @param id document id
     * @return difference result
     */
    override fun getDifference(id: String?): Mono<DifferenceRecord> {
        log.debug("Get difference request with id: {}", id)

        if (id == null || id.trim { it <= ' ' }.isEmpty()) {
            log.debug("Get difference request has empty id")
            return Mono.error(InavlidIdException())
        }

        val record = repository!!.findById(id).switchIfEmpty(Mono.error(InvalidRecordContentException()))
        val yesResultRecord = record.filter { it.result != null }

        return yesResultRecord
                .switchIfEmpty(record
                        .flatMap { rec -> compare(rec).map<R> { x -> rec.toBuilder().result(x).build() } }
                        .flatMap(???({ repository!!.save() })))
    }

    /**
     * Nullify result only if new content is different from already existing. Otherwise old object is returned and no actual persistence performed
     *
     * @param id document id
     * @param doc base64 encoded document
     * @param isLeft document side
     * @return persisted difference record
     */
    private fun putRecord(id: String?, doc: String?, isLeft: Boolean): Mono<DifferenceRecord> {
        log.debug("Put record request with id: {}", id)

        if (id == null || id.trim { it <= ' ' }.isEmpty()) {
            log.debug("Record request has empty id")
            return Mono.error(InavlidIdException())
        }

        if (doc == null || doc.trim { it <= ' ' }.isEmpty()) {
            log.debug("Record request with id: {} has empty content", id)
            return Mono.error(InvalidBase64Exception())
        }

        val decodedDoc = decode(doc)
        val record = repository!!.findById(id).defaultIfEmpty(DifferenceRecord.builder().id(id).build())

        val sameDocRecord = decodedDoc.flatMap<Any> { d -> record.filter({ rec -> if (isLeft) Arrays.equals(rec.getLeft(), d) else Arrays.equals(rec.getRight(), d) }) }

        return sameDocRecord.switchIfEmpty(
                decodedDoc.flatMap<Any> { d ->
                    record
                            .map({ rec -> if (isLeft) rec.toBuilder().left(d).build() else rec.toBuilder().right(d).build() })
                }
                        .map<Any> { rec -> rec.toBuilder().result(null).build() }
                        .flatMap(Function<Any, Mono<*>> { repository!!.save() }))
    }

    private fun decode(doc: String): Mono<ByteArray> {
        return try {
            Mono.just(Base64.getDecoder().decode(doc))
        } catch (e: IllegalArgumentException) {
            log.debug("Not valid base64 string: {}", doc, e)
            Mono.error(InvalidBase64Exception())
        }

    }

    private fun compare(record: DifferenceRecord): Mono<DifferenceResult> {
        if (record.left == null || record.right == null || record.left.isEmpty() || record.right.isEmpty()) {
            log.debug("Record with id: {} doesn't have full date for comparison", record.getId())
            return Mono.error(InvalidRecordContentException())
        }

        val left = record.left
        val right = record.right

        if (Arrays.equals(left, right)) {
            log.debug("Record with id: {} has equal content", record.id)
            return Mono.just(DifferenceResult.builder().type(DifferenceType.EQUALS).message("Records are equal. Congratulations!").build())
        }

        if (left.size !== right.size) {
            log.debug("Record with id: {} has different size content", record.id)
            return Mono.just(DifferenceResult.builder().type(DifferenceType.DIFFERENT_SIZE).message("Records have different size. What a pity!").build())
        }

        log.debug("Record with id: {} has different content", record.id)
        return Mono.just(DifferenceResult.builder()
                .type(DifferenceType.DIFFERENT_CONTENT)
                .message("Records have the same size, but content is different. Differences insight: " + Offsets.getOffsetsMessage(left, right))
                .build())
    }
}