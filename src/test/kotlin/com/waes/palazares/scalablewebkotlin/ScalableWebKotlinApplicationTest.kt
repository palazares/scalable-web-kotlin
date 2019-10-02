package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ScalableWebKotlinApplicationTest {
    @LocalServerPort
    private var localPort: Int = 0

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun contextLoads() {
    }

    @Test
    fun shouldReturnRecordWhenPutLeft() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val url = "http://localhost:$localPort/v1/diff/$testId/left"
        val request = HttpEntity(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        //when
        val putResponse = testRestTemplate.exchange(url, HttpMethod.PUT, request, DifferenceRecord::class.java)
        //then
        assertNotNull(putResponse)
        assertEquals(HttpStatus.OK, putResponse.statusCode)
        assertNotNull(putResponse.body)
        assertEquals(testId, putResponse.body!!.id)
        assertArrayEquals(testContent.toByteArray(), putResponse.body!!.left)
    }

    @Test
    fun shouldReturnRecordWhenPutRight() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val url = "http://localhost:$localPort/v1/diff/$testId/right"
        val request = HttpEntity(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        //when
        val putResponse = testRestTemplate.exchange(url, HttpMethod.PUT, request, DifferenceRecord::class.java)
        //then
        assertNotNull(putResponse)
        assertEquals(HttpStatus.OK, putResponse.statusCode)
        assertNotNull(putResponse.body)
        assertEquals(testId, putResponse.body!!.id)
        assertArrayEquals(testContent.toByteArray(), putResponse.body!!.right)
    }

    @Test
    fun shouldReturnDifferenceWhenPutRightAndLeft() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val rightUrl = "http://localhost:$localPort/v1/diff/$testId/right"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:$localPort/v1/diff/$testId"
        val request = HttpEntity(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        //when
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, DifferenceResult::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.OK, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
        assertEquals(DifferenceType.EQUALS, differenceResponse.body!!.type)
        assertNotNull(differenceResponse.body!!.message)
    }

    @Test
    fun shouldReturnEqualDifferenceWhenMultiplePutRightAndLeft() {
        //given
        val testId = "testId"
        val testContent = "testContent"
        val rightUrl = "http://localhost:$localPort/v1/diff/$testId/right"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:$localPort/v1/diff/$testId"
        val request = HttpEntity(Base64.getEncoder().encodeToString(testContent.toByteArray()))
        //when
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, request, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, DifferenceResult::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.OK, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
        assertEquals(DifferenceType.EQUALS, differenceResponse.body!!.type)
        assertNotNull(differenceResponse.body!!.message)
    }

    @Test
    fun shouldReturnSizeDifferenceWhenMultiplePutRightAndLeft() {
        //given
        val testId = "testId"
        val leftContent = "leftContent"
        val rightContent = "rightContent"
        val rightUrl = "http://localhost:$localPort/v1/diff/$testId/right"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:$localPort/v1/diff/$testId"
        val leftRequest = HttpEntity(Base64.getEncoder().encodeToString(leftContent.toByteArray()))
        val rightRequest = HttpEntity(Base64.getEncoder().encodeToString(rightContent.toByteArray()))
        //when
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, rightRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, DifferenceResult::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.OK, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
        assertEquals(DifferenceType.DIFFERENT_SIZE, differenceResponse.body!!.type)
        assertNotNull(differenceResponse.body!!.message)
    }

    @Test
    fun shouldReturnContentDifferenceWhenMultiplePutRightAndLeft() {
        //given
        val testId = "testId"
        val leftContent = "rightSAMEPARTright"
        val rightContent = "lleftSAMEPARTlleft"
        val rightUrl = "http://localhost:$localPort/v1/diff/$testId/right"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:$localPort/v1/diff/$testId"
        val leftRequest = HttpEntity(Base64.getEncoder().encodeToString(leftContent.toByteArray()))
        val rightRequest = HttpEntity(Base64.getEncoder().encodeToString(rightContent.toByteArray()))
        //when
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, rightRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, DifferenceResult::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.OK, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
        assertEquals(DifferenceType.DIFFERENT_CONTENT, differenceResponse.body!!.type)
        assertNotNull(differenceResponse.body!!.message)
        assertTrue(differenceResponse.body!!.message.contains("(0, 4)"))
        assertTrue(differenceResponse.body!!.message.contains("(13, 4)"))
    }

    @Test
    fun shouldReturnBadRequestWhenEmptyLeftContent() {
        //given
        val testId = "testId"
        val url = "http://localhost:$localPort/v1/diff/$testId/left"
        val request = HttpEntity("")
        //when
        val putResponse = testRestTemplate.exchange(url, HttpMethod.PUT, request, Any::class.java)
        //then
        assertNotNull(putResponse)
        assertEquals(HttpStatus.BAD_REQUEST, putResponse.statusCode)
        assertNotNull(putResponse.body)
    }

    @Test
    fun shouldReturnBadRequestWhenEmptyRightContent() {
        //given
        val testId = "testId"
        val url = "http://localhost:$localPort/v1/diff/$testId/right"
        val request = HttpEntity("")
        //when
        val putResponse = testRestTemplate.exchange(url, HttpMethod.PUT, request, Any::class.java)
        //then
        assertNotNull(putResponse)
        assertEquals(HttpStatus.BAD_REQUEST, putResponse.statusCode)
        assertNotNull(putResponse.body)
    }

    @Test
    fun shouldReturnNotFoundWhenWrongId() {
        //given
        val testId = "testId"
        val leftContent = "leftContent"
        val rightContent = "rightContent"
        val rightUrl = "http://localhost:$localPort/v1/diff/$testId/right"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:" + localPort + "/v1/diff/" + testId + "1"
        val leftRequest = HttpEntity(Base64.getEncoder().encodeToString(leftContent.toByteArray()))
        val rightRequest = HttpEntity(Base64.getEncoder().encodeToString(rightContent.toByteArray()))
        //when
        testRestTemplate.exchange(rightUrl, HttpMethod.PUT, rightRequest, DifferenceRecord::class.java)
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, Any::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.NOT_FOUND, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
    }

    @Test
    fun shouldReturnNotFoundWhenOnlyOnePartExists() {
        //given
        val testId = "testId"
        val leftContent = "leftContent"
        val leftUrl = "http://localhost:$localPort/v1/diff/$testId/left"
        val diffUrl = "http://localhost:$localPort/v1/diff/$testId"
        val leftRequest = HttpEntity(Base64.getEncoder().encodeToString(leftContent.toByteArray()))
        //when
        testRestTemplate.exchange(leftUrl, HttpMethod.PUT, leftRequest, DifferenceRecord::class.java)
        val differenceResponse = testRestTemplate.getForEntity(diffUrl, Any::class.java)
        //then
        assertNotNull(differenceResponse)
        assertEquals(HttpStatus.NOT_FOUND, differenceResponse.statusCode)
        assertNotNull(differenceResponse.body)
    }
}
