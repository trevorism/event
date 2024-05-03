package com.trevorism.controller

import com.trevorism.model.EventSubscription
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import com.trevorism.service.SubscriptionService
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.exceptions.HttpStatusException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/subscription")
class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class.name)

    @Inject
    SubscriptionService subscriptionService

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Get all subscriptions **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    List<EventSubscription> listAllSubscriptions() {
        try {
            return subscriptionService.getAllSubscriptions()
        } catch (Exception e) {
            log.error("Unable to get subscriptions", e)
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to get topics: ${e.message}")
        }
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Create a topic subscription **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true)
    EventSubscription createSubscription(@Body EventSubscription subscription) {
        try {
            return subscriptionService.create(subscription)
        } catch (Exception e) {
            log.error("Unable to create subscription on topic ${subscription.topic}", e)
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to create subscription on topic ${topic}: ${e.message}")
        }
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Get subscriptions for a given topic **Secure")
    @Get(value = "{name}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    EventSubscription get(String name) {
        try {
            return subscriptionService.getSubscription(name)
        } catch (Exception e) {
            log.warn("Unable to find subscription ${name}", e)
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Unable to find subscription ${name}")
        }
    }

    @Tag(name = "Subscription Operations")
    @Operation(summary = "Delete a topic subscription **Secure")
    @Delete(value = "{name}", produces = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER)
    EventSubscription delete(String name) {
        try {
            EventSubscription subscription = subscriptionService.getSubscription(name)
            if(subscriptionService.delete(name))
                return subscription
            throw new Exception("Unable to delete subscription ${name}")
        } catch (Exception e) {
            log.warn("Unable to delete subscription ${name}", e)
            throw new HttpStatusException(HttpStatus.NOT_FOUND, e.message)
        }
    }
}
