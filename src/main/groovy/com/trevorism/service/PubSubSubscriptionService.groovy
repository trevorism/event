package com.trevorism.service

import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.iam.v1.Binding
import com.google.iam.v1.GetIamPolicyRequest
import com.google.iam.v1.Policy
import com.google.iam.v1.SetIamPolicyRequest
import com.google.pubsub.v1.DeadLetterPolicy
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

    public static final GString SUBSCRIPTION_PREFIX = "projects/${EventService.PROJECT_ID}/subscriptions/"
    public static final int MAX_DELIVERY_ATTEMPTS = 5
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
    EventSubscription getSubscription(String name) {
        Subscription subscription = subscriptionAdminClient.getSubscription("$SUBSCRIPTION_PREFIX${name}")
        return createSubscriber(subscription)
    }

    @Override
    EventSubscription create(EventSubscription eventSubscription) {
        TopicName topicName = TopicName.of(EventService.PROJECT_ID, eventSubscription.topic)
        SubscriptionName subscriptionName = SubscriptionName.of(EventService.PROJECT_ID, eventSubscription.name)
        PushConfig.NoWrapper noWrapper = PushConfig.NoWrapper.newBuilder().setWriteMetadata(true).build()
        PushConfig pushConfig = PushConfig.newBuilder().setPushEndpoint(eventSubscription.url).setNoWrapper(noWrapper).build()

        DeadLetterPolicy deadLetterPolicy = DeadLetterPolicy.newBuilder()
                .setDeadLetterTopic("${PubSubTopicService.TOPIC_PREFIX}dead-letter-topic")
                .setMaxDeliveryAttempts(MAX_DELIVERY_ATTEMPTS)
                .build()

        Subscription subscriptionToCreate = Subscription.newBuilder()
                .setName(subscriptionName.toString())
                .setTopic(topicName.toString())
                .setPushConfig(pushConfig)
                .setAckDeadlineSeconds(eventSubscription.ackDeadlineSeconds)
                .setDeadLetterPolicy(deadLetterPolicy)
                .build()

        Subscription subscription = subscriptionAdminClient.createSubscription(subscriptionToCreate)
        assignSubscriberRoleToDeadLetterTopic()
        return createSubscriber(subscription)
    }

    private static void assignSubscriberRoleToDeadLetterTopic() {
        String projectId = EventService.PROJECT_ID
        String serviceAccountEmail = "service-381816155418@gcp-sa-pubsub.iam.gserviceaccount.com"
        TopicName topicName = TopicName.of(projectId, "dead-letter-topic")
        GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
                .setResource(topicName.toString())
                .build()
        try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
            Policy policy = topicAdminClient.getIamPolicy(request)
            Binding binding = Binding.newBuilder()
                    .setRole("roles/pubsub.subscriber")
                    .addMembers("serviceAccount:" + serviceAccountEmail)
                    .build()

            Policy updatedPolicy = policy.toBuilder().addBindings(binding).build()
            SetIamPolicyRequest setPolicyRequest = SetIamPolicyRequest.newBuilder()
                    .setResource(topicName.toString())
                    .setPolicy(updatedPolicy)
                    .build()
            topicAdminClient.setIamPolicy(setPolicyRequest)
        } catch (Exception e) {
            log.error("Failed to assign subscriber role to dead letter topic", e)
        }
    }

    @Override
    boolean delete(String name) {
        try{
            subscriptionAdminClient.deleteSubscription("$SUBSCRIPTION_PREFIX${name}")
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
        subscriber.name = subscription.name.substring(SUBSCRIPTION_PREFIX.length())
        subscriber.url = subscription.getPushConfig().pushEndpoint
        subscriber.topic = subscription.topic.substring(PubSubTopicService.TOPIC_PREFIX.length())
        subscriber.ackDeadlineSeconds = subscription.getAckDeadlineSeconds()
        return subscriber
    }
}
