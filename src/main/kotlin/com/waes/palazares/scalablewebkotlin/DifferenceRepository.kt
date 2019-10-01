package com.waes.palazares.scalablewebkotlin

import com.waes.palazares.scalablewebkotlin.domain.DifferenceRecord
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface DifferenceRepository : ReactiveCrudRepository<DifferenceRecord, String>