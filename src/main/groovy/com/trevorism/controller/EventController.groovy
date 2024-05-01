package com.trevorism.controller

import com.trevorism.service.EventService
import com.trevorism.service.TopicService
import io.micronaut.http.HttpHeaders
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

import java.util.logging.Logger

@Controller("/api")
class EventController {

    private static final Logger log = Logger.getLogger(EventController.class.name)

    @Inject
    EventService eventService

    @Inject
    TopicService topicService

    @Tag(name = "Event Operations")
    @Operation(summary = "Sends an event on the given topic **Secure")
    @Status(HttpStatus.CREATED)
    @Post(value = "{topic}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map<String, Object> sendEvent(String topic, @Body Map<String, Object> data, HttpRequest<?> request) {
        try {
            topicService.createTopic(topic)
            println eventService.sendEvent(topic, data, request)
            return data

        } catch (Exception e) {

            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to create event on topic ${topic}: ${e.message}")
        }
    }

}
