package com.waes.palazares.scalablewebkotlin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.router

@SpringBootApplication
class ScalableWebKotlinApplication {
    @Bean
    fun routes(handler: DifferenceService) = router {
        "/v1/diff".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/{id}") { ServerResponse.ok().body(handler.getDifference(it.pathVariable("id"))) }
                PUT("/{id}/left") {
                    ServerResponse.ok()
                            .body(handler.putLeft(it.pathVariable("id"), it.bodyToMono()))

                }
                PUT("/{id}/right") {
                    ServerResponse.ok()
                            .body(handler.putRight(it.pathVariable("id"), it.bodyToMono()))
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ScalableWebKotlinApplication>(*args)
}

