package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import com.waes.palazares.scalablewebkotlin.exceptions.InavlidIdException
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidBase64Exception
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidRecordContentException
import org.slf4j.Logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
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
     * @param [id] document id
     * @param [doc] base64 encoded document
     * @return persisted difference record
     */
    fun putLeft(id: String, doc: String): Mono<DifferenceRecord>

    /**
     * Gets the difference between left and right documents
     *
     * @param [id] document id
     * @return difference record with a result
     */
    fun getDifference(id: String): Mono<DifferenceRecord>
}

@Service
class DifferenceServiceImpl(private val repository: DifferenceRepository) : DifferenceService, Logging {
    private val log: Logger = logger()

    override fun putRight(id: String, doc: String): Mono<DifferenceRecord> {
        return putRecord(id, doc, false)
    }

    override fun putLeft(id: String, doc: String): Mono<DifferenceRecord> {
        return putRecord(id, doc, true)
    }

    /**
     * Checks that record already has a result and returns it without further processing if it already exists
     *
     * @param [id] document id
     * @return difference result
     */
    override fun getDifference(id: String): Mono<DifferenceRecord> {
        log.debug("Get difference request with id: $id")

        if (id.trim { it <= ' ' }.isEmpty()) {
            log.debug("Get difference request has empty id")
            return Mono.error(InavlidIdException())
        }

        val record = repository.findById(id).switchIfEmpty(Mono.error(InvalidRecordContentException()))
        val yesResultRecord = record.filter { it.result != null }

        return yesResultRecord.switchIfEmpty(record
                .flatMap { rec -> compare(rec).map { DifferenceRecord(rec.id, rec.left, rec.right, it) } }
                .flatMap { repository.save(it) })
    }

    /**
     * Nullify result only if new content is different from already existing. Otherwise old object is returned and no actual persistence performed
     *
     * @param [id] document id
     * @param [doc] base64 encoded document
     * @param [isLeft] document side
     * @return persisted difference record
     */
    private fun putRecord(id: String?, doc: String?, isLeft: Boolean): Mono<DifferenceRecord> {
        log.debug("Put record request with id: $id")

        if (id == null || id.trim { it <= ' ' }.isEmpty()) {
            log.debug("Record request has empty id")
            return Mono.error(InavlidIdException())
        }

        if (doc == null || doc.trim { it <= ' ' }.isEmpty()) {
            log.debug("Record request with id: $id has empty content")
            return Mono.error(InvalidBase64Exception())
        }

        val decodedDoc = decode(doc)
        val record = repository.findById(id).defaultIfEmpty(DifferenceRecord(id))

        val sameDocRecord = decodedDoc.flatMap { content -> record.filter { rec -> Arrays.equals(if (isLeft) rec.left else rec.right, content) } }

        return sameDocRecord.switchIfEmpty(decodedDoc
                .flatMap { content ->
                    record.map { rec -> if (isLeft) DifferenceRecord(rec.id, content, rec.right, null) else DifferenceRecord(rec.id, rec.left, content, null) }
                }
                .flatMap { repository.save(it) })
    }

    private fun decode(doc: String): Mono<ByteArray> =
            Mono.just(Base64.getDecoder().decode(doc)).onErrorMap {
                //log.debug("Not valid base64 string: $doc", it)
                InvalidBase64Exception()
            }

    private fun compare(record: DifferenceRecord): Mono<DifferenceResult> {
        val id = record.id

        if (record.left == null || record.right == null || record.left.isEmpty() || record.right.isEmpty()) {
            log.debug("Record with id: $id doesn't have full date for comparison")
            return Mono.error(InvalidRecordContentException())
        }

        val left = record.left
        val right = record.right

        if (Arrays.equals(left, right)) {
            log.debug("Record with id: $id has equal content")
            return Mono.just(DifferenceResult(DifferenceType.EQUALS, "Records are equal. Congratulations!"))
        }

        if (left.size != right.size) {
            log.debug("Record with id: $id has different size content")
            return Mono.just(DifferenceResult(DifferenceType.DIFFERENT_SIZE, "Records have different size. What a pity!"))
        }

        log.debug("Record with id: $id has different content")
        val offsetsMessage = getOffsetsMessage(left, right)
        return Mono.just(DifferenceResult(DifferenceType.DIFFERENT_CONTENT, "Records have the same size, but content is different. Differences insight: $offsetsMessage"))
    }
}