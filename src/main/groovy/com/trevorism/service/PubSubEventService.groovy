package com.trevorism.service

import com.google.api.core.ApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import com.trevorism.https.SecureHttpClient
import io.micronaut.http.HttpRequest
import io.micronaut.runtime.http.scope.RequestScope
import io.micronaut.security.authentication.ServerAuthentication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RequestScope
class PubSubEventService implements EventService{
    private static final Logger log = LoggerFactory.getLogger(PubSubEventService.class.name)
    private Gson gson = (new GsonBuilder()).setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()

    String sendEvent(String topicName, Map<String, Object> data, HttpRequest<?> request) {
        String json = gson.toJson(data)
        PubsubMessage pubsubMessage = createPubsubMessage(json, topicName, request)
        return sendMessage(topicName, pubsubMessage)
    }

    private static String sendMessage(String topicName, PubsubMessage pubsubMessage) {
        ProjectTopicName topic = ProjectTopicName.of(EventService.PROJECT_ID, topicName)
        Publisher publisher = Publisher.newBuilder(topic).build()
        log.debug("Sending message to topic ${topicName}")
        ApiFuture<String> future = publisher.publish(pubsubMessage)
        publisher.shutdown()
        return future.get()
    }

    private static PubsubMessage createPubsubMessage(String json, String topicName, HttpRequest<?> request) {
        ByteString byteString = ByteString.copyFromUtf8(json)
        def attributesMap = ["topic": topicName, "Content-Type": "application/json"]
        attributesMap = addCorrelationId(request, attributesMap)
        attributesMap = addToken(request, attributesMap)
        attributesMap = addTenantId(request, attributesMap)

        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(byteString)
                .putAllAttributes(attributesMap)
                .build()
        return pubsubMessage
    }

    private static def addCorrelationId(HttpRequest<?> request, Map attributesMap) {
        String correlationId = request.getHeaders().get("X-Correlation-ID")
        if (correlationId)
            attributesMap.put("correlationId", correlationId.toString())
        else
            attributesMap.put("correlationId", UUID.randomUUID().toString())
        log.debug("Event correlationId: ${attributesMap.get("correlationId")}")
        return attributesMap
    }

    private static def addToken(HttpRequest<?> request, Map attributesMap) {
        String authHeader = request.getHeaders().get(SecureHttpClient.AUTHORIZATION)
        if (authHeader && authHeader.toLowerCase().startsWith(SecureHttpClient.BEARER_.toLowerCase()))
            attributesMap.put(SecureHttpClient.AUTHORIZATION, authHeader)
        return attributesMap
    }

    private static def addTenantId(HttpRequest<?> request, Map attributesMap) {
        Optional<ServerAuthentication> wrappedTenant = request.getAttribute("micronaut.AUTHENTICATION", ServerAuthentication)
        if(wrappedTenant.isPresent()) {
            def tenant = wrappedTenant.get()?.attributes?.get("tenant")
            if(tenant){
                attributesMap.put("X-Tenant-ID", tenant)
            }
        }
        return attributesMap
    }
}
