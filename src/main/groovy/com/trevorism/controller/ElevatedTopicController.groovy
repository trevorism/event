package com.trevorism.controller

import com.trevorism.model.ElevatedTopic
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.TopicElevationService
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

@Controller("/api/elevation")
class ElevatedTopicController {

    private static final Logger log = LoggerFactory.getLogger(ElevatedTopicController.class.name)

    @Inject
    TopicElevationService topicElevationService

    @Tag(name = "Elevation Operations")
    @Operation(summary = "Lists the topics that elevate anonymous events to an internal token **Secure")
    @Secure(Roles.SYSTEM)
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    List<ElevatedTopic> listElevatedTopics() {
        return topicElevationService.list()
    }

    @Tag(name = "Elevation Operations")
    @Operation(summary = "Allows anonymous events on a topic to be elevated to an internal token **Secure")
    @Secure(Roles.SYSTEM)
    @Status(HttpStatus.CREATED)
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    ElevatedTopic createElevatedTopic(@Body ElevatedTopic elevatedTopic) {
        if (!elevatedTopic?.topic) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to elevate a topic without a topic name")
        }
        log.info("Elevating anonymous events on topic ${elevatedTopic.topic}")
        return topicElevationService.create(elevatedTopic)
    }

    @Tag(name = "Elevation Operations")
    @Operation(summary = "Stops elevating anonymous events on a topic **Secure")
    @Secure(Roles.SYSTEM)
    @Delete(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    ElevatedTopic deleteElevatedTopic(String id) {
        ElevatedTopic deleted = topicElevationService.delete(id)
        if (!deleted) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "${id} not found")
        }
        return deleted
    }
}
