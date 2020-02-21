package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import org.hibernate.validator.internal.util.Contracts.assertNotNull
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration

@DataMongoTest
class DifferenceRepositoryTest {
    @Autowired
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Autowired
    private lateinit var repository: DifferenceRepository

    @MockBean
    private lateinit var differenceService: DifferenceService

    @Test
    fun shouldReturnSaved() {
        // arrange
        val sample = DifferenceRecord("testId", "leftContent".toByteArray(), "rightContent".toByteArray(),
                DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testMessage"))
        // act
        repository.save(sample).block(Duration.ofSeconds(30))
        // assert
        val result = mongoTemplate.findById("testId", DifferenceRecord::class.java).block()
        assertNotNull(result)
        assertEquals("testId", result!!.id)
        assertArrayEquals("leftContent".toByteArray(), result.left)
        assertArrayEquals("rightContent".toByteArray(), result.right)
        assertEquals(DifferenceType.DIFFERENT_SIZE, result.result!!.type)
        assertEquals("testMessage", result.result!!.message)
    }
}