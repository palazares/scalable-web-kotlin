package com.waes.palazares.scalablewebkotlin

import com.nhaarman.mockitokotlin2.*
import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.domain.DifferenceResult
import com.waes.palazares.scalablewebkotlin.domain.DifferenceType
import com.waes.palazares.scalablewebkotlin.exceptions.InavlidIdException
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidBase64Exception
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidRecordContentException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

class DifferenceServiceImplTest {

    private val repository: DifferenceRepository = mock()

    private val diffService: DifferenceServiceImpl = DifferenceServiceImpl(repository)

    @Test
    fun shouldThrowInvalidIdWhenLeftIdIsEmpty() {
        StepVerifier
                .create(diffService.putLeft("", Mono.just("")))
                .expectError(InavlidIdException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidIdWhenRightIdIsEmpty() {
        StepVerifier
                .create(diffService.putRight("", Mono.just("")))
                .expectError(InavlidIdException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidIdWhenDifIdIsEmpty() {
        StepVerifier
                .create(diffService.getDifference(""))
                .expectError(InavlidIdException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenLeftDocIsEmpty() {
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.empty())
        StepVerifier
                .create(diffService.putLeft("testID", Mono.just("")))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenLeftDocIsInvalidBase64() {
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putLeft("testID", Mono.just("_- &^%")))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenRightDocIsEmpty() {
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.empty())
        StepVerifier
                .create(diffService.putLeft("testID", Mono.just("")))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenRightDocIsInvalidBase64() {
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putRight("testID", Mono.just("_- &^%")))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenRecordWasNotFound() {
        //when
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.empty())
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenRightDocIsNull() {
        //given
        val testIdRecord = DifferenceRecord(id = "testID", left = "content".toByteArray())
        //when
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenRightDocIsEmpty() {
        //given
        val testIdRecord = DifferenceRecord(id = "testID", left = "content".toByteArray(), right = ByteArray(0))
        //when
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenLeftDocIsNull() {
        //given
        val testIdRecord = DifferenceRecord(id = "testID", right = "content".toByteArray())
        //when
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenLeftDocIsEmpty() {
        //given
        val testIdRecord = DifferenceRecord(id = "testID", right = "content".toByteArray(), left = ByteArray(0))
        //when
        whenever(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

    @Test
    fun shouldNotFailWhenCorrectBase64() {
        //given
        val testId = "testID"
        val leftContent = Mono.just("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/")
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.empty())
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putLeft(testId, leftContent))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertNotNull(it.left)
            assertNull(it.right)
            assertNull(it.result)
        })
    }

    @Test
    fun shouldSaveNewRecordWhenLeftDoc() {
        //given
        val testId = "testID"
        val leftContent = "testContent".toByteArray()
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.empty())
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putLeft(testId, Mono.just(Base64.getEncoder().encodeToString(leftContent))))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(leftContent, it.left)
            assertNull(it.right)
            assertNull(it.result)
        })
    }

    @Test
    fun shouldSaveNewRecordWhenRightDoc() {
        //given
        val testId = "testID"
        val rightContent = "testContent".toByteArray()
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.empty())
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putRight(testId, Mono.just(Base64.getEncoder().encodeToString(rightContent))))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(rightContent, it.right)
            assertNull(it.left)
            assertNull(it.result)
        })
    }

    @Test
    fun shouldUpdateRecordWhenLeftDoc() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "rightTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, "oldContent".toByteArray(), rightContent,
                DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult"))

        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putLeft(testId, Mono.just(Base64.getEncoder().encodeToString(leftContent))))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(leftContent, it.left)
            assertArrayEquals(rightContent, it.right)
            assertNull(it.result)
        })
    }

    @Test
    fun shouldUpdateRecordWhenRightDoc() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "rightTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, leftContent, "oldContent".toByteArray(),
                DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult"))
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putRight(testId, Mono.just(Base64.getEncoder().encodeToString(rightContent))))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(leftContent, it.left)
            assertArrayEquals(rightContent, it.right)
            assertNull(it.result)
        })
    }

    @Test
    fun shouldNotUpdateRecordResultWhenRightDocIsTheSame() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "rightTestContent".toByteArray()
        val testResult = DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult")
        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent, testResult)
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        StepVerifier
                .create(diffService.putRight(testId, Mono.just(Base64.getEncoder().encodeToString(rightContent))))
                .expectNextMatches { x -> x == differenceRecord }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository, times(0)).save<DifferenceRecord>(any())
    }

    @Test
    fun shouldNotUpdateRecordResultWhenLeftDocIsTheSame() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "rightTestContent".toByteArray()
        val testResult = DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult")
        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent, testResult)
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        StepVerifier
                .create(diffService.putLeft(testId, Mono.just(Base64.getEncoder().encodeToString(leftContent))))
                .expectNextMatches { x -> x == differenceRecord }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository, times(0)).save<DifferenceRecord>(any())
    }

    @Test
    fun shouldReturnEqualResultWhenDocsAreTheSame() {
        //given
        val testId = "testID"
        val equalContent = "equalTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent)
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.getDifference(testId))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(equalContent, it.left)
            assertArrayEquals(equalContent, it.right)
            assertEquals(DifferenceType.EQUALS, it.result!!.type)
            assertNotNull(it.result?.message)
        })
    }

    @Test
    fun shouldReturnDifferentSizeResultWhenDocsAreDifferent() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "rightTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent)
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.getDifference(testId))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(any())
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(leftContent, it.left)
            assertArrayEquals(rightContent, it.right)
            assertEquals(DifferenceType.DIFFERENT_SIZE, it.result!!.type)
            assertNotNull(it.result?.message)
        })
    }

    @Test
    fun shouldReturnDifferentContentResultWhenDocsContentsAreDifferent() {
        //given
        val testId = "testID"
        val leftContent = "leftTestContent".toByteArray()
        val rightContent = "ightTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, leftContent, (rightContent))

        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        whenever(repository.save<DifferenceRecord>(any())).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.getDifference(testId))
                .expectNextMatches { Objects.nonNull(it) }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository).save<DifferenceRecord>(check {
            assertEquals(testId, it.id)
            assertArrayEquals(leftContent, it.left)
            assertArrayEquals(rightContent, it.right)
            assertEquals(DifferenceType.DIFFERENT_CONTENT, it.result!!.type)
            assertNotNull(it.result?.message)
        })
    }

    @Test
    fun shouldNotSaveWhenLeftDocIsTheSame() {
        //given
        val testId = "testID"
        val equalContent = "equalTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent,
                DifferenceResult(DifferenceType.EQUALS, "equals"))
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        StepVerifier
                .create(diffService.getDifference(testId))
                .expectNextMatches { x -> x == differenceRecord }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository, times(0)).save<DifferenceRecord>(any())
    }

    @Test
    fun shouldNotCompareWhenResultExists() {
        //given
        val testId = "testID"
        val equalContent = "equalTestContent".toByteArray()
        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent,
                DifferenceResult(DifferenceType.EQUALS, "equals"))
        //when
        whenever(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
        StepVerifier
                .create(diffService.getDifference(testId))
                .expectNextMatches { x -> x == differenceRecord }
                .expectComplete()
                .verify()
        //then
        verify(repository).findById(eq(testId))
        verify(repository, times(0)).save<DifferenceRecord>(any())
    }
}