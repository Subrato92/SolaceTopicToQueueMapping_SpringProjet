package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.config.DirectPublisher;
import com.subrato.packages.solace.TopicToQueueMapping.config.MessageRouter;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessagePayload;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.TopicPublisherRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.print.attribute.standard.Media;
import java.util.HashMap;

@RestController
@EnableSwagger2
@RequestMapping( value = "/topic-to-queue-mapping")
public class RestEndPoints {

    private RouterConfig defaultRouterConfig = null;
    private String defaultQueueName = null;
    private HashMap<String, DirectPublisher> lstOfDirectPublishers = new HashMap<String, DirectPublisher>();

    @PostMapping(
            value = "/routerconfig/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport setDefaultRouterConfig(@RequestBody RouterConfig config){
        if(!checkForValidity(config)) {
            return new StatusReport("Parameters Missing...", false);
        }
        defaultRouterConfig = config;
        return new StatusReport("Success", true);
    }

    @GetMapping(
            value = "/routerconfig/getDefault",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody RouterConfig getDefaultRouterConfig(){
        return defaultRouterConfig;
    }

    @PostMapping(
            value = "/queue/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody boolean setDefaultQueue(@RequestBody String defaultQueueName){

        this.defaultQueueName = defaultQueueName;
        return true;
    }

    @GetMapping(
            value = "/queue/getdefault",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody String getDefaultQueue(){
        return defaultQueueName;
    }

    @PostMapping(
            value = "/topicpublisher/add",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport createTopicPublisher(@RequestBody TopicPublisherRequest payload){

        if( payload.getRouterConfig().getTopicName() == null ){
            return new StatusReport("[MissingTopicName]", false);
        }
        MessageRouter router = null;
        boolean isValid = checkForValidity(payload.getRouterConfig());
        DirectPublisher publisher = null;

        if( !isValid && defaultRouterConfig == null ){
            return new StatusReport("Fields Missing. No Default Router Set.", false);
        }else if(isValid){
            router = new MessageRouter(payload.getRouterConfig());
        }else if(defaultRouterConfig != null){
            router = new MessageRouter(defaultRouterConfig);
        }

        if(payload.isSubscribeToQueue() && defaultQueueName == null){
            return new StatusReport("Default Queue Missing ...", false);
        }else if( !payload.isSubscribeToQueue() ){
            publisher = new DirectPublisher(router, payload.getRouterConfig().getTopicName(), null);
        }else {
            publisher = new DirectPublisher(router, payload.getRouterConfig().getTopicName(), defaultQueueName);
        }

        if(publisher != null){
            lstOfDirectPublishers.put(payload.getRouterConfig().getTopicName(), publisher);
        }

        return new StatusReport("Success", true);
    }

    public @ResponseBody StatusReport sendMessage(@RequestBody MessagePayload message){

        return new StatusReport("Success", true);
    }

    private boolean checkForValidity(RouterConfig config){
        if( config.getHost() == null || config.getPassword() == null || config.getUsername() == null || config.getVpn_name() == null ){
            return false;
        }

        return true;
    }

}
