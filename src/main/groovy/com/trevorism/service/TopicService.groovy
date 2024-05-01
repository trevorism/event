package com.trevorism.service

interface TopicService {

    boolean createTopic(String topicId)
    List<String> getAllTopics()
    String getTopic(String topic)
    boolean deleteTopic(String topic)
}