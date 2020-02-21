package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ScalableWebKotlinApplicationTest(@Autowired val webClient: WebTestClient) {
    @LocalServerPort
    private var localPort = 0

    private final val testId = "testId"
    private final val testContent = "testContent"
    private final val url = "http://localhost:$localPort/v1/diff"
    private final val rightUrl = "$url/$testId/right"
    private final val leftUrl = "$url/$testId/left"
    private final val diffUrl = "$url/$testId"
    private final val request = Base64.getEncoder().encodeToString(testContent.toByteArray())

    @Test
    fun shouldReturnRecordWhenPutLeft() {
        //given
        //when
        val rec: DifferenceRecord? = webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody

        //then
        assertEquals(testId, rec!!.id)
        assertArrayEquals(testContent.toByteArray(), rec.left)
    }

    @Test
    fun shouldReturnRecordWhenPutRight() {
        //given
        //when
        val rec: DifferenceRecord? = webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody

        //then
        assertEquals(testId, rec!!.id)
        assertArrayEquals(testContent.toByteArray(), rec.right)
    }

    @Test
    fun shouldReturnDifferenceWhenPutRightAndLeft() {
        //given
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        val rec: DifferenceRecord? = webClient.get()
                .uri(diffUrl)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody

        //then
        assertEquals(DifferenceType.EQUALS, rec!!.result!!.type)
        assertNotNull(rec.result!!.message)
    }

    @Test
    fun shouldReturnEqualDifferenceWhenMultiplePutRightAndLeft() {
        //given
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        val rec: DifferenceRecord? = webClient.get()
                .uri(diffUrl)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody

        //then
        assertEquals(DifferenceType.EQUALS, rec!!.result!!.type)
        assertNotNull(rec.result!!.message)
    }

    @Test
    fun shouldReturnSizeDifferenceWhenMultiplePutRightAndLeft() {
        //given
        val leftRequest = Base64.getEncoder().encodeToString("leftContent".toByteArray())
        val rightRequest = Base64.getEncoder().encodeToString("rightContent".toByteArray())
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(rightRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(leftRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(rightRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(leftRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
        val rec: DifferenceRecord? = webClient.get()
                .uri(diffUrl)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody
        //then
        assertEquals(DifferenceType.DIFFERENT_SIZE, rec!!.result!!.type)
        assertNotNull(rec.result)
    }

    @Test
    fun shouldReturnContentDifferenceWhenMultiplePutRightAndLeft() {
        //given
        val leftRequest = Base64.getEncoder().encodeToString("rightSAMEPARTright".toByteArray())
        val rightRequest = Base64.getEncoder().encodeToString("lleftSAMEPARTlleft".toByteArray())
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(rightRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(leftRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(rightRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(leftRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        val rec: DifferenceRecord? = webClient.get()
                .uri(diffUrl)
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
                .returnResult()
                .responseBody
        //then
        assertEquals(DifferenceType.DIFFERENT_CONTENT, rec!!.result!!.type)
        assertNotNull(rec.result)
        assertTrue(rec.result!!.message.contains("(0, 4)"))
        assertTrue(rec.result!!.message.contains("(13, 4)"))
    }

    @Test
    fun shouldReturnBadRequestWhenEmptyLeftContent() {
        //given
        //when
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(""))
                .exchange()
                .expectStatus().isBadRequest
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Any::class.java)
    }

    @Test
    fun shouldReturnBadRequestWhenEmptyRightContent() {
        //given
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(""))
                .exchange()
                .expectStatus().isBadRequest
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Any::class.java)
    }

    @Test
    fun shouldReturnNotFoundWhenWrongId() {
        //given
        val leftRequest = Base64.getEncoder().encodeToString("leftContent".toByteArray())
        val rightRequest = Base64.getEncoder().encodeToString("rightContent".toByteArray())
        //when
        webClient.put()
                .uri(rightUrl)
                .body(BodyInserters.fromValue(leftRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(rightRequest))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.get()
                .uri(diffUrl + 1)
                .exchange()
                .expectStatus().isNotFound
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Any::class.java)
    }

    @Test
    fun shouldReturnNotFoundWhenOnlyOnePartExists() {
        //given
        //when
        webClient.put()
                .uri(leftUrl)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(DifferenceRecord::class.java)
        webClient.get()
                .uri(diffUrl)
                .exchange()
                .expectStatus().isNotFound
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(Any::class.java)
                .returnResult()
        //then
    }
}
