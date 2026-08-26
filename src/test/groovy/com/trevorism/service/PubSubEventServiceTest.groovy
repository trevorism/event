package com.trevorism.service

import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ElevatedTopic
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import org.junit.jupiter.api.Test

class PubSubEventServiceTest {

    @Test
    void testCallerTokenIsForwardedAndNotElevated() {
        boolean minted = false
        PubSubEventService service = new PubSubEventService()
        service.topicElevationService = elevationService(true, { minted = true; "internal-token" })

        Map attributes = buildAttributes(service, "testResult", requestWithAuthorization("Bearer caller-token"))

        assert attributes.get(SecureHttpClient.AUTHORIZATION) == "Bearer caller-token"
        assert !minted
    }

    @Test
    void testAnonymousEventOnElevatedTopicGetsInternalToken() {
        PubSubEventService service = new PubSubEventService()
        service.topicElevationService = elevationService(true, { "internal-token" })

        Map attributes = buildAttributes(service, "testResult", HttpRequest.POST("/event/testResult", [:]))

        assert attributes.get(SecureHttpClient.AUTHORIZATION) == "Bearer internal-token"
    }

    @Test
    void testAnonymousEventOnUnlistedTopicIsNotElevated() {
        PubSubEventService service = new PubSubEventService()
        service.topicElevationService = elevationService(false, { "internal-token" })

        Map attributes = buildAttributes(service, "somethingElse", HttpRequest.POST("/event/somethingElse", [:]))

        assert !attributes.containsKey(SecureHttpClient.AUTHORIZATION)
    }

    @Test
    void testMintFailureDegradesToNoElevation() {
        PubSubEventService service = new PubSubEventService()
        service.topicElevationService = elevationService(true, { null })

        Map attributes = buildAttributes(service, "testResult", HttpRequest.POST("/event/testResult", [:]))

        assert !attributes.containsKey(SecureHttpClient.AUTHORIZATION)
    }

    @Test
    void testNonBearerAuthorizationHeaderIsNotForwarded() {
        PubSubEventService service = new PubSubEventService()
        service.topicElevationService = elevationService(false, { "internal-token" })

        Map attributes = buildAttributes(service, "testResult", requestWithAuthorization("Basic abc123"))

        assert !attributes.containsKey(SecureHttpClient.AUTHORIZATION)
    }

    private static Map buildAttributes(PubSubEventService service, String topic, HttpRequest<?> request) {
        return service.addToken(request, [:], topic) as Map
    }

    private static MutableHttpRequest<?> requestWithAuthorization(String value) {
        return HttpRequest.POST("/event/testResult", [:]).header(SecureHttpClient.AUTHORIZATION, value)
    }

    private static TopicElevationService elevationService(boolean elevated, Closure<String> mint) {
        return [
                requiresElevation: { String topic -> elevated },
                mintInternalToken: { String tenantId -> mint.call() },
                list             : { [] as List<ElevatedTopic> },
                create           : { ElevatedTopic t -> t },
                delete           : { String id -> null }
        ] as TopicElevationService
    }
}
