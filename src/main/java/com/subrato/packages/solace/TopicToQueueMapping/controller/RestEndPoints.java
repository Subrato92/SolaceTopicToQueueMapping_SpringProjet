package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.pojos.DefaultRouterConfig;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.print.attribute.standard.Media;

@RestController
@EnableSwagger2
@RequestMapping( value = "/topic-to-queue-mapping")
public class RestEndPoints {

    private DefaultRouterConfig defaultConfig = null;

    @PostMapping(
            value = "/routerconfig/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody boolean setDefaultRouterConfig(@RequestBody RouterConfig config){

        defaultConfig = new DefaultRouterConfig(config);
        return true;
    }




}
