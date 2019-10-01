package com.waes.palazares.scalablewebkotlin

import java.lang.System.getLogger

interface Logging

inline fun <reified T : Logging> T.logger(): System.Logger = getLogger(T::class.java)