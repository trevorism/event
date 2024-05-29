package com.trevorism.controller

import com.trevorism.model.EventTopic
import com.trevorism.service.TopicService
import org.junit.jupiter.api.Test

class TopicControllerTest {

    @Test
    void testCreateTopic(){
        TopicController topicController = new TopicController()
        topicController.topicService = [createTopic: {String topic -> true}] as TopicService
        assert topicController.createTopic(new EventTopic(name: "test")).name == "test"
    }

    @Test
    void testCreateTopicFailing(){
        TopicController topicController = new TopicController()
        topicController.topicService = [createTopic: {String topic -> false}] as TopicService
        try{
            assert topicController.createTopic(new EventTopic(name: "test"))
            assert false
        }catch (Exception ignored){
            assert true
        }
    }
}
