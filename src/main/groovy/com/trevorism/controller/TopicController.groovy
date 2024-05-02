package com.trevorism.controller

import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.EventService
import com.trevorism.service.TopicService
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.exceptions.HttpStatusException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Controller("/topic")
class TopicController {

    private static final Logger log = LoggerFactory.getLogger(TopicController.class.name)

    @Inject
    EventService eventService

    @Inject
    TopicService topicService

    @Tag(name = "Topic Operations")
    @Operation(summary = "Sends an event on the given topic **Secure")
    @Status(HttpStatus.CREATED)
    @Post(value = "{topic}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    Map<String, Object> sendEvent(String topic, @Body Map<String, Object> data, HttpRequest<?> request) {
        try {
            topicService.createTopic(topic)
            String eventResult = eventService.sendEvent(topic, data, request)
            log.info("Event sent on topic ${topic}: ${eventResult}")
            return data
        } catch (Exception e) {
            log.error("Unable to create event on topic ${topic}", e)
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to create event on topic ${topic}: ${e.message}")
        }
    }

    @Tag(name = "Topic Operations")
    @Operation(summary = "Get all topics")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    List<String> listTopics() {
        try {
            return topicService.allTopics
        } catch (Exception e) {
            log.error("Unable to get topics", e)
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to get topics: ${e.message}")
        }
    }

    @Tag(name = "Topic Operations")
    @Operation(summary = "Get topic details **Secure")
    @Get(value = "{topic}", produces = MediaType.TEXT_PLAIN)
    @Secure(value = Roles.USER, allowInternal = true)
    String readAll(String topic) {
        try {
            return topicService.getTopic(topic)
        } catch (Exception e) {
            log.warn("Unable to find topic ${topic}", e)
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Unable to find topic ${topic}: ${e.message}")
        }
    }

    @Tag(name = "Topic Operations")
    @Operation(summary = "Delete a topic **Secure")
    @Delete(value = "{topic}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    boolean delete(String topic) {
        try {
            return topicService.deleteTopic(topic)
        } catch (Exception e) {
            log.warn("Unable to delete topic ${topic}", e)
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Unable to delete topic ${topic}: ${e.message}")
        }
    }
}
