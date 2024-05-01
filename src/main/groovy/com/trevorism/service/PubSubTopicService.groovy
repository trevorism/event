package com.trevorism.service

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicsRequest
import com.google.pubsub.v1.ProjectName
import com.google.pubsub.v1.Topic
import com.google.pubsub.v1.TopicName

import java.util.logging.Logger

@jakarta.inject.Singleton
class PubSubTopicService implements TopicService {

    private TopicAdminClient topicAdminClient = TopicAdminClient.create()
    private static final Logger log = Logger.getLogger(TopicService.class.name)

    @Override
    boolean createTopic(String topicId) {
        try {
            TopicName topicName = TopicName.of(EventService.PROJECT_ID, topicId)
            Topic topic = topicAdminClient.createTopic(topicName)
            return topic
        } catch (Exception e) {
            log.warning("Failed to create topic: ${e.message}")
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
        return topicAdminClient.getTopic(topic)?.name
    }

    boolean deleteTopic(String topic) {
        try {
            topicAdminClient.deleteTopic(topic)
            return true
        } catch (Exception e) {
            log.warning("Failed to delete topic: ${e.message}")
            return false
        }
    }
}
