package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

@Controller
class DifferenceController(private val service: DifferenceService) {
    /**
     * Endpoint to add the left side of the document.
     *
     * @param id id of the document
     * @param data base64 encoded content
     * @return Difference record persisted into storage
     */
    @PutMapping("{id}/left")
    fun putLeft(@PathVariable id: String, @RequestBody data: String): Mono<DifferenceRecord> {
        return service.putLeft(id, data)
    }

    /**
     * Endpoint to add the right side of the document.
     *
     * @param id id of the document
     * @param data base64 encoded content
     * @return Difference record persisted into storage
     */
    @PutMapping("{id}/right")
    fun putRight(@PathVariable id: String, @RequestBody data: String): Mono<DifferenceRecord> {
        return service.putRight(id, data)
    }

    /**
     * Endpoint to get the differences between left and right documents
     *
     * @param id id of the document
     * @return Difference result object with difference type and message
     */
    @GetMapping("{id}")
    fun getDifference(@PathVariable id: String): Mono<DifferenceResult> {
        return service.getDifference(id).map { it.result }
    }
}