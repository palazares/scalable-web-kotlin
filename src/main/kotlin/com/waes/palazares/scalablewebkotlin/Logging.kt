package com.waes.palazares.scalablewebkotlin

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

interface Logging

inline fun <reified T : Logging> T.logger(): Logger = getLogger(T::class.java)