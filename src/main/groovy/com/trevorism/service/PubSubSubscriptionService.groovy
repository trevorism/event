package com.trevorism.service

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.pubsub.v1.ListSubscriptionsRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.Subscription
import com.google.pubsub.v1.SubscriptionName
import com.google.pubsub.v1.TopicName
import com.trevorism.model.EventSubscription
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@jakarta.inject.Singleton
class PubSubSubscriptionService implements SubscriptionService{

    private SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()
    private static final Logger log = LoggerFactory.getLogger(PubSubSubscriptionService.class.name)

    @Override
    List<EventSubscription> getAllSubscriptions() {
        ListSubscriptionsRequest listSubscriptionsRequest = ListSubscriptionsRequest.newBuilder().setProject(ProjectName.format(EventService.PROJECT_ID)).build()
        List<EventSubscription> subscribers = []
        for (Subscription subscription : subscriptionAdminClient.listSubscriptions(listSubscriptionsRequest).iterateAll()) {
            subscribers << createSubscriber(subscription)
        }
        return subscribers
    }

    @Override
    List<EventSubscription> getSubscriptions(String topic) {
        return getAllSubscriptions().findAll { it.topic == topic }
    }

    @Override
    EventSubscription create(EventSubscription eventSubscription) {
        try {
            TopicName topicName = TopicName.of(EventService.PROJECT_ID, eventSubscription.topic)
            SubscriptionName subscriptionName = SubscriptionName.of(EventService.PROJECT_ID, eventSubscription.name)
            PushConfig.NoWrapper noWrapper = PushConfig.NoWrapper.newBuilder().setWriteMetadata(true).build()
            PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint(eventSubscription.url).setNoWrapper(noWrapper).build()

            Subscription subscription = subscriptionAdminClient.createSubscription(subscriptionName, topicName, pushConfig, eventSubscription.ackDeadlineSeconds)
            return createSubscriber(subscription)
        } catch (Exception e) {
            log.warn("Failed to create subscription: ${e.message}")
            return EventSubscription.NULL_SUBSCRIPTION
        }
    }

    @Override
    boolean delete(String topic, String name) {
        try{
            subscriptionAdminClient.deleteSubscription("projects/${EventService.PROJECT_ID}/subscriptions/${name}")
            return true
        }catch (Exception e) {
            log.warn("Failed to delete subscription: ${name}", e)
            return false
        }
    }

    private static EventSubscription createSubscriber(Subscription subscription) {
        if (!subscription)
            return null

        EventSubscription subscriber = new EventSubscription()
        subscriber.name = subscription.name.substring("projects/${EventService.PROJECT_ID}/subscriptions/".length())
        subscriber.url = subscription.getPushConfig().pushEndpoint
        subscriber.topic = subscription.topic.substring("projects/${EventService.PROJECT_ID}/topics/".length())
        subscriber.ackDeadlineSeconds = subscription.getAckDeadlineSeconds()
        return subscriber
    }
}
