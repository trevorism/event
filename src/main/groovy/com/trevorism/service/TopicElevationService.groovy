package com.trevorism.service

import com.trevorism.model.ElevatedTopic

interface TopicElevationService {

    boolean requiresElevation(String topic)

    String mintInternalToken(String tenantId)

    List<ElevatedTopic> list()

    ElevatedTopic create(ElevatedTopic elevatedTopic)

    ElevatedTopic delete(String id)
}
