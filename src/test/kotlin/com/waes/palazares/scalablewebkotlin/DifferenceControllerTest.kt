package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.*

@RunWith(SpringRunner::class)
@WebFluxTest(controllers = [DifferenceController::class])
class DifferenceControllerTest {
    @Autowired
    lateinit var client: WebTestClient
    @MockBean
    lateinit var differenceService: DifferenceService

    @Test
    fun shouldCallServiceWhenPutLeft() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val testRecord = Mono.just(DifferenceRecord(id = testId, left = testContent.toByteArray()))
        //when
        `when`(differenceService.putLeft(testId, testContent)).thenReturn(testRecord)
        //then
        client.put()
                .uri("/v1/diff/$testId/left")
                .syncBody(testContent)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("id").isEqualTo(testId)
                .jsonPath("left").isEqualTo(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        verify(differenceService, times(1)).putLeft(testId, testContent)
    }

    @Test
    fun shouldCallServiceWhenPutRight() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val testRecord = Mono.just(DifferenceRecord(id = testId, right = testContent.toByteArray()))
        //when
        `when`(differenceService.putRight(testId, testContent)).thenReturn(testRecord)
        //then
        client
                .put()
                .uri("/v1/diff/$testId/right")
                .syncBody(testContent)
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("id").isEqualTo(testId)
                .jsonPath("right").isEqualTo(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        verify(differenceService, times(1)).putRight(testId, testContent)
    }

    @Test
    fun shouldCallServiceWhenGetDifference() {
        //given
        val testId = "testId"
        val testMessage = "testEquals"
        val testRecord = Mono.just(DifferenceRecord(id = testId, result = DifferenceResult(DifferenceType.EQUALS, testMessage)))
        //when
        `when`(differenceService.getDifference(testId)).thenReturn(testRecord)
        //then
        client
                .get()
                .uri("/v1/diff/$testId")
                .exchange()
                .expectStatus().isOk
                .expectBody().jsonPath("type").isEqualTo(DifferenceType.EQUALS.toString())
                .jsonPath("message").isEqualTo(testMessage)

        verify(differenceService, times(1)).getDifference(testId)
    }
}