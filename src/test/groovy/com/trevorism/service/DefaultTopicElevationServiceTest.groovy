package com.trevorism.service

import com.trevorism.PropertiesProvider
import com.trevorism.data.Repository
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.ElevatedTopic
import org.junit.jupiter.api.Test

class DefaultTopicElevationServiceTest {

    @Test
    void testEnabledTopicRequiresElevation() {
        DefaultTopicElevationService service = build([new ElevatedTopic(topic: "testResult", enabled: true)])

        assert service.requiresElevation("testResult")
    }

    @Test
    void testDisabledTopicDoesNotRequireElevation() {
        DefaultTopicElevationService service = build([new ElevatedTopic(topic: "testResult", enabled: false)])

        assert !service.requiresElevation("testResult")
    }

    @Test
    void testUnlistedTopicDoesNotRequireElevation() {
        DefaultTopicElevationService service = build([new ElevatedTopic(topic: "testResult", enabled: true)])

        assert !service.requiresElevation("deployCandidate")
    }

    @Test
    void testMissingTopicDoesNotRequireElevation() {
        DefaultTopicElevationService service = build([new ElevatedTopic(topic: "testResult", enabled: true)])

        assert !service.requiresElevation(null)
    }

    @Test
    void testDatastoreFailureDegradesToNoElevation() {
        DefaultTopicElevationService service = build(null)
        service.@repository = [list: { throw new RuntimeException("datastore down") }] as Repository

        assert !service.requiresElevation("testResult")
    }

    @Test
    void testAllowlistIsCachedBetweenCalls() {
        int reads = 0
        DefaultTopicElevationService service = build(null)
        service.@repository = [list: {
            reads++
            [new ElevatedTopic(topic: "testResult", enabled: true)]
        }] as Repository

        service.requiresElevation("testResult")
        service.requiresElevation("testResult")

        assert reads == 1
    }

    @Test
    void testCreateInvalidatesTheCache() {
        int reads = 0
        DefaultTopicElevationService service = build(null)
        service.@repository = [
                list  : { reads++; [] },
                create: { ElevatedTopic topic -> topic }
        ] as Repository

        service.requiresElevation("testResult")
        service.create(new ElevatedTopic(topic: "testResult"))
        service.requiresElevation("testResult")

        assert reads == 2
    }

    @Test
    void testMintInternalTokenIsCached() {
        int posts = 0
        DefaultTopicElevationService service = build(null, [post: { String url, String body ->
            posts++
            "internal-token"
        }] as SecureHttpClient)

        assert service.mintInternalToken(null) == "internal-token"
        assert service.mintInternalToken(null) == "internal-token"
        assert posts == 1
    }

    @Test
    void testMintInternalTokenIsScopedPerTenant() {
        DefaultTopicElevationService service = build(null, [post: { String url, String body ->
            body.contains("tenant-a") ? "token-a" : "token-b"
        }] as SecureHttpClient)

        assert service.mintInternalToken("tenant-a") == "token-a"
        assert service.mintInternalToken("tenant-b") == "token-b"
    }

    @Test
    void testMintInternalTokenFailureReturnsNull() {
        DefaultTopicElevationService service = build(null, [post: { String url, String body ->
            throw new RuntimeException("auth down")
        }] as SecureHttpClient)

        assert service.mintInternalToken(null) == null
    }

    @Test
    void testEmptyTokenResponseReturnsNull() {
        DefaultTopicElevationService service = build(null, [post: { String url, String body -> "" }] as SecureHttpClient)

        assert service.mintInternalToken(null) == null
    }

    private static DefaultTopicElevationService build(List<ElevatedTopic> topics, SecureHttpClient client = null) {
        SecureHttpClient httpClient = client ?: ([post: { String url, String body -> "internal-token" }] as SecureHttpClient)
        PropertiesProvider propertiesProvider = [getProperty: { String key -> "event-client-id" }] as PropertiesProvider
        DefaultTopicElevationService service = new DefaultTopicElevationService(propertiesProvider, httpClient)
        if (topics != null) {
            service.@repository = [list: { topics }] as Repository
        }
        return service
    }
}
