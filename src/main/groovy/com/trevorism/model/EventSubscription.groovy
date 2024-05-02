package com.trevorism.model

class EventSubscription {

    public static EventSubscription NULL_SUBSCRIPTION = new EventSubscription()

    String name
    String type = EventSubscriptionType.PULL_UNWRAPPED
    String topic
    String url
    int ackDeadlineSeconds = 60
}
