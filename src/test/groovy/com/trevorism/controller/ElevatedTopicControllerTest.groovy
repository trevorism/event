package com.trevorism.controller

import com.trevorism.model.ElevatedTopic
import com.trevorism.service.TopicElevationService
import io.micronaut.http.exceptions.HttpStatusException
import org.junit.jupiter.api.Test

class ElevatedTopicControllerTest {

    @Test
    void testListElevatedTopics() {
        ElevatedTopicController controller = new ElevatedTopicController()
        controller.topicElevationService = service(list: { [new ElevatedTopic(topic: "testResult")] })

        assert controller.listElevatedTopics().size() == 1
    }

    @Test
    void testCreateElevatedTopic() {
        ElevatedTopicController controller = new ElevatedTopicController()
        controller.topicElevationService = service(create: { ElevatedTopic topic -> topic.id = "1"; topic })

        ElevatedTopic created = controller.createElevatedTopic(new ElevatedTopic(topic: "testResult"))

        assert created.id == "1"
    }

    @Test
    void testCreateWithoutTopicNameIsRejected() {
        ElevatedTopicController controller = new ElevatedTopicController()
        controller.topicElevationService = service([:])

        try {
            controller.createElevatedTopic(new ElevatedTopic())
            assert false
        } catch (HttpStatusException ignored) {
            assert true
        }
    }

    @Test
    void testDeleteElevatedTopic() {
        ElevatedTopicController controller = new ElevatedTopicController()
        controller.topicElevationService = service(delete: { String id -> new ElevatedTopic(id: id) })

        assert controller.deleteElevatedTopic("1").id == "1"
    }

    @Test
    void testDeleteMissingElevatedTopicIsNotFound() {
        ElevatedTopicController controller = new ElevatedTopicController()
        controller.topicElevationService = service(delete: { String id -> null })

        try {
            controller.deleteElevatedTopic("missing")
            assert false
        } catch (HttpStatusException ignored) {
            assert true
        }
    }

    private static TopicElevationService service(Map overrides) {
        Map base = [
                requiresElevation: { String topic -> false },
                mintInternalToken: { String tenantId -> null },
                list             : { [] },
                create           : { ElevatedTopic topic -> topic },
                delete           : { String id -> null }
        ]
        return (base + overrides) as TopicElevationService
    }
}
