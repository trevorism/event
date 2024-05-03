package com.trevorism.controller

import com.trevorism.service.EventService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/event")
class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class.name)

    @Inject
    EventService eventService

    @Tag(name = "Event Operations")
    @Operation(summary = "Sends an event on the given topic")
    @Status(HttpStatus.CREATED)
    @Post(value = "{topic}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    String sendEvent(String topic, @Body Map<String, Object> data, HttpRequest<?> request) {
        try {
            String eventResult = eventService.sendEvent(topic, data, request)
            log.info("Event sent on topic ${topic}: ${eventResult}")
            return eventResult
        } catch (Exception e) {
            log.error("Unable to create event on topic ${topic}", e)
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to create event on topic ${topic}: ${e.message}")
        }
    }
}
