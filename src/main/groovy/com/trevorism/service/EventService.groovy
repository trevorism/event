package com.trevorism.service

import io.micronaut.http.HttpRequest

interface EventService {
    static final String PROJECT_ID = "trevorism-data"

    String sendEvent(String topicName, Map<String, Object> data, HttpRequest<?> request)
}