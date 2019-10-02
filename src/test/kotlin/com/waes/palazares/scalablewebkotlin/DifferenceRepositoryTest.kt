package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.test.context.junit4.SpringRunner
import java.time.Duration

@RunWith(SpringRunner::class)
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