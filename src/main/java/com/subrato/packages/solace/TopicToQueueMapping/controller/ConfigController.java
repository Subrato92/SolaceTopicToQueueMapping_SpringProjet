package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.config.*;
import com.subrato.packages.solace.TopicToQueueMapping.constants.Config;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.*;
import com.subrato.packages.solace.TopicToQueueMapping.utilities.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;

@RestController
@EnableSwagger2
@RequestMapping(value = "/topicToQueueMapping")
public class ConfigController {

    private Logger log = LoggerFactory.getLogger(ConfigController.class);

    @PostMapping(
            value = "/routerconfig/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport setDefaultRouterConfig(@RequestBody RouterConfig config) {
        if (!Validator.checkForValidity(config)) {
            return new StatusReport("Parameters Missing...", false);
        }

        Config.STATIC.setRouterConfig(config);

        return new StatusReport("Success", true);
    }

    @GetMapping(
            value = "/routerconfig/getDefault",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    RouterConfig getDefaultRouterConfig() {
        return Config.STATIC.getRouterConfig();
    }

    @PostMapping(
            value = "/queue/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    boolean setDefaultQueue(@RequestBody String defaultQueueName) {
        if(!Validator.stringValidator(defaultQueueName)){
            return false;
        }

        Config.STATIC.setDefaultQueueName(defaultQueueName);

        return true;
    }

    @GetMapping(
            value = "/queue/getdefault",
            produces = "text/plain"
    )
    public @ResponseBody
    String getDefaultQueue() {
        return Config.STATIC.getDefaultQueueName();
    }

}
