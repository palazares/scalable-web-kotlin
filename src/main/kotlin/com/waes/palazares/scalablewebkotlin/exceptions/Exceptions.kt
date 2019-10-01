package com.waes.palazares.scalablewebkotlin.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Thrown when provided content is not a valid base64 string
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid base64 string")
class InvalidBase64Exception : Exception()

/**
 * Thrown when provided id has wrong format (null or empty)
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid entity Id")
class InavlidIdException : Exception()

/**
 * Thrown when difference record was not found or it's in partial state having only one valid side
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Record is not found or exists only in partial state")
class InvalidRecordContentException : Exception()