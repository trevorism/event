package com.trevorism.service

import com.trevorism.model.EventSubscription

interface SubscriptionService {

    List<EventSubscription> getAllSubscriptions()
    List<EventSubscription> getSubscriptions(String topic)

    EventSubscription create(EventSubscription eventSubscription)
    boolean delete(String topic, String name)
}