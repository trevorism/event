package com.trevorism.service

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.Topic
import com.google.pubsub.v1.TopicName
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@jakarta.inject.Singleton
class PubSubTopicService implements TopicService {

    private TopicAdminClient topicAdminClient = TopicAdminClient.create()
    private static final Logger log = LoggerFactory.getLogger(PubSubTopicService.class.name)

    @Override
    boolean createTopic(String topicId) {
        try {
            TopicName topicName = TopicName.of(EventService.PROJECT_ID, topicId)
            Topic topic = topicAdminClient.createTopic(topicName)
            return topic
        } catch (Exception e) {
            log.warn("Failed to create topic: ${e.message}")
            return false
        }
    }

    List<String> getAllTopics() {
        ListTopicsRequest listTopicsRequest = ListTopicsRequest.newBuilder().setProject(ProjectName.format(EventService.PROJECT_ID)).build()
        List<String> topics = []
        for (Topic topic : topicAdminClient.listTopics(listTopicsRequest).iterateAll()) {
            topics << topic.name.substring("projects/${EventService.PROJECT_ID}/topics/".length())
        }
        return topics
    }

    String getTopic(String topic) {
        return topicAdminClient.getTopic("projects/${EventService.PROJECT_ID}/topics/$topic")?.name
    }

    boolean deleteTopic(String topic) {
        try {
            topicAdminClient.deleteTopic("projects/${EventService.PROJECT_ID}/topics/$topic")
            return true
        } catch (Exception e) {
            log.warn("Failed to delete topic: ${e.message}")
            return false
        }
    }
}
