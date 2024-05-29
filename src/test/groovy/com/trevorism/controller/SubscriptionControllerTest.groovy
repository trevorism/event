package com.trevorism.controller

import com.trevorism.model.EventSubscription
import com.trevorism.service.SubscriptionService
import org.junit.jupiter.api.Test

class SubscriptionControllerTest {

    @Test
    void testListSubscriptions(){
        SubscriptionController subscriptionController = new SubscriptionController()
        subscriptionController.subscriptionService = [getAllSubscriptions: {-> [new EventSubscription()]} ] as SubscriptionService
        def subscriptions = subscriptionController.listAllSubscriptions()
        assert subscriptions
        assert subscriptions.size() == 1
    }

    @Test
    void testCreateSubscription(){
        SubscriptionController subscriptionController = new SubscriptionController()
        subscriptionController.subscriptionService = [create: {subscription -> subscription} ] as SubscriptionService
        def subscription = subscriptionController.createSubscription(new EventSubscription())
        assert subscription
    }

    @Test
    void testGetSubscription(){
        SubscriptionController subscriptionController = new SubscriptionController()
        subscriptionController.subscriptionService = [getSubscription: {id -> new EventSubscription()} ] as SubscriptionService
        def subscription = subscriptionController.get("123-234-345")
        assert subscription
    }

    @Test
    void testDelete(){
        SubscriptionController subscriptionController = new SubscriptionController()
        subscriptionController.subscriptionService = [getSubscription: {id -> new EventSubscription()}, delete: {id -> true} ] as SubscriptionService
        def result = subscriptionController.delete("123-234-345")
        assert result
    }
}
