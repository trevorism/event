package com.trevorism.controller

import com.trevorism.service.EventService
import org.junit.jupiter.api.Test

class EventControllerTests {

    @Test
    void testSendEvent(){
        EventController eventController = new EventController()
        eventController.eventService = [sendEvent: {String event, map, request -> event}] as EventService
        assert eventController.sendEvent("test", [:], null) == "test"
    }

    @Test
    void testSendEventError(){
        EventController eventController = new EventController()
        eventController.eventService = [sendEvent: {String event, map, request -> throw new Exception()}] as EventService
        try{
            assert eventController.sendEvent("test", [:], null)
            assert false
        }catch (Exception e){
            assert true
        }
    }
}
