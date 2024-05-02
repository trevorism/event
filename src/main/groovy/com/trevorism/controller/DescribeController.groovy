package com.trevorism.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag


@Controller("/describe")
class DescribeController {

    @Tag(name = "Describe Operations")
    @Operation(summary = "Perform a describe data operation")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    def operate(@Body def query){
        return ["list", "create", "read", "delete"]
    }

    @Tag(name = "Describe Operations")
    @Operation(summary = "Get a description of the performable data actions")
    @Get(value = "{id}", produces = MediaType.APPLICATION_JSON)
    def operateById(String id){
        return ["list", "create", "read", "delete"]
    }

    @Tag(name = "Describe Operations")
    @Operation(summary = "Get a description of the performable data actions")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    def describe(){
        return ["list", "create", "read", "delete"]
    }
}
