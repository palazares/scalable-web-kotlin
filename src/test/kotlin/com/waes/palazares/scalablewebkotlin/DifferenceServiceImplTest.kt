package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import com.waes.palazares.scalablewebkotlin.exceptions.InavlidIdException
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidBase64Exception
import com.waes.palazares.scalablewebkotlin.exceptions.InvalidRecordContentException
import org.junit.Test
import org.mockito.Mockito.*
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class DifferenceServiceImplTest {

    private val repository: DifferenceRepository = mock(DifferenceRepository::class.java)

    private val diffService: DifferenceServiceImpl = DifferenceServiceImpl(repository)

    @Test
    fun shouldThrowInvalidIdWhenLeftIdIsEmpty() {
        StepVerifier
                .create(diffService.putLeft("", ""))
                .expectError(InavlidIdException::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidIdWhenRightIdIsEmpty() {
        StepVerifier
                .create(diffService.putRight("", ""))
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
        StepVerifier
                .create(diffService.putLeft("testID", ""))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenLeftDocIsInvalidBase64() {
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putLeft("testID", "_- &^%"))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenRightDocIsEmpty() {
        StepVerifier
                .create(diffService.putLeft("testID", ""))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidBase64WhenRightDocIsInvalidBase64() {
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(DifferenceRecord()))
        StepVerifier
                .create(diffService.putRight("testID", "_- &^%"))
                .expectError(InvalidBase64Exception::class.java)
                .verify()
    }

    @Test
    fun shouldThrowInvalidRecordContentWhenRecordWasNotFound() {
        //when
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.empty())
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
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
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
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
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
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
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
        `when`(repository.findById(eq("testID"))).thenReturn(Mono.just(testIdRecord))
        StepVerifier
                .create(diffService.getDifference("testID"))
                .expectError(InvalidRecordContentException::class.java)
                .verify()
    }

//    @Test
//    fun shouldNotFailWhenCorrectBase64() {
//        //given
//        val testId = "testID"
//        val leftContent = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.empty())
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.putLeft(testId, leftContent))
//                .expectNextMatches { Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertNotNull(arg.left)
//            assertNull(arg.right)
//            assertNull(arg.result)
//            true
//        })
//    }

//    @Test
//    fun shouldSaveNewRecordWhenLeftDoc() {
//        //given
//        val testId = "testID"
//        val leftContent = "testContent".toByteArray()
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.empty())
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.putLeft(testId, Base64.getEncoder().encodeToString(leftContent)))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(leftContent, arg.left)
//            assertNull(arg.right)
//            assertNull(arg.result)
//            true
//        })
//    }
//
//    @Test
//    fun shouldSaveNewRecordWhenRightDoc() {
//        //given
//        val testId = "testID"
//        val rightContent = "testContent".toByteArray()
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.empty())
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.putRight(testId, Base64.getEncoder().encodeToString(rightContent)))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(rightContent, arg.right)
//            assertNull(arg.left)
//            assertNull(arg.result)
//            true
//        })
//    }
//
//    @Test
//    fun shouldUpdateRecordWhenLeftDoc() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "rightTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, "oldContent".toByteArray(), rightContent,
//                DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult"))
//
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.putLeft(testId, Base64.getEncoder().encodeToString(leftContent)))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(leftContent, arg.left)
//            assertArrayEquals(rightContent, arg.right)
//            assertNull(arg.result)
//            true
//        })
//    }
//
//    @Test
//    fun shouldUpdateRecordWhenRightDoc() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "rightTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, leftContent, "oldContent".toByteArray(),
//                DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult"))
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.putRight(testId, Base64.getEncoder().encodeToString(rightContent)))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(leftContent, arg.left)
//            assertArrayEquals(rightContent, arg.right)
//            assertNull(arg.result)
//            true
//        })
//    }
//
//    @Test
//    fun shouldNotUpdateRecordResultWhenRightDocIsTheSame() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "rightTestContent".toByteArray()
//        val testResult = DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult")
//        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent, testResult)
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        StepVerifier
//                .create(diffService.putRight(testId, Base64.getEncoder().encodeToString(rightContent)))
//                .expectNextMatches { x -> x == differenceRecord }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository, times(0)).save(any())
//    }
//
//    @Test
//    fun shouldNotUpdateRecordResultWhenLeftDocIsTheSame() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "rightTestContent".toByteArray()
//        val testResult = DifferenceResult(DifferenceType.DIFFERENT_SIZE, "testResult")
//        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent, testResult)
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        StepVerifier
//                .create(diffService.putLeft(testId, Base64.getEncoder().encodeToString(leftContent)))
//                .expectNextMatches { x -> x == differenceRecord }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository, times(0)).save(any())
//    }
//
//    @Test
//    fun shouldReturnEqualResultWhenDocsAreTheSame() {
//        //given
//        val testId = "testID"
//        val equalContent = "equalTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent)
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.getDifference(testId))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(equalContent, arg.left)
//            assertArrayEquals(equalContent, arg.right)
//            assertEquals(DifferenceType.EQUALS, arg.result!!.type)
//            assertNotNull(arg.result?.message)
//            true
//        })
//    }
//
//    @Test
//    fun shouldReturnDifferentSizeResultWhenDocsAreDifferent() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "rightTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, leftContent, rightContent)
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.getDifference(testId))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(any())
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(leftContent, arg.left)
//            assertArrayEquals(rightContent, arg.right)
//            assertEquals(DifferenceType.DIFFERENT_SIZE, arg.result!!.type)
//            assertNotNull(arg.result?.message)
//            true
//        })
//    }
//
//    @Test
//    fun shouldReturnDifferentContentResultWhenDocsContentsAreDifferent() {
//        //given
//        val testId = "testID"
//        val leftContent = "leftTestContent".toByteArray()
//        val rightContent = "ightTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, leftContent, (rightContent))
//
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        `when`(repository.save(any())).thenReturn(Mono.just(DifferenceRecord()))
//        StepVerifier
//                .create(diffService.getDifference(testId))
//                .expectNextMatches{ Objects.nonNull(it) }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository).save(ArgumentMatchers.argThat { arg ->
//            assertEquals(testId, arg.id)
//            assertArrayEquals(leftContent, arg.left)
//            assertArrayEquals(rightContent, arg.right)
//            assertEquals(DifferenceType.DIFFERENT_CONTENT, arg.result!!.type)
//            assertNotNull(arg.result?.message)
//            true
//        })
//    }
//
//    @Test
//    fun shouldNotSaveWhenLeftDocIsTheSame() {
//        //given
//        val testId = "testID"
//        val equalContent = "equalTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent,
//                DifferenceResult(DifferenceType.EQUALS, "equals"))
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        StepVerifier
//                .create(diffService.getDifference(testId))
//                .expectNextMatches { x -> x == differenceRecord }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository, times(0)).save(any())
//    }
//
//    @Test
//    fun shouldNotCompareWhenResultExists() {
//        //given
//        val testId = "testID"
//        val equalContent = "equalTestContent".toByteArray()
//        val differenceRecord = DifferenceRecord(testId, equalContent, equalContent,
//                DifferenceResult(DifferenceType.EQUALS, "equals"))
//        //when
//        `when`(repository.findById(eq(testId))).thenReturn(Mono.just(differenceRecord))
//        StepVerifier
//                .create(diffService.getDifference(testId))
//                .expectNextMatches { x -> x == differenceRecord }
//                .expectComplete()
//                .verify()
//        //then
//        verify(repository).findById(eq(testId))
//        verify(repository, times(0)).save(any())
//    }
}