package com.trevorism.service

import com.trevorism.model.EventSubscription

interface SubscriptionService {

    List<EventSubscription> getAllSubscriptions()
    EventSubscription getSubscription(String name)

    EventSubscription create(EventSubscription eventSubscription)
    boolean delete(String name)
}